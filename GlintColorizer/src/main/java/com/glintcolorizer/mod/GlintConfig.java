package com.glintcolorizer.mod;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class GlintConfig {

    private static Configuration config;

    // ── Color values (0–255) ──────────────────────────────────────────────────
    public static int glintRed   = 100;
    public static int glintGreen = 0;
    public static int glintBlue  = 255;

    // ── Opacity / intensity ───────────────────────────────────────────────────
    public static float glintAlpha = 0.5f;

    // ── Animation speed multiplier ────────────────────────────────────────────
    public static float glintSpeed = 1.0f;

    // ── Enable / disable the mod completely ──────────────────────────────────
    public static boolean enabled = true;

    // ── Render mode: "default", "rainbow", "custom" ──────────────────────────
    public static String renderMode = "custom";

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncConfig();
    }

    public static void syncConfig() {
        config.load();

        enabled = config.getBoolean(
            "enabled", Configuration.CATEGORY_GENERAL, true,
            "Enable or disable the Glint Colorizer mod entirely.");

        renderMode = config.getString(
            "renderMode", Configuration.CATEGORY_GENERAL, "custom",
            "Render mode: 'default' (vanilla), 'custom' (solid color), 'rainbow' (animated hue cycle).");

        glintRed = config.getInt(
            "glintRed", "color", 100, 0, 255,
            "Red channel of the custom glint color (0-255).");

        glintGreen = config.getInt(
            "glintGreen", "color", 0, 0, 255,
            "Green channel of the custom glint color (0-255).");

        glintBlue = config.getInt(
            "glintBlue", "color", 255, 0, 255,
            "Blue channel of the custom glint color (0-255).");

        glintAlpha = (float) config.getFloat(
            "glintAlpha", "color", 0.5f, 0.0f, 1.0f,
            "Alpha (opacity) of the glint overlay (0.0-1.0).");

        glintSpeed = (float) config.getFloat(
            "glintSpeed", "animation", 1.0f, 0.1f, 10.0f,
            "Speed multiplier for the glint animation.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static Configuration getConfig() {
        return config;
    }
}
