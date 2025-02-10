package at.tugraz.oop2.Parameter;

import lombok.Data;
import java.util.List;

@Data
public class ImageParameter {
    private int z;
    private int x;
    private int y;
    private List<String> layers = List.of("motorway");

    public void validate() {
        if (z < 0 || x < 0 || y < 0) {
            throw new IllegalArgumentException("z, x, and y must be non-negative");
        }

        if (layers == null || layers.isEmpty()) {
            throw new IllegalArgumentException("layers must not be null or empty");
        }
    }
}
