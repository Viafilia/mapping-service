package at.tugraz.oop2.LandUsage;

import at.tugraz.oop2.LandUsageProto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import at.tugraz.oop2.LandUsageEntryProto;
import at.tugraz.oop2.LandUsageParameterProto;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Coordinate;

import at.tugraz.oop2.Repository.GeoFeature;
import at.tugraz.oop2.Repository.Repository;
import at.tugraz.oop2.Repository.CoordinateTransform;

@Data
class LandUsageEntry {
    private String type;
    private double area;
    private double share;
}

@Data
public class LandUsage {
    private double area;
    private LinkedList<LandUsageEntry> usages;

    public LandUsage() {
        area = 0.0;
        usages = new LinkedList<>();
    }

    public LandUsage(LandUsageParameterProto parameter, Repository repository) {
        Polygon bbox = createBoundingBox(
                parameter.getBboxTlX(), parameter.getBboxTlY(),
                parameter.getBboxBrX(), parameter.getBboxBrY());

        Map<String, Double> areas = getAreas(repository, bbox);

        fromAreasMap(bbox.getArea(), areas);
    }

    private Polygon createBoundingBox(double tlX, double tlY, double brX, double brY) {
        Polygon bbox = Repository.factory.createPolygon(
                new Coordinate[] {
                        new Coordinate(tlX, tlY),
                        new Coordinate(tlX, brY),
                        new Coordinate(brX, brY),
                        new Coordinate(brX, tlY),
                        new Coordinate(tlX, tlY)
                });

        return (Polygon) CoordinateTransform.transform(bbox);
    }

    private Map<String, Double> getAreas(Repository repository, Polygon bbox) {
        Map<String, Double> areas = new HashMap<>();

        for (GeoFeature entry : repository.getEntries()) {
            String type = entry.getType();
            double area = entry.getOverlappingArea(bbox);

            if (area <= 0.0)
                continue;

            if (areas.containsKey(type)) {
                areas.put(type, areas.get(type) + area);
            } else {
                areas.put(type, area);
            }
        }

        return areas;
    }

    private void fromAreasMap(double bboxArea, Map<String, Double> areas) {
        area = bboxArea;
        usages = new LinkedList<>();

        for (Map.Entry<String, Double> entry : areas.entrySet()) {
            LandUsageEntry landUsageEntry = new LandUsageEntry();
            landUsageEntry.setType(entry.getKey());
            landUsageEntry.setArea(entry.getValue());
            landUsageEntry.setShare(entry.getValue() / bboxArea);
            usages.add(landUsageEntry);
        }

        usages.sort((a, b) -> Double.compare(a.getArea(), b.getArea()));
    }

    public LandUsageProto toProto() {
        LandUsageProto.Builder builder = LandUsageProto.newBuilder();

        builder.setArea(area);
        for (LandUsageEntry entry : usages) {
            LandUsageEntryProto.Builder entryBuilder = LandUsageEntryProto.newBuilder();
            entryBuilder.setType(entry.getType());
            entryBuilder.setArea(entry.getArea());
            entryBuilder.setShare(entry.getShare());
            builder.addEntries(entryBuilder);
        }

        return builder.build();
    }

    public static LandUsage fromProto(LandUsageProto proto) {
        LandUsage landUsage = new LandUsage();
        landUsage.area = proto.getArea();
        landUsage.usages = new LinkedList<>();

        for (int i = 0; i < proto.getEntriesCount(); i++) {
            LandUsageEntryProto entryProto = proto.getEntries(i);
            LandUsageEntry entry = new LandUsageEntry();
            entry.setType(entryProto.getType());
            entry.setArea(entryProto.getArea());
            entry.setShare(entryProto.getShare());
            landUsage.usages.add(entry);
        }

        return landUsage;
    }
}
