package at.tugraz.oop2.CustomSerializer;

import java.io.IOException;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonWriter;
import org.springframework.boot.jackson.JsonComponent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.node.ObjectNode;



@JsonComponent
public class GeometrySerializer extends JsonSerializer<Geometry> {
    @Override
    public void serialize(Geometry geometry, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
            throws IOException {
        GeoJsonWriter writer = new GeoJsonWriter(6);
        String geoJson = writer.write(geometry); 
        ObjectNode geoJsonNode = new ObjectMapper().readValue(geoJson, ObjectNode.class);
        
        jsonGenerator.writeObject(geoJsonNode); 
    }
}
