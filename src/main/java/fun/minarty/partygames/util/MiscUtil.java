package fun.minarty.partygames.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.*;

@UtilityClass
public class MiscUtil {

    /**
     * Dispenses an entity from a dispenser
     * @param block dispenser block
     * @param type entity type to dispense
     * @param force force to shoot with
     * @return dispensed entity if successful
     */
    public Entity dispenseEntity(Block block, EntityType type, float force){
        BlockData blockData = block.getBlockData();
        if(!(blockData instanceof Dispenser))
            return null;

        Dispenser dispenser = (Dispenser) blockData;
        BlockFace blockFace = dispenser.getFacing();

        Location location = block.getRelative(blockFace).getLocation().add(0.5, 0.5, 0.5);
        Entity projectile = block.getWorld().spawnEntity(location, type);
        projectile.setVelocity(new Vector(blockFace.getModX(),
                blockFace.getModY(), blockFace.getModZ()).multiply(force));

        return projectile;
    }

    /**
     * Gets the player from a damage event
     * @param event damage event
     * @return player who damaged the entity or null if not applicable
     */
    public Player getPlayerDamager(EntityDamageByEntityEvent event){
        Player playerDamager = null;
        Entity damager = event.getDamager();
        if (damager instanceof Player) {
            playerDamager = (Player) damager;
        } else if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                playerDamager = (Player) shooter;
            }
        }

        return playerDamager;
    }

    public <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public ItemStack[] parseItemStackList(List<String> serializedItemStacks){
        ItemStack[] stacks = new ItemStack[serializedItemStacks.size()];

        for(String s : serializedItemStacks){

            String[] data = s.split(":");
            ItemStack stack = null;

            int index = Integer.parseInt(data[0]);

            if(data.length >= 3)
                stack = new ItemStack(Material.valueOf(data[1]), Integer.parseInt(data[2]));

            if(data.length == 4){
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(data[3]);
                stack.setItemMeta(meta);
            }

            stacks[index] = stack;
        }

        return stacks;
    }

}