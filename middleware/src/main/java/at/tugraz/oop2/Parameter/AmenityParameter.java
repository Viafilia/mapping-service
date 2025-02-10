package at.tugraz.oop2.Parameter;

import lombok.Data;

@Data
public class AmenityParameter {
    String amenity="";
    BoundingBoxParameter bbox;
    PointParameter point;
    long take=50;
    long skip=0;
    
    public void validate() {
        if (take < 0 || skip < 0) {
            throw new IllegalArgumentException("take and skip must be positive");
        }
        
        boolean bbox_valid = false, point_valid = false;
        if ((bbox == null || !(bbox_valid = bbox.isValid())) 
                && (point == null || !(point_valid = point.isValid()))) {
            throw new IllegalArgumentException("either bbox or point must be provided");
        }

        if (bbox_valid && !bbox.checkBounds()) {
            throw new IllegalArgumentException("bounding box out of bounds or left top corner is not left top");
        }

        if (point_valid && !point.checkBounds()) {
            throw new IllegalArgumentException("point out of bounds");
        }

        if (point == null)
            point = new PointParameter(0.0, 0.0, 999999999.0);

        if (bbox == null)
            bbox = new BoundingBoxParameter(-10000.0, -10000.0, 10000.0, 10000.0);
    }
}
