package net.maximlvr.overcooked.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.maximlvr.overcooked.block.ModBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;

public class PanItemRenderer extends BlockEntityWithoutLevelRenderer {

    public PanItemRenderer() {
        super(
                Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                Minecraft.getInstance().getEntityModels()
        );
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        poseStack.pushPose();

        renderPanModel(poseStack, bufferSource, packedLight, packedOverlay);

        ItemStack storedItem = getStoredItem(stack);

        if (!storedItem.isEmpty()) {
            renderStoredItem(storedItem, displayContext, poseStack, bufferSource, packedLight, packedOverlay);
        }

        poseStack.popPose();
    }

    private void renderPanModel(PoseStack poseStack, MultiBufferSource bufferSource,
                                int packedLight, int packedOverlay) {
        BlockState state = ModBlocks.PAN.get().defaultBlockState();

        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                state,
                poseStack,
                bufferSource,
                packedLight,
                packedOverlay
        );
    }

    private void renderStoredItem(ItemStack storedItem, ItemDisplayContext displayContext, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();

        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5D, 0.35D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.9F, 0.9F, 0.9F);
        } else {
            poseStack.translate(0.5D, 0.25D, 0.5D);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(0.65F, 0.65F, 0.65F);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(
                storedItem,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                Minecraft.getInstance().level,
                0
        );

        poseStack.popPose();
    }

    private ItemStack getStoredItem(ItemStack panStack) {
        CustomData customData = panStack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return ItemStack.EMPTY;
        }

        CompoundTag tag = customData.copyTag();

        if (!tag.contains("StoredItem")) {
            return ItemStack.EMPTY;
        }

        if (Minecraft.getInstance().level == null) {
            return ItemStack.EMPTY;
        }

        return ItemStack.parseOptional(
                Minecraft.getInstance().level.registryAccess(),
                tag.getCompound("StoredItem")
        );
    }
}