package at.tugraz.oop2.CustomSerializer;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

@JsonComponent
public class GeometryDeserializer extends JsonDeserializer<Geometry> {
    private final GeometryFactory geometryFactory = new GeometryFactory(); 

    @Override
    public Geometry deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        
        String geoJson = node.toString(); 
        GeoJsonReader reader = new GeoJsonReader(geometryFactory);

        try {
            return reader.read(geoJson);
        } catch (ParseException e) {
            throw new IOException("Failed to parse geometry", e);
        }
    }
}
