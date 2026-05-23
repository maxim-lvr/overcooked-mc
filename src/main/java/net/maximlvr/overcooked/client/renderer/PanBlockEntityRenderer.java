package net.maximlvr.overcooked.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.maximlvr.overcooked.OverCookedMod;
import net.maximlvr.overcooked.block.entity.PanBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

public class PanBlockEntityRenderer implements BlockEntityRenderer<PanBlockEntity> {

    private static final ResourceLocation BUBBLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OverCookedMod.MOD_ID, "textures/gui/food_bubble.png");

    public PanBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PanBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        ItemStack stack = blockEntity.getStoredItem();

        if (stack.isEmpty()) {
            return;
        }

        renderItemInPan(blockEntity, stack, poseStack, bufferSource, packedLight, packedOverlay);
        renderBubble(blockEntity, stack, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderItemInPan(PanBlockEntity blockEntity, ItemStack stack, PoseStack poseStack,
                                 MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        poseStack.translate(0.5D, 0.12D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.45F, 0.45F, 0.45F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );

        poseStack.popPose();
    }

    private void renderBubble(PanBlockEntity blockEntity, ItemStack stack, PoseStack poseStack,
                              MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        // Position de la bulle au-dessus de la poêle
        poseStack.translate(0.5D, 0.78D, 0.5D);

        // La bulle regarde toujours la caméra
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());

        // Taille globale de la bulle
        poseStack.scale(0.55F, 0.55F, 0.55F);

        drawBubbleQuad(poseStack, bufferSource, packedLight);

        // Item affiché devant la bulle
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.03D);
        poseStack.scale(0.65F, 0.65F, 0.65F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack,
                ItemDisplayContext.GUI,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                blockEntity.getLevel(),
                0
        );

        poseStack.popPose();
        poseStack.popPose();
    }

    private void drawBubbleQuad(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Matrix4f matrix = poseStack.last().pose();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(BUBBLE_TEXTURE));

        float size = 0.6F;

        consumer.addVertex(matrix, -size, -size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, size, -size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, size, size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, -size, size, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}