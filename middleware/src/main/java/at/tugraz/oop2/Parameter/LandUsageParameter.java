package at.tugraz.oop2.Parameter;

import lombok.Data;

@Data
public class LandUsageParameter {
    BoundingBoxParameter bbox;

    public void validate() {
        if (bbox == null || !bbox.isValid() || !bbox.checkBounds()) {
            throw new IllegalArgumentException(
                    "bounding box must be provided and valid");
        }
    }
}
