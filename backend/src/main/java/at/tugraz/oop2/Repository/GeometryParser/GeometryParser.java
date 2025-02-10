package at.tugraz.oop2.Repository.GeometryParser;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;

import org.w3c.dom.Element;

import java.util.HashMap;

import at.tugraz.oop2.Repository.Node;
import at.tugraz.oop2.Repository.Way;
import at.tugraz.oop2.Repository.Relation;

import java.util.List;

public class GeometryParser {
    static private GeometryFactory factory = new GeometryFactory();

    public static Geometry parseNodeGeo(Node node, Element element) {
        try {
            double lat = Double.parseDouble(element.getAttribute("lat"));
            double lon = Double.parseDouble(element.getAttribute("lon"));

            return factory.createPoint(new Coordinate(lon, lat));
        } catch (NumberFormatException e) {
            System.out.println("Error: could not build Geometry of Node " + node.getId() + " tags: " + node.getTags());
            return null;
        } 
    }

    static public Geometry parseWayGeo(Way way, HashMap<Long, Node> nodes_map) {
        List<Long> childNodes = way.getChildNodes();

        Coordinate[] coordinates = new Coordinate[childNodes.size()];
        for (int i = 0; i < childNodes.size(); i++) {
            long id = childNodes.get(i);

            if (!nodes_map.containsKey(id)) 
            {
                System.out.println("Error: could not build Geometry of Way " + way.getId() + " tags: " + way.getTags());
                return null;
            }

            Node node = nodes_map.get(id);
            coordinates[i] = new Coordinate(node.getGeom().getCoordinate());
        }

        if (coordinates.length == 0) {
            System.out.println("Error: could not build Geometry of Way " + way.getId() + " tags: " + way.getTags());
            return null;
        }

        if (coordinates.length == 1) {
            return factory.createPoint(coordinates[0]);
        } else if (coordinates.length > 2 
                && coordinates[0].equals(coordinates[coordinates.length - 1])) {
            return factory.createPolygon(coordinates);
        } else {
            return factory.createLineString(coordinates);
        }
    }

    static public Geometry parseRelationGeo(
            Relation relation, HashMap<Long, Node> nodes_map, 
            HashMap<Long, Way> ways_map, HashMap<Long, Relation> relations_map) {

        RelationGeometryParser parser = new RelationGeometryParser(
                relation, nodes_map, ways_map, relations_map,
                factory);

        Geometry geom = parser.getGeometry();

        if (geom == null)
            System.out.println("Error: could not build Geometry of Relation " + relation.getId() + " tags: " + relation.getTags());

        return geom;
    }
}
