package at.tugraz.oop2.Route;

import at.tugraz.oop2.*;
import at.tugraz.oop2.Repository.GeoFeature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    double time;
    double length;
    ArrayList<GeoFeature> roads;

    public static Route fromProto(RouteProto routeProto) {
        Route answer = new Route();
        answer.time = routeProto.getTime();
        answer.length = routeProto.getLength();
        answer.roads = new ArrayList<>();
        for (int i= 0; i < routeProto.getRoadsCount(); i++) {
            String road = routeProto.getRoads(i);
            GeoFeature entry = new GeoFeature();
            entry.fromJson(road);
            answer.roads.add(entry);
        }
        return answer;
    }

    public static RouteProto fromPath(Path path, int error) {
        RouteProto.Builder builder = RouteProto.newBuilder();
        if (error != 0)
        {
            builder.setError(error);
            return builder.build();
        }
        if (path == null || path.isEmpty())
        {
            builder.setError(400);
            return builder.build();
        }

        builder.setError(0);
        List<GeoFeature> roads_answer = new ArrayList<>();
        List<Edge> edges = path.getEdges();
        double time_answer = 0;
        double length_answer = 0;
        for (Edge edge : edges) {
            roads_answer.add(((EdgeData) edge.getObject()).getRoad());
            time_answer += ((EdgeData) edge.getObject()).getTime();
            length_answer += ((EdgeData) edge.getObject()).getLength();
        }



        for (GeoFeature entry : roads_answer) {
            builder.addRoads(entry.toJson());
        }
        builder.setLength(length_answer);
        builder.setTime(time_answer);

        return builder.build();
    }
}
