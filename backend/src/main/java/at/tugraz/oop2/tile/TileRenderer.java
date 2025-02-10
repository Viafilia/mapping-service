package at.tugraz.oop2.tile;

import at.tugraz.oop2.Repository.Repository;
import at.tugraz.oop2.Repository.GeoFeature;

import java.awt.image.BufferedImage;

import java.util.Map;
import java.util.HashMap;

import at.tugraz.oop2.TileParam;

public class TileRenderer {
    private final Map<String, Repository> repositoryMap = new HashMap<>();

    public TileRenderer(Repository roads, 
            Repository landUsageRepository, Repository natureRepository) {

        repositoryMap.put("background", null);
        repositoryMap.put("motorway", roads);
        repositoryMap.put("trunk", roads);
        repositoryMap.put("primary", roads);
        repositoryMap.put("secondary", roads);
        repositoryMap.put("road", roads);
        repositoryMap.put("forest", landUsageRepository);
        repositoryMap.put("residential", landUsageRepository);
        repositoryMap.put("vineyard", landUsageRepository);
        repositoryMap.put("grass", landUsageRepository);
        repositoryMap.put("railway", landUsageRepository);
        repositoryMap.put("water", natureRepository);
    }

    public BufferedImage renderTile(TileParam param) {
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);

        GeometryRenderer renderer = new GeometryRenderer(image, 
                param.getZ(), param.getX(), param.getY());

        renderer.drawBackground();
        for (String layer : param.getLayersList()) {
            if (!repositoryMap.containsKey(layer))
                continue;

            renderer.setLayer(layer);

            Repository r = repositoryMap.get(layer);
            for (GeoFeature feature : r.getEntries()) {
                if (feature.getType().equals(layer) || layer.equals("road")) {
                    renderer.drawGeometry(feature.getGeom());
                }
            }
        }

        return image;
    }
}
