package at.tugraz.oop2.Repository;

import org.geotools.referencing.CRS;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.geotools.geometry.jts.JTS;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Envelope;

public class CoordinateTransform {
    private static MathTransform transform = null;

    public static void initTransform(String source, String target) throws FactoryException {
        CoordinateReferenceSystem sourceCRS = CRS.decode(source, true);
        CoordinateReferenceSystem targetCRS = CRS.decode(target, true);

        transform = CRS.findMathTransform(sourceCRS, targetCRS, true);
    }

    public static Geometry transform(Geometry geom) {
        try {
            return JTS.transform(geom, transform);
        } catch (MismatchedDimensionException | TransformException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }


    public static Envelope transform(Envelope env) {
        try {
            return JTS.transform(env, transform);
        } catch (MismatchedDimensionException | TransformException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}

