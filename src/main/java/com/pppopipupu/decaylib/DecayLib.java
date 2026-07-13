package com.pppopipupu.decaylib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import com.pppopipupu.decaylib.recipe.DecayMergeRecipe;

@Mod(
    modid = DecayLib.MODID,
    version = "1.0.0",
    name = "DecayLib",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:hbm")
public class DecayLib {

    public static final String MODID = "decaylib";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.Instance(DecayLib.MODID)
    public static DecayLib instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.addRecipe(new DecayMergeRecipe());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        DecayConfig.load();
    }
}
