package ai.cogmission.mosaic;

import javafx.application.Platform;
import javafx.event.EventType;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.awt.geom.Rectangle2D;

/**
 * An implementation of a container object which is meant to be used
 * with the {@link MosaicEngineImpl}.
 * 
 * @author David Ray
 *
 * @param <T>
 */
public class MosaicPane<T extends Node> extends Region {
	public static final DataFormat MOSAIC_DRAG_OVER_ITEM_FORMAT = new DataFormat("javafx.scene.Node");

	private MosaicEngine<T> layoutEngine;
	private Surface<T> surface;
	private Group content;
	private double nodeMinWidth;
	private double nodeMinHeight;
	private MosaicPaneListener listener;

	private Object itemBeingDraggedFromOutside;
	private String currentNodeIdDraggedOverFromOutside;
	private boolean hasDraggedNodeOutside;
	
	/**
	 * Constructs a new {@code MosaicPane}
	 */
	public MosaicPane() {
		this(null, null, null);
    }
	
	/**
	 * Constructs a new MosaicPane using the specified
	 * {@link MosaicEngine} and {@link Surface}. 
	 * 
	 *  This constructor is used to construct a pane containing the same engine
	 *  and surface definition as a previously configured pane for copying,
	 *  serialization etc.
	 * 
	 * @param engine	the layout engine
	 * @param surface 	the pre-configured surface
	 * @param group		the {@link Group} containing ui elements
	 */
	public MosaicPane(MosaicEngine<T> engine, Surface<T> surface, Group group) {
		if(engine == null || surface == null) {
			this.layoutEngine = new MosaicEngineBuilder<T>().build(this);
			
			MosaicSurfaceBuilder<T> builder = new MosaicSurfaceBuilder<T>();
			this.surface = builder
				.useIntegerPrecision(false)
				.cornerClickRadius(5)
				.useSurfaceOffset(false)
				.dividerSize(10)
				.snapDistance(15).build();
			
			this.surface.addChangeListener(getSurfaceObserver());
			
			content = new Group();
	        content.setManaged(false);
	        getChildren().add(content);
	    }else{
			this.layoutEngine = engine;
			this.surface = surface;
			this.content = group;
		}
		
		layoutBoundsProperty().addListener((arg0, arg1, arg2) -> {
			if(arg2.getWidth() == 0 || arg2.getHeight() == 0) return;
			this.surface.setArea(new Rectangle2D.Double(0, 0, arg2.getWidth(), arg2.getHeight()));
			this.surface.requestLayout();
		});
        
        addEventHandler(MouseEvent.ANY, evt -> {
			EventType type = evt.getEventType();
			if(type == MouseEvent.MOUSE_PRESSED) {
				this.surface.mousePressed(evt.getX(), evt.getY());
				hasDraggedNodeOutside = false;
			}
			else if(type == MouseEvent.MOUSE_DRAGGED) {
				if (!hasDraggedNodeOutside) { // if node has already been drag outside, don't take care of it anymore
					if (this.getLayoutBounds().contains(evt.getX(), evt.getY())) {
						this.surface.mouseDragged(evt.getX(), evt.getY());
					}
					else {
						if (this.surface.getInputManager().isDraggingNode) {
							hasDraggedNodeOutside = true;
							dragMovedOutside(evt);
						}
					}
				}
			}
			else if(type == MouseEvent.MOUSE_RELEASED) {
				this.surface.mouseReleased();
			}
			else if (type == MouseEvent.MOUSE_MOVED) {
				this.surface.mouseMoved(evt.getX(), evt.getY());
			}
		});

        setupDragAndDropHandlers();
	}


	private void setupDragAndDropHandlers () {
		setOnDragEntered(dragEvent -> {
			itemBeingDraggedFromOutside = dragEvent.getDragboard().getContent(MOSAIC_DRAG_OVER_ITEM_FORMAT);
			dragEvent.consume();
		});

		setOnDragOver(dragEvent -> {
			if (itemBeingDraggedFromOutside != null) {
				String nodeId = surface.dragOverFromOutside(dragEvent.getX(), dragEvent.getY());

				if (nodeId == null) {
					if (currentNodeIdDraggedOverFromOutside != null) {
						if (listener != null) {
							listener.nodeExitedWithDrag(dragEvent, currentNodeIdDraggedOverFromOutside);
						}

						currentNodeIdDraggedOverFromOutside = null;
					}
				}
				else if (listener != null) {
					if (currentNodeIdDraggedOverFromOutside != null && !nodeId.equals(currentNodeIdDraggedOverFromOutside)) {
						listener.nodeExitedWithDrag(dragEvent, currentNodeIdDraggedOverFromOutside);
					}

					listener.nodeEnteredWithDrag(dragEvent, nodeId);
					currentNodeIdDraggedOverFromOutside = nodeId;
				}
			}

			dragEvent.consume();
		});

		setOnDragExited(dragEvent -> {
			if (listener != null && currentNodeIdDraggedOverFromOutside != null) {
				listener.nodeExitedWithDrag(dragEvent, currentNodeIdDraggedOverFromOutside);
			}

			itemBeingDraggedFromOutside = null;
			currentNodeIdDraggedOverFromOutside = null;
			dragEvent.consume();
		});

		setOnDragDropped(dragEvent -> {
			if (listener != null && currentNodeIdDraggedOverFromOutside != null) {
				listener.dragDropped(dragEvent, currentNodeIdDraggedOverFromOutside, dragEvent.getX(), dragEvent.getY());
			}

			dragEvent.consume();
		});

		setOnDragDone(dragEvent -> {
			itemBeingDraggedFromOutside = null;
			currentNodeIdDraggedOverFromOutside = null;
			dragEvent.consume();
		});
	}

	
	/**
	 * Called to add an object to be laid out, to the layout engine.
	 * 
	 * @param t					the object to be laid out or key to such.
	 * @param percentX			the percentage of the overall width, the x position is located at.
	 * @param percentY			the percentage of the overall height, the y position is located at.
	 * @param percentWidth		the percentage of the overall width the object should occupy.
	 * @param percentHeight		the percentage of the overall height the object should occupy.
	 */
	public void add(T t, double percentX, double percentY, double percentWidth, double percentHeight) {
		surface.addRelative("", t, percentX, percentY, percentWidth, percentHeight, getNodeMinWidth(), Double.MAX_VALUE, getNodeMinHeight(), Double.MAX_VALUE);
		content.getChildren().add(t);

		checkEnableDragging();
	}
	
