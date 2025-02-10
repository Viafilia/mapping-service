package at.tugraz.oop2.Repository;

import lombok.Data;

import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import at.tugraz.oop2.Repository.GeometryParser.GeometryParser;

import java.util.List;
import java.util.ArrayList;

@Data
public class Relation extends Node {
    public class ChildMember {
        public ChildMember(String type, long ref, String role) {
            this.type = type;
            this.ref = ref;
            this.role = role;
        }

        public String type;
        public long ref;
        public String role;
    }

    private List<ChildMember> childMembers;

    public Relation(Element element) {
        super(element);

        parseChildMember(element);
    }

    private void parseChildMember(Element element) {
        NodeList memberNodes = element.getElementsByTagName("member");
        int numNodes = memberNodes.getLength();
        childMembers = new ArrayList<>(numNodes);
        for (int i = 0; i < numNodes; i++) {
            Element memberElement = (Element) memberNodes.item(i);

            childMembers.add(new ChildMember(
                    memberElement.getAttribute("type"),
                    Long.parseLong(memberElement.getAttribute("ref")),
                    memberElement.getAttribute("role")));
        }
    }

    public void buildGeom(HashMap<Long, Node> nodes_maps, 
            HashMap<Long, Way> ways_maps, HashMap<Long, Relation> relations_maps) {
        if (geom != null) 
            return;

        geom = GeometryParser.parseRelationGeo(this, nodes_maps,
                ways_maps, relations_maps);
    }

    public void removeChildren(HashMap<Long, Node> nodes_map, 
            HashMap<Long, Way> ways_map) {
        for (ChildMember member : childMembers) {
            if (member.type.equals("node") && nodes_map.containsKey(member.ref)) {
                nodes_map.remove(member.ref);
            } else if (member.type.equals("way") && ways_map.containsKey(member.ref)) {
                Way way = ways_map.get(member.ref);
                way.removeChildren(nodes_map);
                ways_map.remove(member.ref);
            }
        }
    }

    public GeoFeature toGeoFeature(String geoFeatureType) {
        long[] childIds = getChildIds();
        return GeoFeatureFactory.createGeoFeature(
                geoFeatureType, id, tags, geom, childIds);
    }

    public long[] getChildIds() {
        long[] childIds = new long[childMembers.size()];
        for (int i = 0; i < childMembers.size(); i++) {
            childIds[i] = childMembers.get(i).ref;
        }
        return childIds;
    }

    public void removeMissingChildIds(HashMap<Long, Node> nodes_map, 
            HashMap<Long, Way> ways_map, HashMap<Long, Relation> relations_map) {
        childMembers.removeIf(member -> {
            if (member.type.equals("node")) {
                return !nodes_map.containsKey(member.ref);
            } else if (member.type.equals("way")) {
                return !ways_map.containsKey(member.ref);
            } else if (member.type.equals("relation")) {
                return !relations_map.containsKey(member.ref);
            }
            return true;
        });
    }
}
