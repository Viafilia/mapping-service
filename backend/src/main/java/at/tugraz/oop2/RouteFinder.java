package at.tugraz.oop2;

import at.tugraz.oop2.Repository.GeoFeature;
import at.tugraz.oop2.Repository.Repository;
import at.tugraz.oop2.Route.EdgeData;
import lombok.Data;
import org.geotools.graph.build.basic.BasicDirectedGraphBuilder;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.standard.DijkstraIterator;

import java.util.*;

@Data
public class RouteFinder {
    private Graph graph;
    HashMap<Long, Node> nodes;
    List<Edge> edges;
    private Node start;
    private Node end;
    private String weighting;


    DijkstraIterator.EdgeWeighter lengthWeighter = new DijkstraIterator.EdgeWeighter() {
        @Override
        public double getWeight(Edge e) {
            EdgeData object = (EdgeData) e.getObject();
            return object.getLength();
        }
    };

    DijkstraIterator.EdgeWeighter timeWeighter = new DijkstraIterator.EdgeWeighter() {
        @Override
        public double getWeight(Edge e) {
            EdgeData object = (EdgeData) e.getObject();
            return object.getTime();
        }
    };

    public RouteFinder(Repository repository) {

        nodes = new HashMap<>();
        edges = new ArrayList<>();
        long temp1 = 20929584L;
        long temp2 = 6137492613L;
        ArrayList<GeoFeature> edges_list = repository.getEntries();
        BasicDirectedGraphBuilder graphBuilder = new BasicDirectedGraphBuilder();

        for (GeoFeature feature : edges_list) {
            if (feature.getChild_ids().length > 0) {
                long prev_id = feature.getChild_ids()[0];
                long child_id = feature.getChild_ids()[feature.getChild_ids().length - 1];
                Node new_node = graphBuilder.buildNode();
                new_node.setObject(child_id);
                nodes.put(child_id, new_node);
                graphBuilder.addNode(new_node);
                new_node = graphBuilder.buildNode();
                new_node.setObject(prev_id);
                nodes.put(prev_id, new_node);
                graphBuilder.addNode(new_node);
            }

        }

        for (GeoFeature feature : edges_list) {
            double speed = feature.countSpeed();
            if (feature.getChild_ids().length > 0 && feature.getGeom() != null) {
                long prev_id = feature.getChild_ids()[0];
                long child_id = feature.getChild_ids()[feature.getChild_ids().length - 1];
                if (nodes.containsKey(child_id) && nodes.containsKey(prev_id)) {
                    Edge new_edge = graphBuilder.buildEdge(nodes.get(prev_id), nodes.get(child_id));
                    EdgeData data = new EdgeData(speed, feature);
                    new_edge.setObject(data);
                    edges.add(new_edge);
                    graphBuilder.addEdge(new_edge);
                    new_edge = graphBuilder.buildEdge(nodes.get(child_id), nodes.get(prev_id));
                    new_edge.setObject(data);
                    edges.add(new_edge);
                    graphBuilder.addEdge(new_edge);
                }

            }
        }

        graph = graphBuilder.getGraph();

    }



    public int setParameters(long start_id, long end_id, String weigthing_param)
    {
        start = nodes.get(start_id);
        end = nodes.get(end_id);
        weighting = weigthing_param;

        if (start == null || end == null)
        {
            return 404;
        }
        return 0;
    }




    public Path findPath() {
        if (start == null || end == null)
        {
            return null;
        }
        DijkstraShortestPathFinder pf;
        if (weighting.equals("time"))
            pf = new DijkstraShortestPathFinder( graph, start, timeWeighter );
        else
            pf = new DijkstraShortestPathFinder( graph, start, lengthWeighter );
        pf.calculate();
        Path path = pf.getPath(end);
        //printPath(path);
        return path;
    }

    public static void printPath(Path path) {
        if (path == null || path.isEmpty()) {
            System.out.println("No path found.");
            return;
        }

        List<Node> nodes = new ArrayList<>(path); // Collect nodes in the path

        for (int i = nodes.size() - 1; i >= 0; i--) {
            System.out.print(nodes.get(i).getObject() + " -> ");
        }
        System.out.println("End");
    }

}
