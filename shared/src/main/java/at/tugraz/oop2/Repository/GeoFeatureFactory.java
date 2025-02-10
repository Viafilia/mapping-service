package at.tugraz.oop2.Repository;

import java.util.HashMap;

import org.locationtech.jts.geom.Geometry;

public class GeoFeatureFactory {
    public static GeoFeature createGeoFeature(String geoFeatureType, 
            long id, HashMap<String, String> tags,
            Geometry geom, long[] child_ids) {
        if (!tags.containsKey(geoFeatureType))
            return null;

        String type = tags.getOrDefault(geoFeatureType, "");
        String name = tags.getOrDefault("name", "");

        switch (geoFeatureType) {
            case "highway":
                return new GeoFeature(type, name, id, tags, geom, child_ids);
            case "amenity":
                return new GeoFeature(type, name, id, tags, geom, null);
            case "landuse": 
                return new GeoFeature(type, name, id, tags, geom, null);
            case "natural":
                return new GeoFeature(type, name, id, tags, geom, null);
            default:
                return null;
        }
    }
}
