package net.maximlvr.overcooked.client;

import net.maximlvr.overcooked.OverCookedMod;
import net.maximlvr.overcooked.block.entity.ModBlockEntities;
import net.maximlvr.overcooked.client.renderer.PanBlockEntityRenderer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(
        modid = OverCookedMod.MOD_ID,
        value = Dist.CLIENT
)
public class OverCookedClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                ModBlockEntities.PAN_BE.get(),
                PanBlockEntityRenderer::new
        );
    }
}