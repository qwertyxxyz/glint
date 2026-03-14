package com.glintcolorizer.mod;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.Color;

/**
 * Provides the RGBA color used to tint the enchantment glint.
 *
 * Call {@link #getGlintColor()} from your mixin / ASM patch where
 * Minecraft normally applies the purple glint overlay.
 */
@SideOnly(Side.CLIENT)
public class GlintRenderer {

    /** Rainbow hue, advances every client tick. */
    private static float rainbowHue = 0.0f;

    /** Degrees-per-tick for rainbow cycling (configurable via speed). */
    private static final float HUE_STEP = 0.5f; // 0–360 deg domain used as 0.0–1.0

    /**
     * Call once per client tick to advance the rainbow animation.
     */
    public static void tickRainbow() {
        if (!GlintConfig.enabled) return;
        if ("rainbow".equalsIgnoreCase(GlintConfig.renderMode)) {
            rainbowHue += (HUE_STEP * GlintConfig.glintSpeed) / 360.0f;
            if (rainbowHue > 1.0f) rainbowHue -= 1.0f;
        }
    }

    /**
     * Returns the current glint color as a packed ARGB int,
     * suitable for use with GL11.glColor4f() after unpacking.
     */
    public static int getGlintColor() {
        if (!GlintConfig.enabled) {
            // Vanilla purple: 0x80_6400FF
            return packARGB(128, 100, 0, 255);
        }

        switch (GlintConfig.renderMode.toLowerCase()) {

            case "rainbow": {
                Color c = Color.getHSBColor(rainbowHue, 1.0f, 1.0f);
                int alpha = Math.round(GlintConfig.glintAlpha * 255);
                return packARGB(alpha, c.getRed(), c.getGreen(), c.getBlue());
            }

            case "default": {
                // Restore vanilla values
                return packARGB(128, 100, 0, 255);
            }

            default: // "custom"
            {
                int alpha = Math.round(GlintConfig.glintAlpha * 255);
                return packARGB(
                    alpha,
                    GlintConfig.glintRed,
                    GlintConfig.glintGreen,
                    GlintConfig.glintBlue
                );
            }
        }
    }

    /** Unpack and return the red channel (0–1 float). */
    public static float getRed()   { return ((getGlintColor() >> 16) & 0xFF) / 255.0f; }
    /** Unpack and return the green channel (0–1 float). */
    public static float getGreen() { return ((getGlintColor() >>  8) & 0xFF) / 255.0f; }
    /** Unpack and return the blue channel (0–1 float). */
    public static float getBlue()  { return  (getGlintColor()        & 0xFF) / 255.0f; }
    /** Unpack and return the alpha channel (0–1 float). */
    public static float getAlpha() { return ((getGlintColor() >> 24) & 0xFF) / 255.0f; }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static int packARGB(int a, int r, int g, int b) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }
}
