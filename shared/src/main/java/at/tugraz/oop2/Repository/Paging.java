package at.tugraz.oop2.Repository;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Paging {
    private long skip = 0;
    private long take = 0;
    private long total = 0;
}
