package at.tugraz.oop2.Route;

import at.tugraz.oop2.Repository.CoordinateTransform;
import at.tugraz.oop2.Repository.GeoFeature;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

@Getter
public class EdgeData {
    private final Geometry geometry;
    private final double length;
    private final double time;
    private GeoFeature road;

    public EdgeData(Geometry geometry, double length, double time) {
        this.geometry = geometry;
        this.length = length;
        this.time = time;
    }

    public EdgeData(double speed, GeoFeature road) {
        geometry = CoordinateTransform.transform(road.getGeom());

        length = (geometry.getLength());
        time = (geometry.getLength() / speed) * 0.06;
        this.road = road;

    }
}