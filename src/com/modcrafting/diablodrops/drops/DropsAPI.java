package com.modcrafting.diablodrops.drops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;

import com.modcrafting.diablodrops.DiabloDrops;
import com.modcrafting.diablodrops.items.Drop;
import com.modcrafting.diablodrops.items.Socket;
import com.modcrafting.diablodrops.items.Tome;
import com.modcrafting.diablodrops.tier.Tier;
import com.modcrafting.toolapi.lib.Tool;

public class DropsAPI
{
    private final DiabloDrops plugin;

    public DropsAPI(final DiabloDrops instance)
    {
        plugin = instance;
    }

    /**
     * Is material armor or tool?
     * 
     * @param material
     * @return is armor or tool
     */
    public boolean canBeItem(final Material material)
    {
        if (plugin.drop.isArmor(material) || plugin.drop.isTool(material))
            return true;
        return false;
    }

    /**
     * Determines if a List contains a string
     * 
     * @param l
     *            List to check
     * @param s
     *            String to find
     * @return if contains
     */
    public boolean containsIgnoreCase(final List<String> l, final String s)
    {
        Iterator<String> it = l.iterator();
        while (it.hasNext())
            if (it.next().equalsIgnoreCase(s))
                return true;
        return false;
    }

    /**
     * Gets a random amount of damage for an ItemStack
     * 
     * @param material
     *            of ItemStack
     * @return durability to be set
     */
    public short damageItemStack(final Material material)
    {
        short dur = material.getMaxDurability();
        try
        {
            int newDur = plugin.gen.nextInt(dur);
            return (short) newDur;
        }
        catch (Exception e)
        {
            dur = 0;
        }
        return dur;
    }

    /**
     * Returns a Material that was randomly picked
     * 
     * @return random material
     */
    public Material dropPicker()
    {
        int next = plugin.gen.nextInt(10);
        switch (next)
        {
            case 1:
                return plugin.drop.getHelmet();
            case 2:
                return plugin.drop.getChestPlate();
            case 3:
                return plugin.drop.getLeggings();
            case 4:
                return plugin.drop.getBoots();
            case 5:
                return plugin.drop.getHoe();
            case 6:
                return plugin.drop.getPickaxe();
            case 7:
                return plugin.drop.getAxe();
            case 8:
                return plugin.drop.getSpade();
            case 9:
                return Material.BOW;
            default:
                return plugin.drop.getSword();

        }
    }

    /**
     * Fills a chest with items.
     * 
     * @param block
     *            Material of item to get name for
     */
    public void fillChest(final Block block)
    {
        try
        {
            if (!(block.getState() instanceof Chest))
                return;
            Chest chestB = ((Chest) block.getState());
            Inventory chest = chestB.getBlockInventory();
            for (int i = 0; i < plugin.gen.nextInt(chest.getSize()); i++)
            {
                CraftItemStack cis = plugin.dropsAPI.getItem();
                while (cis == null)
                {
                    cis = plugin.dropsAPI.getItem();
                }
                chest.setItem(i, cis);
            }
        }
        catch (Exception e)
        {
        }
    }

    /**
     * Get a particular custom itemstack
     * 
     * @param name
     * @return particular custom itemstack
     */
    public CraftItemStack getCustomItem(final String name)
    {
        CraftItemStack cis = null;
        for (CraftItemStack item : plugin.custom)
            if (ChatColor.stripColor(((Tool) item).getName()).equalsIgnoreCase(
                    name))
            {
                cis = item;
            }
        return cis;
    }

    /**
     * Gets a list of safe enchantments for an item.
     * 
     * @param ci
     * @return set
     */
    public List<Enchantment> getEnchantStack(final CraftItemStack ci)
    {
        List<Enchantment> set = new ArrayList<Enchantment>();
        for (Enchantment e : Enchantment.values())
            if (e.canEnchantItem(ci))
            {
                set.add(e);
            }
        return set;
    }

