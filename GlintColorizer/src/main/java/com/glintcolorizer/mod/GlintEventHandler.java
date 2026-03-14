package com.glintcolorizer.mod;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Listens to client-tick events so the rainbow hue can advance
 * independently of frame rate.
 */
@SideOnly(Side.CLIENT)
public class GlintEventHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            GlintRenderer.tickRainbow();
        }
    }
}
