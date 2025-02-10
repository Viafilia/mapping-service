package at.tugraz.oop2.Parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BoundingBoxParameter {
    @Data
    class Point {
        Double x, y;
    }

    Point tl = new Point();
    Point br = new Point();

    public BoundingBoxParameter(double x1, double y1, double x2, double y2) {
        tl.x = x1;
        tl.y = y1;
        br.x = x2;
        br.y = y2;
    }
    
    public boolean isValid() {
        return tl != null && br != null &&
                tl.x != null && tl.y != null && br.x != null && br.y != null;
    }

    public boolean checkBounds() {
        return tl.x >= -180 && tl.x <= 180 && tl.y >= -90 && tl.y <= 90
                && br.x >= -180 && br.x <= 180 && br.y >= -90 && br.y <= 90
                && tl.x <= br.x && tl.y >= br.y;
    }

    public double getTlX() {
        return tl.x;
    }
    public double getTlY() {
        return tl.y;
    }
    public double getBrX() {
        return br.x;
    }
    public double getBrY() {
        return br.y;
    }
}