    public CraftItemStack getIdItem(Material mat, final String name)
    {
        while (mat == null)
        {
            mat = dropPicker();
        }
        Material material = mat;
        CraftItemStack ci = null;
        Tier tier = getTier();
        while (tier == null)
        {
            tier = getTier();
        }
        if ((tier.getMaterials().size() > 0)
                && !tier.getMaterials().contains(material))
        {
            material = tier.getMaterials().get(
                    plugin.gen.nextInt(tier.getMaterials().size()));
        }
        int e = tier.getAmount();
        int l = tier.getLevels();
        short damage = 0;
        if (plugin.config.getBoolean("DropFix.Damage", true))
        {
            damage = damageItemStack(mat);
        }
        if (plugin.config.getBoolean("Display.TierName", true)
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            ci = new Drop(material, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage, tier.getColor()
                            + tier.getName());
        }
        else if (plugin.config.getBoolean("Display.TierName", true)
                && tier.getColor().equals(ChatColor.MAGIC))
        {
            ci = new Drop(material, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage, ChatColor.WHITE
                            + tier.getName());
        }
        else
        {
            ci = new Drop(material, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage);
        }
        if (tier.getColor().equals(ChatColor.MAGIC))
            return ci;
        Tool tool = new Tool(ci);
        for (String s : tier.getLore())
            if (s != null)
            {
                tool.addLore(s);
            }
        List<Enchantment> eStack = Arrays.asList(Enchantment.values());
        boolean safe = plugin.config.getBoolean("SafeEnchant.Enabled", true);
        if (safe)
        {
            eStack = getEnchantStack(ci);
        }
        for (; e > 0; e--)
        {
            int lvl = plugin.gen.nextInt(l + 1);
            int size = eStack.size();
            if (size < 1)
            {
                continue;
            }
            Enchantment ench = eStack.get(plugin.gen.nextInt(size));
            if ((lvl != 0) && (ench != null)
                    && !tier.getColor().equals(ChatColor.MAGIC))
                if (safe)
                {
                    if ((lvl >= ench.getStartLevel())
                            && (lvl <= ench.getMaxLevel()))
                    {
                        try
                        {
                            ci.addEnchantment(ench, lvl);
                        }
                        catch (Exception e1)
                        {
                            if (plugin.debug)
                            {
                                plugin.log.warning(e1.getMessage());
                            }
                            e++;
                        }
                    }
                }
                else
                {
                    ci.addUnsafeEnchantment(ench, lvl);
                }
        }
        if (plugin.config.getBoolean("SocketItem.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "SocketItem.Chance", 5)))
        {
            tool.addLore(colorPicker() + "(Socket)");
            return tool;
        }
        if (plugin.config.getBoolean("Lore.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "Lore.Chance", 5)))
        {
            for (int i = 0; i < plugin.config.getInt("Lore.EnhanceAmount", 2); i++)
                if (plugin.drop.isArmor(mat))
                {
                    tool.addLore(plugin.defenselore.get(plugin.gen
                            .nextInt(plugin.defenselore.size())));
                }
                else if (plugin.drop.isTool(mat))
                {
                    tool.addLore(plugin.offenselore.get(plugin.gen
                            .nextInt(plugin.offenselore.size())));
                }
        }
        return tool;
    }

    /**
     * Returns an itemstack that was randomly generated
     * 
     * @return CraftItemStack
     */
    public CraftItemStack getItem()
    {
        if (plugin.gen.nextBoolean()
                && plugin.config.getBoolean("IdentifyTome.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "IdentifyTome.Chance", 3)))
            return new Tome();
        if (plugin.gen.nextBoolean()
                && plugin.config.getBoolean("SocketItem.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "SocketItem.Chance", 5)))
        {
            List<String> l = plugin.config.getStringList("SocketItem.Items");
            return new Socket(Material.valueOf(l.get(
                    plugin.gen.nextInt(l.size())).toUpperCase()));
        }
        if (plugin.gen.nextBoolean()
                && plugin.config.getBoolean("Custom.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "Custom.Chance", 1)))
            return plugin.custom.get(plugin.gen.nextInt(plugin.custom.size()));
        return getItem(dropPicker());
    }

    /**
     * Returns a specific type of item randomly generated
     * 
     * @param mat
     *            Material of itemstack
     * @return item
     */
    public CraftItemStack getItem(Material mat)
    {
        while (mat == null)
        {
            mat = dropPicker();
        }
        CraftItemStack ci = null;
        Tier tier = getTier();
        while (tier == null)
        {
            tier = getTier();
        }
        if ((tier.getMaterials().size() > 0)
                && !tier.getMaterials().contains(mat))
        {
            mat = tier.getMaterials().get(
                    plugin.gen.nextInt(tier.getMaterials().size()));
        }
        int e = tier.getAmount();
        int l = tier.getLevels();
        short damage = 0;
        if (plugin.config.getBoolean("DropFix.Damage", true))
        {
            damage = damageItemStack(mat);
        }
        if (plugin.config.getBoolean("Display.TierName", true)
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            ci = new Drop(mat, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage, tier.getColor()
                            + tier.getName());
        }
        else if (plugin.config.getBoolean("Display.TierName", true)
                && tier.getColor().equals(ChatColor.MAGIC))
        {
            ci = new Drop(mat, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage, ChatColor.WHITE
                            + tier.getName());
        }
        else
        {
            ci = new Drop(mat, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage);
        }
        if (tier.getColor().equals(ChatColor.MAGIC))
            return ci;
        Tool tool = new Tool(ci);
        List<Enchantment> eStack = Arrays.asList(Enchantment.values());
        for (String s : tier.getLore())
            if (s != null)
            {
                tool.addLore(s);
            }
        boolean safe = plugin.config.getBoolean("SafeEnchant.Enabled", true);
        if (safe)
        {
            eStack = getEnchantStack(tool);
        }
        for (; e > 0; e--)
        {
            int lvl = plugin.gen.nextInt(l + 1);
            int size = eStack.size();
            if (size < 1)
            {
                continue;
            }
            Enchantment ench = eStack.get(plugin.gen.nextInt(size));
            if ((lvl != 0) && (ench != null)
                    && !tier.getColor().equals(ChatColor.MAGIC))
                if (safe)
                {
                    if ((lvl >= ench.getStartLevel())
                            && (lvl <= ench.getMaxLevel()))
                    {
                        try
                        {
                            tool.addEnchantment(ench, lvl);
                        }
                        catch (Exception e1)
                        {
                            if (plugin.debug)
                            {
                                plugin.log.warning(e1.getMessage());
                            }
                            e++;
                        }
                    }
                }
                else
                {
                    tool.addUnsafeEnchantment(ench, lvl);
                }
        }
        if (plugin.config.getBoolean("SocketItem.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "SocketItem.Chance", 5))
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            tool.addLore(colorPicker() + "(Socket)");
            return tool;
        }
        if (plugin.config.getBoolean("Lore.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "Lore.Chance", 5))
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            for (int i = 0; i < plugin.config.getInt("Lore.EnhanceAmount", 2); i++)
                if (plugin.drop.isArmor(mat))
                {
                    tool.addLore(plugin.defenselore.get(plugin.gen
                            .nextInt(plugin.defenselore.size())));
                }
                else if (plugin.drop.isTool(mat))
                {
                    tool.addLore(plugin.offenselore.get(plugin.gen
                            .nextInt(plugin.offenselore.size())));
                }
        }
        return tool;
    }

    /**
     * Returns an specific type of ItemStack that was randomly generated
     * 
     * @param tier
     *            name
     * @return item
     */
    public CraftItemStack getItem(Tier tier)
    {
        Material mat = dropPicker();
        while (mat == null)
        {
            mat = dropPicker();
        }
        while(tier==null)
        	tier = getTier();
        CraftItemStack ci = null;
        if ((tier.getMaterials().size() > 0)
                && !tier.getMaterials().contains(mat))
        {
            mat = tier.getMaterials().get(
                    plugin.gen.nextInt(tier.getMaterials().size()));
        }
        int e = tier.getAmount();
        int l = tier.getLevels();
        short damage = 0;
        if (plugin.config.getBoolean("DropFix.Damage", true))
        {
            damage = damageItemStack(mat);
        }
        if (plugin.config.getBoolean("Display.TierName", true)
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            ci = new Drop(mat, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage, tier.getColor()
                            + tier.getName());
        }
        else if (plugin.config.getBoolean("Display.TierName", true)
                && tier.getColor().equals(ChatColor.MAGIC))
        {
            ci = new Drop(mat, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage, ChatColor.WHITE
                            + tier.getName());
        }
        else
        {
            ci = new Drop(mat, tier.getColor(),
                    ChatColor.stripColor(name(mat)), damage);
        }
        if (tier.getColor().equals(ChatColor.MAGIC))
            return ci;
        Tool tool = new Tool(ci);
        for (String s : tier.getLore())
            if (s != null)
            {
                tool.addLore(s);
            }
        List<Enchantment> eStack = Arrays.asList(Enchantment.values());
        boolean safe = plugin.config.getBoolean("SafeEnchant.Enabled", true);
        if (safe)
        {
            eStack = getEnchantStack(ci);
        }
        for (; e > 0; e--)
        {
            int lvl = plugin.gen.nextInt(l + 1);
            int size = eStack.size();
            if (size < 1)
            {
                continue;
            }
            Enchantment ench = eStack.get(plugin.gen.nextInt(size));
            if ((lvl != 0) && (ench != null)
                    && !tier.getColor().equals(ChatColor.MAGIC))
                if (safe)
                {
                    if ((lvl >= ench.getStartLevel())
                            && (lvl <= ench.getMaxLevel()))
                    {
                        try
                        {
                            tool.addEnchantment(ench, lvl);
                        }
                        catch (Exception e1)
                        {
                            if (plugin.debug)
                            {
                                plugin.log.warning(e1.getMessage());
                            }
                            e++;
                        }
                    }
                }
                else
                {
                    tool.addUnsafeEnchantment(ench, lvl);
                }
        }
        if (plugin.config.getBoolean("SocketItem.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "SocketItem.Chance", 5))
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            tool.addLore(colorPicker() + "(Socket)");
            return tool;
        }
        if (plugin.config.getBoolean("Lore.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "Lore.Chance", 5))
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            for (int i = 0; i < plugin.config.getInt("Lore.EnhanceAmount", 2); i++)
                if (plugin.drop.isArmor(mat))
                {
                    tool.addLore(plugin.defenselore.get(plugin.gen
                            .nextInt(plugin.defenselore.size())));
                }
                else if (plugin.drop.isTool(mat))
                {
                    tool.addLore(plugin.offenselore.get(plugin.gen
                            .nextInt(plugin.offenselore.size())));
                }
        }

        return tool;
    }

    /**
     * Gets a new tool from an unidentified tool
     * 
     * @param tool
     * @return brand new tool
     */
    public Tool getItem(Tool tool)
    {
        short oldDam = tool.getDurability();
        tool = new Tool(tool.getType());
        tool.setDurability(oldDam);
        Tier tier = getTier();
        while ((tier == null) || tier.getColor().equals(ChatColor.MAGIC))
        {
            tier = getTier();
        }
        int e = tier.getAmount();
        int l = tier.getLevels();
        tool.setName(tier.getColor() + name(tool.getType()));
        if (plugin.config.getBoolean("Display.TierName", true))
        {
            tool.addLore(tier.getColor() + tier.getName());
        }
        for (String s : tier.getLore())
            if (s != null)
            {
                tool.addLore(s);
            }
        List<Enchantment> eStack = Arrays.asList(Enchantment.values());
        boolean safe = plugin.config.getBoolean("SafeEnchant.Enabled", true);
        if (safe)
        {
            eStack = getEnchantStack(tool);
        }
        for (; e > 0; e--)
        {
            int lvl = plugin.gen.nextInt(l + 1);
            int size = eStack.size();
            if (size < 1)
            {
                continue;
            }
            Enchantment ench = eStack.get(plugin.gen.nextInt(size));
            if ((lvl != 0) && (ench != null)
                    && !tier.getColor().equals(ChatColor.MAGIC))
                if (safe)
                {
                    if ((lvl >= ench.getStartLevel())
                            && (lvl <= ench.getMaxLevel()))
                    {
                        try
                        {
                            tool.addEnchantment(ench, lvl);
                        }
                        catch (Exception e1)
                        {
                            if (plugin.debug)
                            {
                                plugin.log.warning(e1.getMessage());
                            }
                            e++;
                        }
                    }
                }
                else
                {
                    tool.addUnsafeEnchantment(ench, lvl);
                }
        }
        boolean sock = false;
        if (plugin.config.getBoolean("SocketItem.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "SocketItem.Chance", 5))
                && !tier.getColor().equals(ChatColor.MAGIC))
        {
            tool.addLore(colorPicker() + "(Socket)");
            sock = true;
        }
        if (plugin.config.getBoolean("Lore.Enabled", true)
                && (plugin.gen.nextInt(100) <= plugin.config.getInt(
                        "Lore.Chance", 5))
                && !tier.getColor().equals(ChatColor.MAGIC) && !sock)
        {
            for (int i = 0; i < plugin.config.getInt("Lore.EnhanceAmount", 2); i++)
                if (plugin.drop.isArmor(tool.getType()))
                {
                    tool.addLore(plugin.defenselore.get(plugin.gen
                            .nextInt(plugin.defenselore.size())));
                }
                else if (plugin.drop.isTool(tool.getType()))
                {
                    tool.addLore(plugin.offenselore.get(plugin.gen
                            .nextInt(plugin.offenselore.size())));
                }
        }
        return tool;
    }

    /**
     * Gets a calculated tier.
     * 
     * @return tier
     */
    public Tier getTier()
    {
        for (Tier tier : plugin.tiers)
            if (plugin.gen.nextInt(100) <= tier.getChance())
                return tier;
        return null;
    }

    /**
     * Gets tier from name
     * 
     * @param name
     * @return tier
     */
    public Tier getTier(final String name)
    {
        for (Tier tier : plugin.tiers)
            if (tier.getName().equalsIgnoreCase(name))
                return tier;
        return null;
    }

    /**
     * Is type an actual tier?
     * 
     * @param type
     * @return is tier
     */
    public boolean matchesTier(final String type)
    {
        for (Tier tier : plugin.tiers)
            if (tier.getName().equalsIgnoreCase(type))
                return true;
        return false;
    }

    /**
     * Gets a random color.
     * 
     * @return color
     */
    public ChatColor colorPicker()
    {
        switch (plugin.gen.nextInt(4))
        {
            case 1:
                return ChatColor.RED;
            case 2:
                return ChatColor.BLUE;
            case 3:
                return ChatColor.GREEN;
            default:
                return ChatColor.RESET;
        }
    }

    /**
     * Gets a random name
     * 
     * @param material
     *            Material of item to get name for
     * @return name
     */
    public String name(final Material material)
    {
        String template = plugin.config.getString("Display.ItemNameFormat",
                "%randprefix% %randsuffix%");
        String prefix = "";
        String suffix = "";
        String matName = material.name();
        if (plugin.hmprefix.containsKey(material))
        {
            List<String> l = plugin.hmprefix.get(material);
            prefix = l.get(plugin.gen.nextInt(l.size()));
        }
        if (plugin.hmsuffix.containsKey(material))
        {
            List<String> l = plugin.hmsuffix.get(material);
            suffix = l.get(plugin.gen.nextInt(l.size()));
        }
        boolean t = plugin.config.getBoolean("Display.ItemMaterialExtras",
                false);
        if ((prefix.length() < 1) || !t)
        {
            prefix = plugin.prefix
                    .get(plugin.gen.nextInt(plugin.prefix.size()));
        }
        if ((suffix.length() < 1) || !t)
        {
            suffix = plugin.suffix
                    .get(plugin.gen.nextInt(plugin.suffix.size()));
        }

        if (template == null)
            return null;
        return template.replace("%randprefix%", prefix)
                .replace("%randsuffix%", suffix).replace("%matname%", matName);
    }

    /**
     * Replace a line of lore with another line
     * 
     * @param tool
     *            Tool to replace lore on
     * @param toReplace
     *            Line of lore to be replaced
     * @param replaceWith
     *            Line replacing toReplace
     * @return Tool with new lore
     */
    public Tool replaceLore(final Tool tool, final String toReplace,
            final String replaceWith)
    {
        List<String> loreList = tool.getLoreList();
        if ((loreList == null) || loreList.isEmpty())
            return tool;
        for (String s : tool.getLoreList())
            if (s.equals(toReplace))
            {
                loreList.remove(s);
                loreList.add(replaceWith);
            }
        tool.setLore(loreList);
        return tool;
    }
}
