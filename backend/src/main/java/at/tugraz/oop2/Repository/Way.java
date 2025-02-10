package at.tugraz.oop2.Repository;

import lombok.Data;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.tugraz.oop2.Repository.GeometryParser.GeometryParser;

import java.util.List;
import java.util.ArrayList;

@Data
public class Way extends Node {
    private List<Long> childNodes;

    public Way(Element element) {
        super(element);

        parseChildNodes(element);
    }
    
    private void parseChildNodes(Element element) {
        NodeList ndNodes = element.getElementsByTagName("nd");
        int numNodes = ndNodes.getLength();
        childNodes = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            Element ndElement = (Element) ndNodes.item(i);
            long ref = Long.parseLong(ndElement.getAttribute("ref"));
            childNodes.add(ref);
        }
    }

    public void buildGeom(HashMap<Long, Node> nodes_map) {
        if (geom != null) 
            return;
        
        geom = GeometryParser.parseWayGeo(this, nodes_map);
    }

    public void removeChildren(HashMap<Long, Node> nodes_map) {
        for (long id : childNodes) {
            if (nodes_map.containsKey(id)) {
                nodes_map.remove(id);
            }
        }
    }

    public GeoFeature toGeoFeature(String geoFeatureType) {
        long[] childIds = getChildIds();
        return GeoFeatureFactory.createGeoFeature(
                geoFeatureType, id, tags, geom, childIds);
    }

    public long[] getChildIds() {
        long[] childIds = new long[childNodes.size()];
        for (int i = 0; i < childNodes.size(); i++) {
            childIds[i] = childNodes.get(i);
        }
        return childIds;
    }

    public void removeMissingChildIds(HashMap<Long, Node> nodes_map) {
        childNodes.removeIf(id -> !nodes_map.containsKey(id));
    }

    public boolean isHighway() {
        return tags.containsKey("highway");
    }
}

