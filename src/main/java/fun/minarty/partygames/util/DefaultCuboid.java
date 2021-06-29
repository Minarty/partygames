package fun.minarty.partygames.util;

import fun.minarty.partygames.util.jackson.LocationDeserializer;
import fun.minarty.partygames.util.jackson.LocationSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        creatorVisibility = JsonAutoDetect.Visibility.NONE
)
public final class DefaultCuboid implements Cuboid, Iterable<Block> {

    private final int x1, z1, x2, z2;
    private int y1;

    private int y2;
    private final String worldName;

    @JsonDeserialize(using = LocationDeserializer.class)
    @JsonSerialize(using = LocationSerializer.class)
    @JsonProperty
    @Getter
    private final Location high;

    @JsonDeserialize(using = LocationDeserializer.class)
    @JsonSerialize(using = LocationSerializer.class)
    @JsonProperty
    @Getter
    private final Location low;

    @JsonCreator
    public DefaultCuboid(@JsonProperty("high") Location high,
                         @JsonProperty("low") Location low) {
        if (high == null || low == null)
            throw new NullPointerException("Location can not be null");
        else if (high.getWorld() == null || low.getWorld() == null)
            throw new IllegalStateException("Can not create a Cuboid for an unloaded world");
        else if (!high.getWorld().getName().equals(low.getWorld().getName()))
            throw new IllegalStateException("Can not create a Cuboid between two different worlds");
        worldName = high.getWorld().getName();

        x1 = Math.min(high.getBlockX(), low.getBlockX());
        y1 = Math.min(high.getBlockY(), low.getBlockY());
        z1 = Math.min(high.getBlockZ(), low.getBlockZ());
        x2 = Math.max(high.getBlockX(), low.getBlockX());
        y2 = Math.max(high.getBlockY(), low.getBlockY());
        z2 = Math.max(high.getBlockZ(), low.getBlockZ());

        this.high = high;
        this.low = low;
    }

    public boolean containsLocation(Location l) {
        World world = l.getWorld();
        if(world == null)
            return false;

        if (!world.getName().equals(worldName))
            return false;

        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        if (x >= x1 && x <= x2)
            if (y >= y1 && y <= y2)
                return z >= z1 && z <= z2;

        return false;
    }

    public List<Block> getBlocks(){
        List<Block> blocks = new ArrayList<>();
        iterator().forEachRemaining(blocks::add);

        return blocks;
    }

    public void expand(){
        high.setY(255);
        low.setY(0);

        y1 = Math.min(high.getBlockY(), low.getBlockY());
        y2 = Math.max(high.getBlockY(), low.getBlockY());
    }


    public double getDiagonalLength(){
        double c1 = square(Math.abs(high.getX() - low.getX()));
        double c2 = square(Math.abs(high.getZ() - low.getZ()));

        return Math.sqrt(c1 + c2);
    }

    private double square(double num){
        return Math.pow(num, 2);
    }

    @Override
    public boolean containsMaterial(Material m) {
        if (m.isBlock())
            throw new IllegalArgumentException("'" + m.name() + "' is not a valid block material");
        for (Block b : this)
            if (b.getType() == m)
                return true;
        return false;
    }

    public boolean isEmpty() {
        return getContent().stream()
                .allMatch(location -> location.getBlock().getType() == Material.AIR);
    }

    @Override
    public void fill(Material m) {
        for (Block b : this)
            b.setType(m);
    }

    public int getSizeX() {
        return (x2 - x1) + 1;
    }

    public int getSizeY() {
        return (y2 - y1) + 1;
    }

    public int getSizeZ() {
        return (z2 - z1) + 1;
    }

    public Location[] getCorners() {
        return new Location[] { this.high, new Location(
                getWorld(), this.high.getX(), this.high.getY(), this.low.getZ()), new Location(
                getWorld(), this.low.getX(), this.high.getY(), this.low.getZ()), new Location(
                getWorld(), this.low.getX(), this.high.getY(), this.high.getZ()), this.low, new Location(
                getWorld(), this.low.getX(), this.low.getY(), this.high.getZ()), new Location(
                getWorld(), this.high.getX(), this.low.getY(), this.high.getZ()), new Location(
                getWorld(), this.high.getX(), this.low.getY(), this.low.getZ()) };
    }

    public List<DefaultCuboid> getWalls(boolean cover) {
        List<DefaultCuboid> walls = new ArrayList<>();
        walls.add(new DefaultCuboid(getCorners()[4], getCorners()[3]));
        walls.add(new DefaultCuboid(getCorners()[3], getCorners()[6]));
        walls.add(new DefaultCuboid(getCorners()[6], getCorners()[1]));
        walls.add(new DefaultCuboid(getCorners()[1], getCorners()[4]));
        if (cover) {
            walls.add(new DefaultCuboid(getCorners()[1], getCorners()[3]));
        }
        walls.add(new DefaultCuboid(getCorners()[4], getCorners()[6]));

        return walls;
    }

    public void fillWalls(Material material, boolean cover) {
        for (DefaultCuboid wall : getWalls(cover)) {
            for (Block block : wall) {
                block.setType(material);
            }
        }
    }

    public int getVolume() {
        return getSizeX() * getSizeY() * getSizeZ();
    }

    public Location getLowerNE() {
        return new Location(this.getWorld(), this.x1, this.y1, this.z1);
    }

    public Location getUpperSW() {
        return new Location(this.getWorld(), this.x2, this.y2, this.z2);
    }

    private World getWorld() {
        World w = Bukkit.getWorld(worldName);
        if (w == null)
            throw new IllegalStateException("World '" + worldName + "' is not loaded");

        return w;
    }

    public List<Location> getContent(){
        List<Location> content = new ArrayList<>();
        iterator().forEachRemaining(block -> content.add(block.getLocation()));

        return content;
    }

    @Override
    public Iterator<Block> iterator() {
        return new CuboidIterator(getWorld(), x1, y1, z1, x2, y2, z2);
    }

    private static class CuboidIterator implements Iterator<Block> {
        private final World w;
        private final int baseX, baseY, baseZ;
        private int x, y, z;
        private final int sizeX, sizeY, sizeZ;

        CuboidIterator(World w, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.w = w;
            baseX = x1;
            baseY = y1;
            baseZ = z1;
            sizeX = Math.abs(x2 - x1) + 1;
            sizeY = Math.abs(y2 - y1) + 1;
            sizeZ = Math.abs(z2 - z1) + 1;
            x = this.y = this.z = 0;
        }

        @Override
        public boolean hasNext() {
            return x < sizeX && y < sizeY && z < sizeZ;
        }

        @Override
        public Block next() {
            Block b = this.w.getBlockAt(baseX + x, baseY + y, baseZ + z);
            if (++x >= sizeX) {
                x = 0;
                if (++y >= this.sizeY) {
                    y = 0;
                    ++z;
                }
            }
            return b;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("This operation is not available");
        }

    }

}