package ai.cogmission.mosaic;

import static ai.cogmission.mosaic.LayoutConstants.*;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class DividerDeserializer extends JsonDeserializer<Rectangle2D.Double> {
	@Override
    public Rectangle2D.Double deserialize(JsonParser jsonParser, 
        DeserializationContext deserializationContext) throws IOException {
		
		ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        
        String[] params = node.get(KEY_DIVIDER_BOUNDS).asText().split(CELL_PTRN);
        return new Rectangle2D.Double(
        	Double.parseDouble(params[X - 1]),
        	Double.parseDouble(params[Y - 1]),
        	Double.parseDouble(params[W - 1]),
        	Double.parseDouble(params[H - 1]));
	}
}