	/**
	 * Called to add an object to be laid out, to the layout engine applying the specified
	 * String id.
	 *  
	 * @param t					the object to be laid out or key to such.
	 * @param id				the user-specified String id.
	 * @param percentX			the percentage of the overall width, the x position is located at.
	 * @param percentY			the percentage of the overall height, the y position is located at.
	 * @param percentWidth		the percentage of the overall width the object should occupy.
	 * @param percentHeight		the percentage of the overall height the object should occupy.
	 */
	public void add(T t, String id, double percentX, double percentY, double percentWidth, double percentHeight) {
		surface.addRelative(id, t, percentX, percentY, percentWidth, percentHeight, getNodeMinWidth(), Double.MAX_VALUE, getNodeMinHeight(), Double.MAX_VALUE);
		content.getChildren().add(t);

		checkEnableDragging();
	}

	public void addIntoNode (T source, String id, T target, Position pos) {
		surface.requestAdd(source, id, target, pos, getNodeMinWidth(), getNodeMinHeight());

		checkEnableDragging();
	}

	public void remove (String id) {
		surface.requestRemove(id);

		checkEnableDragging();
	}

	private void checkEnableDragging () {
//		if (content.getChildren().size() > 1) {
			surface.setDragEnabled(true);
//		}
	}
	
	public MosaicEngine<T> getEngine() {
		return layoutEngine;
	}
	
	public Surface<T> getSurface() {
		return surface;
	}
	
	public SurfaceListener<T> getSurfaceObserver() {
		SurfaceListener<T> l = (changeType, n, id, r1, r2) -> {
			switch(changeType) {
				case REMOVE_DISCARD: {
					n.setEffect(null);

					// fix for a ui glitch that would make the drop shadow stay even when the node is removed
					Platform.runLater(() -> {
						content.getChildren().remove(n);
						requestLayout();
					});
					break;
				}
				case RESIZE_RELOCATE:
				case RELOCATE_DRAG_TARGET:
				case ANIMATE_RESIZE_RELOCATE:
				case RESIZE_DRAG_TARGET: {
					n.resizeRelocate(r2.getX(), r2.getY(), r2.getWidth(), r2.getHeight());
					requestLayout();
					break;
				}
				case ADD_COMMIT: {
					content.getChildren().add(n);
					n.resizeRelocate(r2.getX(), r2.getY(), r2.getWidth(), r2.getHeight());
					requestLayout();
					break;
				}
				case MOVE_BEGIN: {
					DropShadow shadow = new DropShadow();
					shadow.setOffsetX(0);
					shadow.setOffsetY(0);
					shadow.setRadius(15);
					shadow.setColor(Color.valueOf("#333"));
					n.setEffect(shadow);
					n.toFront();
					n.setOpacity(.58);
					break;
				}
				case MOVE_END: {
					n.setOpacity(1);
					n.setEffect(null);
					break;
				}
			}
		};

		return l;
	}


	private void dragMovedOutside (MouseEvent evt) {
		if (listener == null) return;

		String selectedNodeID = getSurface().getInputManager().getSelectedElement().stringID;
		surface.mouseReleased();
		remove(selectedNodeID);
		listener.dragExited(evt, selectedNodeID);
	}


	public void setNodeMinWidth(double width) {
		this.nodeMinWidth = width;
	}


	public double getNodeMinWidth() {
		return nodeMinWidth;
	}


	public void setNodeMinHeight(double height) {
		this.nodeMinHeight = height;
	}


	public double getNodeMinHeight() {
		return nodeMinHeight;
	}


	public void setListener (MosaicPaneListener listener) {
		this.listener = listener;
	}


	public int getChildrenCount () {
		return content.getChildren().size();
	}


	public interface MosaicPaneListener {

		void nodeEnteredWithDrag(DragEvent evt, String nodeId);
		void nodeExitedWithDrag(DragEvent evt, String nodeId);
		void dragDropped(DragEvent evt, String nodeId, double x, double y);
		void dragExited(MouseEvent evt, String nodeId);
	}
}