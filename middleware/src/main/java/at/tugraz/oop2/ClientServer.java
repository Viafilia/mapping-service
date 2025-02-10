package at.tugraz.oop2;

import at.tugraz.oop2.Parameter.*;
import at.tugraz.oop2.Route.Route;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import at.tugraz.oop2.Repository.Repository;
import at.tugraz.oop2.Repository.GeoFeature;
import jakarta.servlet.http.HttpServletResponse;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.http.HttpStatus;
import org.springframework.util.RouteMatcher;
import org.springframework.web.client.HttpServerErrorException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;
import java.io.IOException;

import at.tugraz.oop2.LandUsage.LandUsage;

public class ClientServer {
    private static ManagedChannel channel;
    private static RequestServiceGrpc.RequestServiceBlockingStub stub;
    private static final Logger logger = Logger.getLogger(ClientServer.class.getName());

    public static void setPort(String backendTarget) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget(backendTarget)
                .usePlaintext()
                .build();

        stub = RequestServiceGrpc.newBlockingStub(channel);
    }
    public static void shutdown()
    {
        channel.shutdown();
    }
    public static Repository amenities(AmenityParameter parameter) {
        try {
            AmenityParam request = AmenityParam.newBuilder()
                    .setAmenity(parameter.getAmenity())
                    .setBboxTlX(parameter.getBbox().getTlX())
                    .setBboxTlY(parameter.getBbox().getTlY())
                    .setBboxBrX(parameter.getBbox().getBrX())
                    .setBboxBrY(parameter.getBbox().getBrY())
                    .setPointX(parameter.getPoint().getX())
                    .setPointY(parameter.getPoint().getY())
                    .setPointD(parameter.getPoint().getD())
                    .setTake(parameter.getTake())
                    .setSkip(parameter.getSkip())
                    .build();

            Response response = stub.amenities(request);
            Repository amenities = new Repository();
            amenities.fromJson(response.getMessage());

            return amenities;
        } catch (Exception e) {
            logger.severe("Failed to fetch amenities: " + e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to reach backend service.");
        }
    }
    public static Repository roads(RoadParameter parameter) {
        try {
            RoadParam request = RoadParam.newBuilder()
                    .setRoad(parameter.getRoad())
                    .setBboxTlX(parameter.getBbox().getTlX())
                    .setBboxTlY(parameter.getBbox().getTlY())
                    .setBboxBrX(parameter.getBbox().getBrX())
                    .setBboxBrY(parameter.getBbox().getBrY())
                    .setTake(parameter.getTake())
                    .setSkip(parameter.getSkip())
                    .build();

            Response response = stub.roads(request);

            Repository roads = new Repository();
            roads.fromJson(response.getMessage());

            return roads;
        } catch (Exception e) {
            logger.severe("Failed to fetch roads: " + e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to reach backend service.");
        }
    }
    public static GeoFeature amenityById(long id) {
        try {
            ById request = ById.newBuilder()
                    .setId(id)
                    .build();

            Response response = stub.amenityById(request);

            if (response.getError() == 404)
                throw new NoSuchElementException("Amenity with ID " + id + " not found.");

            GeoFeature feature = new GeoFeature();
            feature.fromJson(response.getMessage());

            return feature;
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to fetch amenity by ID: " + e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to reach backend service.");
        }
    }

    public static GeoFeature roadById(long id) {
        try {
            ById request = ById.newBuilder()
                    .setId(id)
                    .build();

            Response response = stub.roadById(request);

            if (response.getError() == 404)
                throw new NoSuchElementException("Road with ID " + id + " not found.");

            GeoFeature feature = new GeoFeature();
            feature.fromJson(response.getMessage());

            return feature;
        } catch (NoSuchElementException e) {
            throw e;
        } catch (Exception e) {
            logger.severe("Failed to fetch road by ID: " + e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to reach backend service.");
        }
    }

    public static void renderTile(ImageParameter parameter, HttpServletResponse http_response) {
        TileParam request = TileParam.newBuilder()
            .setZ(parameter.getZ())
            .setX(parameter.getX())
            .setY(parameter.getY())
            .addAllLayers(parameter.getLayers())
            .build();

        TileResponse response = stub.renderTile(request);

        if (response.getError() != 0)
            throw new HttpServerErrorException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Unable to render tile.");

        byte[] image_data = response.getImageData().toByteArray();

        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(image_data));
            http_response.setContentType("image/png");
            OutputStream out = http_response.getOutputStream();
            ImageIO.write(image, "PNG", out);
            out.close();

        } catch (Exception e) {
            logger.severe("Server send invalid image: " + e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to render tile.");
        }
    }

    public static LandUsage landUsage(LandUsageParameter parameter) {
        try {
            LandUsageParameterProto request = LandUsageParameterProto.newBuilder()
                    .setBboxTlX(parameter.getBbox().getTlX())
                    .setBboxTlY(parameter.getBbox().getTlY())
                    .setBboxBrX(parameter.getBbox().getBrX())
                    .setBboxBrY(parameter.getBbox().getBrY())
                    .build();

            LandUsageProto response = stub.landUsage(request);

            return LandUsage.fromProto(response);

        } catch (Exception e) {
            logger.severe("Failed to fetch land usage: " + e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, 
                    "Unable to reach backend service.");
        }
    }

    public static Route route(RouteParameter parameter) {
        try {
            RouteParameterProto request = RouteParameterProto.newBuilder()
                    .setFrom(parameter.getFrom())
                    .setTo(parameter.getTo())
                    .setWeighting(parameter.getWeighting())
                    .build();
            RouteProto response = stub.route(request);
            if (response.getError() == 404)
            {
                throw new NoSuchElementException("One of the nodes not present in the graph");
            }
            if (response.getError() == 400)
            {
                throw new IllegalArgumentException("No path found");
            }
            return Route.fromProto(response);
        }
        catch (NoSuchElementException | IllegalArgumentException e) {
            throw e;
        }
        catch (Exception e)
        {
            logger.severe("Failed to fetch route: " + e.getMessage());
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to reach backend service.");
        }
    }
}
