package at.tugraz.oop2;

import at.tugraz.oop2.Repository.*;
import lombok.Data;
import org.opengis.referencing.FactoryException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

@Data
public class LoadOsm {
    private HashMap<Long, Node> nodes_maps;
    private HashMap<Long, Way> ways_maps;
    private HashMap<Long, Relation> relations_maps;

    public LoadOsm(String osmfile) throws ParserConfigurationException, IOException, SAXException, FactoryException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document osm = builder.parse(new File(osmfile));

        nodes_maps = parseNode(osm);
        ways_maps = parseWay(osm);
        relations_maps = parseRelation(osm);
    }

    public void finish() {
        removeReferencedElements();
        MapLogger.backendLoadFinished(nodes_maps.size(), ways_maps.size(), relations_maps.size());

        nodes_maps.clear();
        ways_maps.clear();
        relations_maps.clear();
    }

    private HashMap<Long, Node> parseNode(Document osm) {
        HashMap<Long, Node> nodes_map = new HashMap<>();
        NodeList nodeList = osm.getElementsByTagName("node");
        int num_node = nodeList.getLength();

        for (int i = 0; i < num_node; i++) {
            Element node_element = (Element) nodeList.item(i);
            Node parsed_node = new Node(node_element);

            parsed_node.buildGeom(node_element);
            nodes_map.put(parsed_node.getId(), parsed_node);
        }
        return nodes_map;
    }

    private HashMap<Long, Way> parseWay(Document osm) {
        HashMap<Long, Way> ways_map = new HashMap<>();
        NodeList wayList = osm.getElementsByTagName("way");
        int num_way = wayList.getLength();

        for (int i = 0; i < num_way; i++) {
            Element way_element = (Element) wayList.item(i);
            Way parsed_way = new Way(way_element);

            // with this only the missing child is ignored and not the whole geometry
            // parsed_way.removeMissingChildIds(nodes_maps);
            
            parsed_way.buildGeom(nodes_maps);
            ways_map.put(parsed_way.getId(), parsed_way);
        }
        return ways_map;
    }

    private HashMap<Long, Relation> parseRelation(Document osm) {
        HashMap<Long, Relation> relations_map = new HashMap<>();
        NodeList relationList = osm.getElementsByTagName("relation");
        int num_relation = relationList.getLength();

        for (int i = 0; i < num_relation; i++) {
            Element relation_element = (Element) relationList.item(i);
            Relation parsed_relation = new Relation(relation_element);

            relations_map.put(parsed_relation.getId(), parsed_relation);
        }

        for (Relation relation : relations_map.values()) {
            // with this only the missing child is ignored and not the whole geometry
            // relation.removeMissingChildIds(nodes_maps, ways_maps, relations_map);

            relation.buildGeom(nodes_maps, ways_maps, relations_map);
        }

        return relations_map;
    }

    private void removeReferencedElements() {
        for (Way way : ways_maps.values())
            way.removeChildren(nodes_maps);

        for (Relation relation : relations_maps.values())
            relation.removeChildren(nodes_maps, ways_maps);
    }

    public <T extends GeoFeature> Repository createRepository(String type) {
        Repository repo = new Repository();

        for (Node n : nodes_maps.values()) {
            GeoFeature feature = n.toGeoFeature(type);
            if (feature != null)
                repo.addFeature(feature);
        }

        for (Way w : ways_maps.values()) {
            GeoFeature feature = w.toGeoFeature(type);
            if (feature != null)
                repo.addFeature(feature);
        }

        for (Relation r : relations_maps.values()) {
            GeoFeature feature = r.toGeoFeature(type);
            if (feature != null)
                repo.addFeature(feature);
        }

        repo.sort();

        return repo;
    }

}
