package at.tugraz.oop2.Parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointParameter {
    Double x, y, d;
    
    public boolean isValid() {
        return x != null && y != null && d != null;
    }
    
    public boolean checkBounds() {
        return x >= -180 && x <= 180 && y >= -90 && y <= 90 && d >= 0;
    }
}
