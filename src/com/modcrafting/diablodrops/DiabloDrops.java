package com.modcrafting.diablodrops;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import net.h31ix.updater.Updater;
import net.h31ix.updater.Updater.UpdateResult;
import net.h31ix.updater.Updater.UpdateType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.modcrafting.devbuild.DevUpdater;
import com.modcrafting.devbuild.DevUpdater.DevUpdateResult;
import com.modcrafting.diablodrops.builders.ArmorSetBuilder;
import com.modcrafting.diablodrops.builders.CustomBuilder;
import com.modcrafting.diablodrops.builders.SocketBuilder;
import com.modcrafting.diablodrops.builders.TierBuilder;
import com.modcrafting.diablodrops.commands.DiabloDropCommand;
import com.modcrafting.diablodrops.drops.DropUtils;
import com.modcrafting.diablodrops.drops.DropsAPI;
import com.modcrafting.diablodrops.listeners.ChunkListener;
import com.modcrafting.diablodrops.listeners.EffectsListener;
import com.modcrafting.diablodrops.listeners.MobListener;
import com.modcrafting.diablodrops.listeners.SetListener;
import com.modcrafting.diablodrops.listeners.SocketListener;
import com.modcrafting.diablodrops.listeners.TomeListener;
import com.modcrafting.diablodrops.log.LogHandler;
import com.modcrafting.diablodrops.name.NamesLoader;
import com.modcrafting.diablodrops.sets.ArmorSet;
import com.modcrafting.diablodrops.sets.SetsAPI;
import com.modcrafting.diablodrops.tier.Tier;
import com.modcrafting.toolapi.lib.Tool;

public class DiabloDrops extends JavaPlugin
{
    public boolean debug;
    public Random gen = new Random();
    public DropUtils drop = new DropUtils(gen);
    public List<String> prefix = new ArrayList<String>();
    public List<String> suffix = new ArrayList<String>();
    public HashMap<Material, List<String>> hmprefix = new HashMap<Material, List<String>>();
    public HashMap<Material, List<String>> hmsuffix = new HashMap<Material, List<String>>();
    public HashSet<Tier> tiers = new HashSet<Tier>();
    public HashSet<ArmorSet> armorSets = new HashSet<ArmorSet>();
    public List<Tool> custom = new ArrayList<Tool>();
    public List<String> worlds = new ArrayList<String>();
    public List<String> defenselore = new ArrayList<String>();
    public List<String> offenselore = new ArrayList<String>();
    public HashMap<Block, ItemStack> furnanceMap = new HashMap<Block, ItemStack>();
    private NamesLoader nameLoader;
    public FileConfiguration config;
    public DropsAPI dropsAPI;
    public SetsAPI setsAPI;
    public Integer build;
    private static DiabloDrops instance;

    /**
     * Gets the instance of DiabloDrops
     * 
     * @return plugin's instance
     */
    public static DiabloDrops getInstance()
    {
        return instance;
    }

    private int id;

    public Logger log;

