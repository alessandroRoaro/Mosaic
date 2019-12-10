package ai.cogmission.mosaic.refimpl;


import ai.cogmission.mosaic.Log;
import ai.cogmission.mosaic.ModelLoader;
import ai.cogmission.mosaic.Position;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Map;
import java.util.Random;

public class MosaicPaneRefImpl extends Application {
	/** Mapping of element id's to labels for later reference when serializing */
	private Map<String, Label> clientMap = new java.util.HashMap<>();
	/** Holds colors to use for component illustration */
	private String[] colors = new String[] { "blue", "red", "green", "yellow", "orange" };
	/** Used to randomize ui element colors */
	private Random random = new Random();

	private MosaicPane<Node> mosaicPane;

	private boolean pippoIsDropped = false;
	private boolean isDraggingOutside = false;
	private Stage draggedWindow;


	@Override
	public void start(Stage stage) throws Exception {
		Parameters params = getParameters();
		Map<String, String> map = params.getNamed();
		Log.d("params = " + params);
		
		ModelLoader loader = new ModelLoader("C:\\Users\\PC-1-\\workspace\\Mosaic\\out\\production\\resources\\testModel.txt");
		String[] model = loader.getModel(map.get("surface"));

		Log.d("model.length = " + model.length);
		mosaicPane = new MosaicPane<>();
		mosaicPane.setNodeMinHeight(100);
		mosaicPane.setNodeMinWidth(300);

		int i = 0;

		for (String def : model) {
			String[] args = def.split("[\\s]*\\,[\\s]*");
			int offset = args.length > 4 ? args.length - 4 : 0;
			String id = args.length == 4 ? "" + (i++) : args[0];
			Label l = getLabel(i > 4 ? colors[random.nextInt(5)] : colors[i], id);
			mosaicPane.add(l, id, 
				Double.parseDouble(args[offset + 0]), 
				Double.parseDouble(args[offset + 1]),
				Double.parseDouble(args[offset + 2]),
				Double.parseDouble(args[offset + 3]));
			clientMap.put(id, l);
		}
		
        mosaicPane.getEngine().addSurface(mosaicPane.getSurface());



		AnchorPane root = new AnchorPane();
		Label pippo = new Label("Drag me!");
		pippo.setId("pippo");
		pippo.setStyle("-fx-background-color: " + colors[0] + ";-fx-alignment:center;-fx-text-alignment:center;");
		pippo.setTextFill(Color.WHITE);
		pippo.setPrefWidth(350);
		pippo.setPrefHeight(100);
		pippo.setOnDragDetected(event -> {
			if (pippoIsDropped) return;

			Dragboard db = pippo.startDragAndDrop(TransferMode.ANY);
			ClipboardContent content = new ClipboardContent();
			content.put(MosaicPane.MOSAIC_DRAG_OVER_ITEM_FORMAT, "pippo");
			db.setContent(content);
			db.setDragView(pippo.snapshot(null, null));
			event.consume();
		});

		root.setOnMouseDragged(dragEvent -> {
			// are we dragging a window around? draggedWindow can be null when we are just resizing and the drag goes outside.
			if (draggedWindow == null || !isDraggingOutside && mosaicPane.localToScene(mosaicPane.getBoundsInLocal()).contains(dragEvent.getX(), dragEvent.getY())) return;

			isDraggingOutside = true;

			draggedWindow.setX(dragEvent.getScreenX());
			draggedWindow.setY(dragEvent.getScreenY());
		});

		root.setOnMouseReleased(mouseDragEvent -> {
			if (isDraggingOutside) {
				isDraggingOutside = false;
			}
		});

		root.getChildren().add(pippo);

		Pane mosaicContainer = new Pane(mosaicPane);
		mosaicContainer.setPrefWidth(1200);
		mosaicContainer.setPrefHeight(900);
		mosaicPane.setPrefWidth(1200);
		mosaicPane.setPrefHeight(900);
		mosaicContainer.setLayoutX(500);
		root.getChildren().add(mosaicContainer);

		Scene scene = new Scene(root, 1600, 900);
	    stage.setTitle("Mosaic Layout Engine Demo (JavaFX)");
		stage.setScene(scene);
		stage.show();

		mosaicPane.setListener(new MosaicPane.MosaicPaneListener() {
			public void nodeEnteredWithDrag (DragEvent evt, String nodeId) {
				Label label = clientMap.get(nodeId);
				label.setBorder(new Border(new BorderStroke(Color.PURPLE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.THICK)));
//				label.setStyle("-fx-border-color: purple; -fx-border-width: 2px;");
			}

			public void nodeExitedWithDrag (DragEvent evt, String nodeId) {
				Label label = clientMap.get(nodeId);
				label.setBorder(null);
			}

			public void dragDropped (DragEvent evt, String nodeId, double x, double y) {
				Label target = clientMap.get(nodeId);
				Position pos;

				if (target.getWidth() >= (pippo.getWidth() * 3 / 2)) {
					pos = Position.EAST;
				}
				else {
					pos = Position.SOUTH;
				}

				Label newLabel = getLabel(colors[1], "10");
				pippoIsDropped = true;
				mosaicPane.addIntoNode(newLabel, "10", target, pos);
			}

			public void dragExited(MouseEvent evt, String nodeId) {
				Pane root = new Pane();

				// make ghost snapshot of the dragged item
				Label selectedNode = clientMap.get(nodeId);
				double originalWidth = selectedNode.getWidth();
				double originalHeight = selectedNode.getHeight();
				selectedNode.resize(200, 200);
				ImageView ghostImage = new ImageView(selectedNode.snapshot(null, null));
				ghostImage.setStyle("-fx-opacity: 0.5; -fx-effect: dropshadow(three-pass-box, #333, 10, 0, 0, 0);");
				selectedNode.resize(originalWidth, originalHeight);
				root.getChildren().add(ghostImage);

				// show view in a new window
				draggedWindow = new Stage();
				draggedWindow.initModality(Modality.WINDOW_MODAL);
				draggedWindow.initStyle(StageStyle.UNDECORATED);
				draggedWindow.setScene(new Scene(root));
				draggedWindow.setX(evt.getScreenX());
				draggedWindow.setY(evt.getScreenY());
				draggedWindow.show();
			}
		});
	}

	
	public Label getLabel(String color, String id) {
		Label label = new Label();
		label.textProperty().set(id);
		label.textAlignmentProperty().set(TextAlignment.CENTER);
		label.alignmentProperty().set(Pos.CENTER);
		label.setOpacity(1.0);
		label.setTextFill(Color.WHITE);
		label.setFont(Font.font("Arial", FontWeight.BOLD, 16d));
		label.setStyle("-fx-background-color: " + color + ";-fx-alignment:center;-fx-text-alignment:center;");
		label.setManaged(false);
		label.setOnMouseClicked(mouseEvent -> {
			Log.d("click!");
			if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
				mosaicPane.remove(id);
			}
		});
		
		return label;
	}
	
	public static void main(String[] args) {
		if(args == null || args.length < 1 || args[0] == null) {
//		    URL url = MosaicPaneRefImpl.class.getResource("/testModel.txt");
//		    String path = Paths.get(url.toExternalForm()).toAbsolutePath().toString();
//		    try{
//		        path = Paths.get(url.toURI()).toAbsolutePath().toString();
//		    }catch(Exception e) { e.printStackTrace(); }

			String path = "C:/Users/PC-1-/workspace/Mosaic/out/production/resources/testModel.txt";
			args = new String[] { "--file="+path, "--surface=test"};
		}
		Log.d(System.getProperty("user.dir"));
        launch(args);
    }
}
