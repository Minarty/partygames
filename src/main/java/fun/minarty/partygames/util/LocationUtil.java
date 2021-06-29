package fun.minarty.partygames.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class LocationUtil {

    private static final String LOCATION_DELIMITER = ":";
    private static final String AREA_DELIMITER = ";";

    public String serializeArea(Cuboid area){
        return serializeLocation(area.getHigh(), false) + AREA_DELIMITER
                + serializeLocation(area.getLow(), false);
    }

    public Cuboid deserializeArea(World world, String serialized){
        String[] locations = serialized.split(AREA_DELIMITER);
        return new DefaultCuboid(deserializeLocation(world, locations[0]), deserializeLocation(world, locations[1]));
    }

    public String serializeLocation(Location location, boolean angle){
        StringBuilder builder = new StringBuilder();
        builder.append(location.getX()).append(LOCATION_DELIMITER);
        builder.append(location.getY()).append(LOCATION_DELIMITER);
        builder.append(location.getZ());

        if(angle){
            builder.append(LOCATION_DELIMITER)
                    .append(location.getYaw())
                    .append(LOCATION_DELIMITER);
            builder.append(location.getPitch());
        }

        return builder.toString();
    }

    public Location deserializeLocation(World world, String serialized){
        String[] array = serialized.split(LOCATION_DELIMITER);
        double x = Double.parseDouble(array[0]);
        double y = Double.parseDouble(array[1]);
        double z = Double.parseDouble(array[2]);

        if(array.length == 3){
            return new Location(world, x, y, z);
        } else if(array.length == 5){
            float yaw = Float.parseFloat(array[3]);
            float pitch = Float.parseFloat(array[4]);

            return new Location(world, x, y, z, yaw, pitch);
        }

        return null;
    }

    public List<Location> deserializeLocations(World world, List<String> serializedLocations){
        return serializedLocations.stream()
                .map(s -> deserializeLocation(world, s))
                .collect(Collectors.toList());
    }

}