package at.tugraz.oop2.Repository;

import lombok.Data;
import org.locationtech.jts.geom.Geometry;
import java.util.HashMap;

import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import at.tugraz.oop2.Repository.GeometryParser.GeometryParser;

@Data
public class Node {
    protected long id;
    protected Geometry geom = null;
    protected HashMap<String, String> tags;

    public Node(Element element) {
        parseId(element);
        parseTags(element);
    }

    private void parseId(Element element) {
        id = Long.parseLong(element.getAttribute("id"));
    }

    private void parseTags(Element element) {
        tags = new HashMap<>();
        NodeList tagNodes = element.getElementsByTagName("tag");
        for (int i = 0; i < tagNodes.getLength(); i++) {
            Element tagElement = (Element) tagNodes.item(i);
            String key = tagElement.getAttribute("k");
            String value = tagElement.getAttribute("v");
            tags.put(key, value);
        }
    }

    public void buildGeom(Element element) {
        if (geom != null) 
            return;

        geom = GeometryParser.parseNodeGeo(this, element);
    }

    public GeoFeature toGeoFeature(String geoFeatureType) {
        return GeoFeatureFactory.createGeoFeature(
                geoFeatureType, id, tags, geom, new long[0]);
    }
}
