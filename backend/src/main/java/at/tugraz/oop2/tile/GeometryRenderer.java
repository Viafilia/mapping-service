package at.tugraz.oop2.tile;

import org.apache.commons.lang3.tuple.Pair;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import java.util.Map;
import java.util.HashMap;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.GeometryCollection;

@Getter
@AllArgsConstructor
class Tile {
    private int x, y, z;
}

@Data
public class GeometryRenderer {
    private static final GeometryFactory factory = new GeometryFactory();
    private static final Map<String, Pair<Color, Integer>> map = new HashMap<>();
    static {
        map.put("background" , Pair.of(new  Color(255, 255, 255), 1));
        map.put("motorway" , Pair.of(new  Color(255, 0, 0), 3));
        map.put("trunk" , Pair.of(new  Color(255, 140, 0), 2));
        map.put("primary" , Pair.of(new  Color(255, 165, 0), 2));
        map.put("secondary" , Pair.of(new  Color(255, 255, 0), 2));
        map.put("road" , Pair.of(new  Color(128, 128, 128), 2));
        map.put("forest" , Pair.of(new  Color(173, 209, 158), 1));
        map.put("residential", Pair.of(new  Color(223, 233, 233), 1));
        map.put("vineyard" , Pair.of(new  Color(172, 224, 161), 1));
        map.put("grass" , Pair.of(new  Color(205, 235, 176), 1));
        map.put("railway" , Pair.of(new  Color(235, 219, 233), 1));
        map.put("water" , Pair.of(new  Color(0, 128, 255), 1));
    }

    private Graphics2D g2d;
    private Polygon bbox;
    private Tile currentTile;
    private int width, height;

    public GeometryRenderer(BufferedImage image, int z, int x, int y) {
        currentTile = new Tile(x, y, z);

        width = image.getWidth();
        height = image.getHeight();

        g2d = createGraphics(image);
        bbox = createBoundingBox(z, x, y);
    }

    public void drawGeometry(Geometry geometry) {
        if (geometry == null || !bbox.intersects(geometry))
            return;

        if (geometry instanceof Polygon)
            drawPolygon((Polygon) geometry);
        else if (geometry instanceof LineString)
            drawLineString((LineString) geometry);
        else if (geometry instanceof MultiPolygon)
            drawMultiPolygon((MultiPolygon) geometry);
        else if (geometry instanceof GeometryCollection)
            drawGeometryCollection((GeometryCollection) geometry);
    }
    
    private void drawGeometryCollection(GeometryCollection geometryCollection) {
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            drawGeometry(geometryCollection.getGeometryN(i));
        }
    }

    private void drawMultiPolygon(MultiPolygon multiPolygon) {
        Polygon outerPolygon = (Polygon) multiPolygon.getGeometryN(0);
        Area area = new Area(pathFromCoords(outerPolygon.getCoordinates()));

        for (int i = 1; i < multiPolygon.getNumGeometries(); i++) {
            Polygon innerPolygon = (Polygon) multiPolygon.getGeometryN(i);
            area.subtract(new Area(pathFromCoords(innerPolygon.getCoordinates())));
        }

        g2d.fill(area);
    }

    private Path2D pathFromCoords(Coordinate[] coordinates) {
        Path2D path = new Path2D.Double();

        for (int i = 0; i < coordinates.length; i++) {
            double[] pixelCoord = coordToPixelCoord(coordinates[i]);
            if (i == 0)
                path.moveTo(pixelCoord[0], pixelCoord[1]);
            else
                path.lineTo(pixelCoord[0], pixelCoord[1]);
        }

        return path;
    }

    private void drawLineString(LineString lineString) {
        Path2D path = pathFromCoords(lineString.getCoordinates());
        g2d.draw(path);
    }

    private void drawPolygon(Polygon polygon) {
        Path2D path = pathFromCoords(polygon.getCoordinates());
        g2d.fill(path);
    }

    private double[] coordToPixelCoord(Coordinate c) {
        double lon4326 = Math.toRadians(c.x);
        double lat4326 = Math.toRadians(c.y);

        double x3857 = lon4326;
        double y3857 = Math.log(Math.tan(lat4326) 
                + 1.0 / Math.cos(lat4326));

        double x = 0.5 + x3857 / (2 * Math.PI);
        double y = 0.5 - y3857 / (2 * Math.PI);

        double N = Math.pow(2, currentTile.getZ());
        double xTile = x * N;
        double yTile = y * N;
        
        double xPixel = width * (xTile - currentTile.getX());
        double yPixel = height * (yTile - currentTile.getY());

        return new double[] {Math.floor(xPixel), Math.floor(yPixel)};
    }

    private Polygon createBoundingBox(int z, int x, int y) {
        Coordinate tl = tileToCoordinate(z, x, y);
        Coordinate br = tileToCoordinate(z, x + 1, y + 1);

        return factory.createPolygon(new Coordinate[] {
            new Coordinate(tl.x, tl.y),
            new Coordinate(tl.x, br.y),
            new Coordinate(br.x, br.y),
            new Coordinate(br.x, tl.y),
            new Coordinate(tl.x, tl.y)
        });
    }

    private Coordinate tileToCoordinate(int z, int x, int y) {
        double p = Math.pow(2, z);
        double longitude = x * 360.0 / p - 180;
        double latitude = Math.atan(Math.sinh(Math.PI - 2 * Math.PI * y / p)) * 180 / Math.PI;

        return new Coordinate(longitude, latitude);
    }

    private Graphics2D createGraphics(BufferedImage image) {
        Graphics2D g2d = image.createGraphics();

        g2d.setRenderingHints(Map.of(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON, 
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON));

        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);

        g2d.setClip(0, 0, 512, 512);

        return g2d;
    }

    public void drawBackground() {
        setLayer("background");
        g2d.fillRect(0, 0, 512, 512);
    }

    public void setLayer(String layer) {
        Color color = map.get(layer).getLeft();
        g2d.setColor(color);

        int width = map.get(layer).getRight();
        g2d.setStroke(new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
    }
}
