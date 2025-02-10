package at.tugraz.oop2;


import at.tugraz.oop2.Route.Route;
import io.grpc.stub.StreamObserver;
import at.tugraz.oop2.Repository.Repository;

import at.tugraz.oop2.Repository.GeoFeature;

import at.tugraz.oop2.LandUsage.LandUsage;
import org.geotools.graph.path.Path;
import org.springframework.util.SerializationUtils;


import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import com.google.protobuf.ByteString;
import java.io.File;

import at.tugraz.oop2.tile.TileRenderer;

public class BackendServerImpl extends RequestServiceGrpc.RequestServiceImplBase{
    Repository amenties;
    Repository roads;
    Repository landUsageRepository;
    Repository natureRepository;
    private RouteFinder finder;

    public BackendServerImpl(String osmfile)
    {
        amenties = null;
        roads = null;
        landUsageRepository = null;
        finder = null;
        try {
            LoadOsm osm = new LoadOsm(osmfile);
            amenties = osm.createRepository("amenity");
            roads = osm.createRepository("highway");
            landUsageRepository = osm.createRepository("landuse");
            natureRepository = osm.createRepository("natural");

            osm.finish();
            finder = new RouteFinder(roads);
        } catch (Exception e) {
            System.out.println("Error: could not load OSM file ( " + osmfile + " )");
            e.printStackTrace();
        }
    }

    @Override
    public void amenities(AmenityParam request, StreamObserver<Response> responseObserver)
    {
        MapLogger.backendLogAmenitiesRequest();

        Repository result = amenties.getMatches(request);

        Response r = Response.newBuilder().setMessage(result.toJson()).build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }
    @Override
    public void roads(RoadParam request, StreamObserver<Response> responseObserver)
    {
        MapLogger.backendLogRoadsRequest();

        Repository result = roads.getMatches(request);

        Response r = Response.newBuilder().setMessage(result.toJson()).build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }
    @Override
    public void amenityById(ById request, StreamObserver<Response> responseObserver) {

        MapLogger.backendLogAmenityRequest((int)request.getId());

        GeoFeature feature = amenties.byId(request.getId());

        int error = 0;
        String message = "";

        if (feature == null)
            error = 404;
        else
            message = feature.toJson();

        Response r = Response.newBuilder().setMessage(message).setError(error).build();

        responseObserver.onNext(r);
        responseObserver.onCompleted();

    }
    @Override
    public void roadById(ById request, StreamObserver<Response> responseObserver)
    {
        MapLogger.backendLogRoadRequest((int)request.getId());

        GeoFeature feature = roads.byId(request.getId());

        int error = 0;
        String message = "";
        if (feature == null)
            error = 404;
        else
            message = feature.toJson();

        Response r = Response.newBuilder().setMessage(message).setError(error).build();
        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    @Override
    public void renderTile(TileParam request, StreamObserver<TileResponse> responseObserver)
    {
        TileResponse r = null;
        try {
            BufferedImage image = new TileRenderer(roads, landUsageRepository,
                    natureRepository).renderTile(request);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            r = TileResponse.newBuilder().setImageData(ByteString.copyFrom(imageBytes)).build();
        } catch (Exception e) {
            r = TileResponse.newBuilder().setError(500).build();
            e.printStackTrace();
        }

        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    @Override
    public void landUsage(LandUsageParameterProto request, StreamObserver<LandUsageProto> responseObserver)
    {
        LandUsage landUsage = new LandUsage(request, landUsageRepository);

        LandUsageProto r = landUsage.toProto();

        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }

    @Override
    public void route(RouteParameterProto request, StreamObserver<RouteProto> responseObserver)
    {

        int error = finder.setParameters(request.getFrom(), request.getTo(), request.getWeighting());

        Path path = finder.findPath();

        RouteProto r = Route.fromPath(path, error);

        responseObserver.onNext(r);
        responseObserver.onCompleted();
    }
}
