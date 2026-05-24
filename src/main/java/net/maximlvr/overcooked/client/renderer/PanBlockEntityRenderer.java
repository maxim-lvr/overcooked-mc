package net.maximlvr.overcooked.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.maximlvr.overcooked.OverCookedMod;
import net.maximlvr.overcooked.block.entity.PanBlockEntity;
import net.maximlvr.overcooked.item.ModItems;
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

    private static final ResourceLocation BUBBLE_WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OverCookedMod.MOD_ID, "textures/gui/food_bubble_white.png");

    private static final ResourceLocation BUBBLE_YELLOW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OverCookedMod.MOD_ID, "textures/gui/food_bubble_yellow.png");

    private static final ResourceLocation BUBBLE_GREEN_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OverCookedMod.MOD_ID, "textures/gui/food_bubble_green.png");

    private static final ResourceLocation BUBBLE_WARNING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OverCookedMod.MOD_ID, "textures/gui/food_bubble_warning.png");

    private static final ResourceLocation BUBBLE_RED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(OverCookedMod.MOD_ID, "textures/gui/food_bubble_red.png");

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

        poseStack.translate(0.5D, 0.78D, 0.5D);
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.scale(0.55F, 0.55F, 0.55F);

        if (stack.is(ModItems.BURNED_FOOD.get())) {
            drawBubbleQuad(poseStack, bufferSource, packedLight, BUBBLE_RED_TEXTURE, 255);
        } else if (stack.is(ModItems.PATTY_COOKED.get())) {
            int burn = blockEntity.getBurningProgress();

            if (burn >= blockEntity.getWarningStartProgress()) {
                float warningProgress = (burn - blockEntity.getWarningStartProgress())
                        / (float) (blockEntity.getMaxBurningProgress() - blockEntity.getWarningStartProgress());

                warningProgress = Math.max(0.0F, Math.min(1.0F, warningProgress));

                long gameTime = blockEntity.getLevel() != null ? blockEntity.getLevel().getGameTime() : 0L;

                float minSpeed = 0.15F;
                float maxSpeed = 0.85F;
                float speed = minSpeed + ((maxSpeed - minSpeed) * warningProgress);

                float alphaWave = (float) ((Math.sin(gameTime * speed) + 1.0D) / 2.0D);

                int warningAlpha = (int) (80 + (175 * alphaWave));

                drawBubbleQuad(poseStack, bufferSource, packedLight, BUBBLE_GREEN_TEXTURE, 255);
                drawBubbleQuad(poseStack, bufferSource, packedLight, BUBBLE_WARNING_TEXTURE, warningAlpha);
            } else {
                drawBubbleQuad(poseStack, bufferSource, packedLight, BUBBLE_GREEN_TEXTURE, 255);
            }
        } else if (stack.is(ModItems.PATTY_UNCOOKED.get()) && blockEntity.getCookingProgress() > 0) {
            float progress = blockEntity.getCookingProgress() / (float) blockEntity.getMaxCookingProgress();
            progress = Math.max(0.0F, Math.min(1.0F, progress));

            // Partie jaune déjà chargée
            drawPartialBubbleQuad(poseStack, bufferSource, packedLight, BUBBLE_YELLOW_TEXTURE, 0.0F, progress);

            // Partie blanche restante
            drawPartialBubbleQuad(poseStack, bufferSource, packedLight, BUBBLE_WHITE_TEXTURE, progress, 1.0F);
        } else {
            drawBubbleQuad(poseStack, bufferSource, packedLight, BUBBLE_WHITE_TEXTURE, 255);
        }

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

    private void drawBubbleQuad(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                ResourceLocation texture, int alpha) {
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        float size = 0.6F;

        consumer.addVertex(matrix, -size, -size, 0.0F)
                .setColor(255, 255, 255, alpha)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, size, -size, 0.0F)
                .setColor(255, 255, 255, alpha)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, size, size, 0.0F)
                .setColor(255, 255, 255, alpha)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, -size, size, 0.0F)
                .setColor(255, 255, 255, alpha)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);
    }

    private void drawPartialBubbleQuad(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                                       ResourceLocation texture, float startProgress, float endProgress) {
        startProgress = Math.max(0.0F, Math.min(1.0F, startProgress));
        endProgress = Math.max(0.0F, Math.min(1.0F, endProgress));

        if (endProgress <= startProgress) {
            return;
        }

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(texture));

        float size = 0.6F;

        float left = -size + (size * 2.0F * startProgress);
        float right = -size + (size * 2.0F * endProgress);
        float bottom = -size;
        float top = size;

        float uMin = startProgress;
        float uMax = endProgress;
        float vMin = 0.0F;
        float vMax = 1.0F;

        float z = 0.01F;

        consumer.addVertex(matrix, left, bottom, z)
                .setColor(255, 255, 255, 255)
                .setUv(uMin, vMax)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, right, bottom, z)
                .setColor(255, 255, 255, 255)
                .setUv(uMax, vMax)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, right, top, z)
                .setColor(255, 255, 255, 255)
                .setUv(uMax, vMin)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);

        consumer.addVertex(matrix, left, top, z)
                .setColor(255, 255, 255, 255)
                .setUv(uMin, vMin)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(0.0F, 0.0F, 1.0F);
    }
}