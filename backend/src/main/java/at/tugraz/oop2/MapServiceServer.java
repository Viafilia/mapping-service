package at.tugraz.oop2;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.logging.Logger;

import org.opengis.referencing.FactoryException;

import at.tugraz.oop2.Repository.CoordinateTransform;

public class MapServiceServer {

    private static final Logger logger = Logger.getLogger(MapServiceServer.class.getName());

    private static final Integer JMAP_BACKEND_PORT = 8020;
    // private static final String JMAP_BACKEND_OSMFILE = "data/graz_tiny_reduced.osm";
    private static final String JMAP_BACKEND_OSMFILE = "data/styria_reduced.osm";

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, FactoryException {
        logger.info("Starting backend...");

        int jmap_backend_port = parseJmapBackendPort();
        String jmap_backend_osmfile = parseJmapBackendOsmfile();
        MapLogger.backendStartup(jmap_backend_port, jmap_backend_osmfile);

        CoordinateTransform.initTransform("EPSG:4326", "EPSG:31256");

        BackendServer server = new BackendServer(jmap_backend_port, jmap_backend_osmfile);
        server.run();
    }

    private static Integer parseJmapBackendPort() {
        try {
            Integer port = Integer.valueOf(
                    System.getenv().getOrDefault("JMAP_BACKEND_PORT",
                            JMAP_BACKEND_PORT.toString()));

            return port >= 0 && port <= 65535 ? port
                    : JMAP_BACKEND_PORT;
        } catch (NumberFormatException e) {
            return JMAP_BACKEND_PORT;
        }
    }

    private static String parseJmapBackendOsmfile() {
        return System.getenv().getOrDefault("JMAP_BACKEND_OSMFILE",
                JMAP_BACKEND_OSMFILE);
    }
}
