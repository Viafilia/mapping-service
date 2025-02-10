package at.tugraz.oop2.Repository;

import java.util.HashMap;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import at.tugraz.oop2.CustomSerializer.GeometrySerializer;
import at.tugraz.oop2.CustomSerializer.GeometryDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonInclude;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoFeature {
    String type, name;
    long id;
    HashMap<String, String> tags;
    long[] child_ids;

    @JsonSerialize(using = GeometrySerializer.class)
    @JsonDeserialize(using = GeometryDeserializer.class)
    Geometry geom;

    public GeoFeature() {
        type = "";
        name = "";
        id = 0;
        tags = new HashMap<>();
        child_ids = null;
        geom = null;
    }

    public GeoFeature(String type, String name,
            long id, HashMap<String, String> tags,
            Geometry geom, long[] child_ids) {
        this.id = id;
        this.tags = tags;
        this.geom = geom;
        this.type = type;
        this.name = name;
        this.child_ids = child_ids;
    }

    public double getOverlappingArea(Polygon bbox) {
        if (bbox == null || geom == null) 
            return 0.0;
        Geometry transformedGeom = CoordinateTransform.transform(geom);

        return OverlappingArea.calculate(transformedGeom, bbox);
    }

    boolean isInsideBbox(Polygon bbox) {
        if (bbox == null || geom == null) 
            return false;

        return geom.intersects(bbox);
    }

    boolean isInsideCircle(Point point, double range) {
        if (point == null || geom == null) 
            return false;

        Geometry transformedGeom = CoordinateTransform.transform(geom);

        return transformedGeom.distance(point) <= range;
    }

    public String toJson() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(this);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "";
        } 
    }

    public void fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            GeoFeature feature = mapper.readValue(json, GeoFeature.class);

            this.id = feature.id;
            this.tags = feature.tags;
            this.geom = feature.geom;
            this.type = feature.type;
            this.name = feature.name;
            this.child_ids = feature.child_ids;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public double countSpeed()
    {
        if (tags.containsKey("maxspeed"))
        {
            try
            {
                return Double.parseDouble(tags.get("maxspeed"));
            }
            catch (Exception e)
            {
                return 30;
            }
        }
        else
            return 30;
    }
}
