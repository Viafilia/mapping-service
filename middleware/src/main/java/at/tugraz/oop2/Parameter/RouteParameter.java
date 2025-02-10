package at.tugraz.oop2.Parameter;

import lombok.Data;

@Data
public class RouteParameter {
    Long from;
    Long to;
    String weighting = "length";

    public void validate() {
        if (from == null || to == null) {
            throw new IllegalArgumentException("missing route parameters");
        }

        if (!weighting.equals("length") && !weighting.equals("time")) {
            weighting = "length";
            // maybe needs to be changed to an exception
        }

    }
}
