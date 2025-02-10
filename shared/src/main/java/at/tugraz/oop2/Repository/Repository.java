package at.tugraz.oop2.Repository;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

import at.tugraz.oop2.AmenityParam;
import at.tugraz.oop2.RoadParam;
import org.locationtech.jts.geom.*;

@Getter
@Setter
public class Repository {
    public static GeometryFactory factory = new GeometryFactory();

    ArrayList<GeoFeature> entries = new ArrayList<>();
    Paging paging = new Paging();

    public void addFeature(GeoFeature feature) {
        entries.add(feature);
    }

    public Repository getMatches(AmenityParam parameter) {
        Polygon bbox = createBoundingBox(
                parameter.getBboxTlX(), parameter.getBboxTlY(),
                parameter.getBboxBrX(), parameter.getBboxBrY());

        Point point = createPoint(
                parameter.getPointX(), parameter.getPointY());
        double range = parameter.getPointD();

        ArrayList<GeoFeature> temp = entries.stream()
                .filter(e -> parameter.getAmenity().equals("") 
                        || e.getType().equals(parameter.getAmenity()))
                .filter(e -> e.isInsideBbox(bbox) 
                        && (e.isInsideCircle(point, range) || isBoundingBoxValid(bbox))) 
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        Repository result = new Repository();
        result.setEntries(temp);
        result.page(parameter.getTake(), parameter.getSkip());

        return result;
    }

    public Repository getMatches(RoadParam parameter) {
        Polygon bbox = createBoundingBox(
                parameter.getBboxTlX(), parameter.getBboxTlY(),
                parameter.getBboxBrX(), parameter.getBboxBrY());

        ArrayList<GeoFeature> temp = entries.stream()
                .filter(e -> parameter.getRoad().equals("") 
                        || e.getType().equals(parameter.getRoad()))
                .filter(e -> e.isInsideBbox(bbox))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        Repository result = new Repository();
        result.setEntries(temp);
        result.page(parameter.getTake(), parameter.getSkip());

        return result;
    }

    public void page(long take, long skip) {
        paging.setTake(take);
        paging.setSkip(skip);
        paging.setTotal(entries.size());

        skip = Math.min(skip, entries.size());
        take = Math.min(take, entries.size() - skip);

        entries = new ArrayList<>(entries.subList(
                    (int) skip, (int) (skip + take)));
    }

    public GeoFeature byId(long id) {
        int left = 0;
        int right = entries.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (entries.get(mid).getId() == id) {
                return entries.get(mid);
            }

            if (entries.get(mid).getId() < id) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return null;
    }

    private Polygon createBoundingBox(double tlx, double tly,
            double brx, double bry) {
        return factory.createPolygon(new Coordinate[] {
            new Coordinate(tlx, tly),
            new Coordinate(tlx, bry),
            new Coordinate(brx, bry),
            new Coordinate(brx, tly),
            new Coordinate(tlx, tly)
        });
    }

    private boolean isBoundingBoxValid(Polygon bbox) {
        Polygon maxBbox = createBoundingBox(180.0, 90.0, -180.0, -90.0);

        return bbox.within(maxBbox);
    }

    private Point createPoint(double x, double y) {
        Point p = factory.createPoint(new Coordinate(x, y));
        return (Point) CoordinateTransform.transform(p);
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

    public void sort() {
        entries.sort((a, b) -> {
            if (a.getId() < b.getId()) {
                return -1;
            } else if (a.getId() > b.getId()) {
                return 1;
            } else {
                return 0;
            }
        });
    }

    public void fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Repository temp = mapper.readValue(json, Repository.class);

            entries = temp.entries;
            paging = temp.paging;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
