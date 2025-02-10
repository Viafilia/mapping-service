package at.tugraz.oop2;

import java.util.Collections;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MapApplication {
    private static final Integer JMAP_MIDDLEWARE_PORT_DEFAULT = 8010;
    private static final String JMAP_BACKEND_TARGET_DEFAULT = "localhost:8020";

    public static void main(String[] args) {
        int jmap_middleware_port = parseJmapMiddlewarePort();
        String jmap_backend_target = parseJmapBackendTarget();

        MapLogger.middlewareStartup(jmap_middleware_port, jmap_backend_target);
        ClientServer.setPort(jmap_backend_target);

        var middleware = new SpringApplication(MapApplication.class);
        middleware.setDefaultProperties(Collections.singletonMap(
                    "server.port", jmap_middleware_port));
        middleware.run(args);
    }

    private static Integer parseJmapMiddlewarePort() {
        try {
            int port = Integer.parseInt(
                    System.getenv().getOrDefault("JMAP_MIDDLEWARE_PORT",
                            JMAP_MIDDLEWARE_PORT_DEFAULT.toString()));

            return port >= 0 && port <= 65535 ? port
                    : JMAP_MIDDLEWARE_PORT_DEFAULT;
        } catch (NumberFormatException e) {
            return JMAP_MIDDLEWARE_PORT_DEFAULT;
        }
    }

    private static String parseJmapBackendTarget() {
        return System.getenv().getOrDefault("JMAP_BACKEND_TARGET",
                JMAP_BACKEND_TARGET_DEFAULT);
    }
}
