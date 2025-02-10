package at.tugraz.oop2;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class BackendServer {
    Server server;
    int port;

    public BackendServer(int port, String osmfile) {
        this.port = port;

        server = ServerBuilder
            .forPort(port)
            .addService(new BackendServerImpl(osmfile))
            .build();
    }

    public void run() {
        try {
            server.start();
            server.awaitTermination();
        } catch (Exception e) {
            System.out.println("Error: could not start server");
        }
    }
}
