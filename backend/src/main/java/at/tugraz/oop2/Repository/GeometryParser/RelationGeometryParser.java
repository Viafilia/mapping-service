package at.tugraz.oop2.Repository.GeometryParser;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;

import at.tugraz.oop2.Repository.Relation;
import at.tugraz.oop2.Repository.Node;
import at.tugraz.oop2.Repository.Way;

class ClosedCircle {
    public ClosedCircle(Polygon closedCircle, int lastMemberIndex) {
        this.closedCircle = closedCircle;
        this.lastMemberIndex = lastMemberIndex;
    }

    public Polygon getClosedCircle() {
        return closedCircle;
    }

    public int getLastMemberIndex() {
        return lastMemberIndex;
    }
        

    private Polygon closedCircle;
    private int lastMemberIndex;
}

class RelationGeometryParser {
    private HashMap<Long, Node> nodes_map;
    private HashMap<Long, Way> ways_map;
    private HashMap<Long, Relation> relations_map;
    private GeometryFactory factory;
    private Geometry geometry;

    public RelationGeometryParser(
            Relation relation, HashMap<Long, Node> nodes_map, 
            HashMap<Long, Way> ways_map, HashMap<Long, Relation> relations_map,
            GeometryFactory factory) {
        this.nodes_map = nodes_map;
        this.ways_map = ways_map;
        this.relations_map = relations_map;
        this.factory = factory;

        parse(relation);
    }

    public Geometry getGeometry() {
        return geometry;
    }

    private boolean addNonWayGeometries(Relation relation, LinkedList<Geometry> geometries) {
        List<Relation.ChildMember> childMembers = relation.getChildMembers();

        for (Relation.ChildMember member : childMembers) {
            if (!"way".equals(member.type)) {
                Geometry geom = geomFromMember(member);
                if (geom == null)
                    return false;
                else if (geom instanceof GeometryCollection) {
                    GeometryCollection collection = (GeometryCollection) geom;
                    for (int j = 0; j < collection.getNumGeometries(); j++)
                        geometries.add(collection.getGeometryN(j));
                } else 
                    geometries.add(geom);
            }
        }
        return true;
    }

    private void parseMuliPolygon(Relation relation) {
        List<Relation.ChildMember> childMembers = relation.getChildMembers();
        LinkedList<Polygon> inners = getCircles(childMembers, "inner");
        LinkedList<Polygon> outers = getCircles(childMembers, "outer");
        LinkedList<Geometry> geometries = new LinkedList<>();

        if (inners == null || outers == null || !addNonWayGeometries(relation, geometries)) {
            geometry = null;
            return;
        }

        for (Polygon outer : outers) {
            LinkedList<Polygon> multiPolygon = new LinkedList<>();
            multiPolygon.add(outer);

            inners.removeIf(inner -> {
                if (outer.contains(inner)) {
                    multiPolygon.add(inner);
                    return true;
                }
                return false;
            });

            geometries.add(factory.createMultiPolygon(multiPolygon.toArray(new Polygon[0])));
        }
        if (!geometries.isEmpty())
            geometry = factory.createGeometryCollection(geometries.toArray(new Geometry[0]));
    }

    private void parseGeometryCollection(Relation relation) {
        List<Relation.ChildMember> childMembers = relation.getChildMembers();

        ArrayList<Geometry> geometries = new ArrayList<>();
        for (Relation.ChildMember member : childMembers) {
            Geometry geom = geomFromMember(member);
            if (geom == null) {
                geometry = null;
                return;
            } else if (geom instanceof GeometryCollection) {
                GeometryCollection collection = (GeometryCollection) geom;
                for (int j = 0; j < collection.getNumGeometries(); j++)
                    geometries.add(collection.getGeometryN(j));
            } else 
                geometries.add(geom);
        }
        geometry = factory.createGeometryCollection(geometries.toArray(new Geometry[0]));
    }

    private void parse(Relation relation) {
        if ("multipolygon".equals(relation.getTags().get("type")))
            parseMuliPolygon(relation);
        else
            parseGeometryCollection(relation);
    }

    private Geometry geomFromMember(Relation.ChildMember member) {
        if (member.type.equals("way")) {
            Way way = ways_map.get(member.ref);
            if (way != null)
                return way.getGeom();
        } else if (member.type.equals("relation")) {
            Relation relation = relations_map.get(member.ref);
            if (relation != null) {
                relation.buildGeom(nodes_map, ways_map, relations_map);
                return relation.getGeom();
            }
        } else if (member.type.equals("node")) {
            Node node = nodes_map.get(member.ref);
            if (node != null)
                return node.getGeom();
        }
        return null;
    }

    private Coordinate[] listFromMember(Relation.ChildMember member) {
        Way way = ways_map.get(member.ref);
        if (way == null)
            return null;

        return way.getGeom().getCoordinates();
    }

    private LinkedList<Polygon> getCircles(List<Relation.ChildMember> childMembers, String role) {
        CoordList coordList = new CoordList();
        LinkedList<Polygon> circles = new LinkedList<>();

        for (Relation.ChildMember member : childMembers) {
            if (!role.equals(member.role) || !"way".equals(member.type))
                continue;

            Coordinate[] coordinates = listFromMember(member);
            if (coordinates == null) 
                return null;

            coordList.add(coordinates);
            Coordinate[] closedCircle = coordList.getClosedCircle();
            if (closedCircle != null)
                circles.add(factory.createPolygon(closedCircle));
        }

        return circles;
    }
}
