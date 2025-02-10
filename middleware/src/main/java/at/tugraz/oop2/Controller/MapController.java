package at.tugraz.oop2.Controller;

import at.tugraz.oop2.Parameter.*;
import at.tugraz.oop2.Route.Route;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import at.tugraz.oop2.Repository.Repository;
import at.tugraz.oop2.Repository.GeoFeature;

import at.tugraz.oop2.ClientServer;

import at.tugraz.oop2.LandUsage.LandUsage;

@RestController
public class MapController {
    @GetMapping("/amenities")
    Repository getAmenities(AmenityParameter parameter) {
        parameter.validate();

        return ClientServer.amenities(parameter);
    }
    
    @GetMapping("/amenities/{id}")
    GeoFeature getAmenity(@PathVariable long id) {
        return ClientServer.amenityById(id);
    }

    @GetMapping("/roads")
    Repository getRoads(RoadParameter parameter) {
        parameter.validate();
        
        return ClientServer.roads(parameter);
    }

    @GetMapping("/roads/{id}")
    GeoFeature getRoad(@PathVariable long id) {
        return ClientServer.roadById(id);
    }

    @GetMapping("/tile/{z}/{x}/{y}.png")
    public void getImage(ImageParameter parameter, HttpServletResponse http_response) {
        parameter.validate();

        ClientServer.renderTile(parameter, http_response);
    }

    @GetMapping("/usage")
    public LandUsage getLandUsage(LandUsageParameter parameter) {
        parameter.validate();

        return ClientServer.landUsage(parameter);
    }

    @GetMapping("/route")
    public Route getRoute(RouteParameter parameter) {
        parameter.validate();

        return ClientServer.route(parameter);
    }
};
