package at.tugraz.oop2.Parameter;

import lombok.Data;

@Data
public class RoadParameter {
    String road="";
    BoundingBoxParameter bbox;
    long take=50;
    long skip=0;

    public void validate() {
        if (take < 0 || skip < 0) {
            throw new IllegalArgumentException("take and skip must be positive");
        }

        if (bbox == null || !bbox.isValid()) {
            throw new IllegalArgumentException("bounding box must be provided");
        }

        if (!bbox.checkBounds()) {
            throw new IllegalArgumentException("bounding box out of bounds or left top corner is not left top");
        }
    }
}
