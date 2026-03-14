package com.glintcolorizer.mod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = GlintColorizer.MODID,
    name = GlintColorizer.NAME,
    version = GlintColorizer.VERSION,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]"
)
public class GlintColorizer {

    public static final String MODID   = "glintcolorizer";
    public static final String NAME    = "Glint Colorizer";
    public static final String VERSION = "1.0.0";

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        GlintConfig.init(event.getSuggestedConfigurationFile());
        logger.info("[GlintColorizer] Pre-initialization complete.");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new GlintEventHandler());
        logger.info("[GlintColorizer] Registered event handler.");
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("[GlintColorizer] Post-initialization complete.");
    }
}