    /**
     * Stops all tasks for the plugin.
     */
    public void killTasks()
    {
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public void onDisable()
    {
        killTasks();
        prefix.clear();
        suffix.clear();
        hmprefix.clear();
        hmsuffix.clear();
        tiers.clear();
        armorSets.clear();
        custom.clear();
        worlds.clear();
        offenselore.clear();
        defenselore.clear();
        furnanceMap.clear();
    }

    @Override
    public void onEnable()
    {
        instance = this;
        getDataFolder().mkdir();
        log = getLogger();
        log.addHandler(new LogHandler(getDataFolder()));
        nameLoader = new NamesLoader(this);
        nameLoader.writeDefault("config.yml");
        nameLoader.writeDefault("custom.yml");
        nameLoader.writeDefault("tier.yml");
        nameLoader.writeDefault("set.yml");
        nameLoader.writeDefault("prefix.txt");
        nameLoader.writeDefault("suffix.txt");
        nameLoader.writeDefault("defenselore.txt");
        nameLoader.writeDefault("offenselore.txt");
        config = getConfig();
        if (config.getBoolean("Display.ItemMaterialExtras", false))
        {
            // Works now.
            File loc = new File(getDataFolder(), "/NamesPrefix/");
            if (!loc.exists())
                loc.mkdir();
            for (File f : loc.listFiles())
                if (f.getName().endsWith(".txt"))
                {
                    getLogger().info("Loading Prefix File:" + f.getName());
                    nameLoader.loadFile(hmprefix, new File(loc, f.getName()));
                }
            File sloc = new File(getDataFolder(), "/NamesSuffix/");
            if (!sloc.exists())
                sloc.mkdir();
            for (File f : loc.listFiles())
                if (f.getName().endsWith(".txt"))
                {
                    getLogger().info("Loading Suffix File:" + f.getName());
                    nameLoader.loadFile(hmsuffix, new File(sloc, f.getName()));
                }
        }
        nameLoader.loadFile(prefix, "prefix.txt");
        nameLoader.loadFile(suffix, "suffix.txt");
        nameLoader.loadFile(defenselore, "defenselore.txt");
        nameLoader.loadFile(offenselore, "offenselore.txt");
        new CustomBuilder(this).build();
        new SocketBuilder(this).build();
        new TierBuilder(this).build();
        new ArmorSetBuilder(this).build();
        dropsAPI = new DropsAPI(this);
        setsAPI = new SetsAPI(this);
        if (config.getBoolean("Worlds.Enabled", false))
        {
            for (String s : config.getStringList("Worlds.Allowed"))
            {
                worlds.add(s.toLowerCase());
            }
        }
        debug = config.getBoolean("Plugin.Debug", false);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new MobListener(this), this);
        pm.registerEvents(new TomeListener(this), this);
        pm.registerEvents(new SocketListener(this), this);
        pm.registerEvents(new ChunkListener(this), this);
        pm.registerEvents(new EffectsListener(this), this);
        pm.registerEvents(new SetListener(this), this);

        getCommand("diablodrops").setExecutor(new DiabloDropCommand(this));

        // AutoUpdater
        final PluginDescriptionFile pdf = getDescription();
        if (config.getBoolean("Plugin.AutoUpdate", true))
        	getServer().getScheduler().runTaskAsynchronously(this,
           // getServer().getScheduler().scheduleAsyncDelayedTask(this,
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Updater up = new Updater(getInstance(), pdf
                                    .getName().toLowerCase(), getFile(),
                                    UpdateType.DEFAULT, true);
                            if (!up.getResult().equals(UpdateResult.SUCCESS)
                                    || up.pluginFile(getFile().getName()))
                            {
                                if (up.getResult().equals(
                                        Updater.UpdateResult.FAIL_NOVERSION))
                                    log.info("Unable to connect to dev.bukkit.org.");
                                else
                                    log.info("No Updates found on dev.bukkit.org.");
                            }
                            else
                                log.info("Update "
                                        + up.getLatestVersionString()
                                        + " found and downloaded please restart your server.");
                        }

                    });
        // Jenkins AutoUpdater
        if (config.getBoolean("Plugin.Dev.Update", false))
           // id = getServer().getScheduler().scheduleAsyncRepeatingTask(this,

            id = getServer().getScheduler().runTaskTimerAsynchronously(this,
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            DevUpdater up = new DevUpdater(getInstance(),
                                    getFile(), build);
                            if (up.getResult().equals(DevUpdateResult.FAILED))
                                return;

                            if (up.getResult().equals(DevUpdateResult.SUCCESS))
                            {
                                getServer().getScheduler().cancelTask(id);
                                getServer()
                                        .broadcastMessage(
                                                ChatColor.AQUA
                                                        + "Jenkins Update Downloaded Build#"
                                                        + String.valueOf(up
                                                                .getBuild()));
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        long time = System.currentTimeMillis() + 30 * 1000;
                                        boolean voodoo = true;
                                        while (voodoo)
                                            // Conducting Voodoo
                                            if (time > System
                                                    .currentTimeMillis())
                                            {
                                                voodoo = false;
                                                Bukkit.getServer().reload();
                                            }

                                    }
                                }).start();
                            }
                            build = up.getBuild();
                        }
                    }, 0, 2400).getTaskId();
    }
}
