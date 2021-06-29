package fun.minarty.partygames.model.kit;

import fun.minarty.partygames.api.model.kit.Kit;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Objects;

/**
 * Object that maps a player inventory to a name which can then
 * be loaded back to any player, with an exact replica of the inventory
 */
public class DefaultKit implements Kit {

    @Getter
    private final String name;
    private final ItemStack[] contents;
    private final ItemStack[] armour;
    private final ItemStack[] extra;

    public DefaultKit(String name, ItemStack[] contents, ItemStack[] armour, ItemStack[] extra) {
        this.name     = name;
        this.contents = contents;
        this.armour   = armour;
        this.extra    = extra;
    }

    /**
     * Creates a Kit from bukkit config
     * @param section section to load from
     * @return {@link DefaultKit} object
     */
    public static DefaultKit fromConfig(ConfigurationSection section){
        ItemStack[] contents = getStacksFromSection(section, "contents");
        ItemStack[] armour   = getStacksFromSection(section, "armour");
        ItemStack[] extra    = getStacksFromSection(section, "extra");
        return new DefaultKit(section.getName(), contents, armour, extra);
    }

    /**
     * Creates a kit object and saves it to the section
     * @param inventory inventory to save
     * @param name name to save
     * @param section section to save to
     * @return new Kit object
     */
    public static DefaultKit toConfig(PlayerInventory inventory, String name, ConfigurationSection section) {
        section.set("contents", inventory.getContents());
        section.set("armour", inventory.getArmorContents());
        section.set("extra", inventory.getExtraContents());
        return new DefaultKit(name, inventory.getContents(), inventory.getArmorContents(), inventory.getExtraContents());
    }

    /**
     * Applies the content of the kit to the player
     * @param player player to apply to
     */
    public void apply(Player player){
        PlayerInventory inventory = player.getInventory();
        inventory.clear();
        inventory.setContents(contents);
        inventory.setArmorContents(armour);
        inventory.setExtraContents(extra);
        player.updateInventory();
    }

    /**
     * Gets a list of ItemStacks by key in the section
     * @param section section to get from
     * @param key key to get
     * @return array of stacks by the key
     */
    @SuppressWarnings("unchecked")
    private static ItemStack[] getStacksFromSection(ConfigurationSection section, String key){
        return ((List<ItemStack>) Objects.requireNonNull(section.get(key))).toArray(new ItemStack[0]);
    }

    @Override
    public ItemStack[] getContents() {
        return contents;
    }

    @Override
    public ItemStack[] getArmour() {
        return armour;
    }

    @Override
    public ItemStack[] getExtra() {
        return extra;
    }

}