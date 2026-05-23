package net.maximlvr.overcooked.block.custom;

import com.mojang.serialization.MapCodec;
import net.maximlvr.overcooked.block.ModBlocks;
import net.maximlvr.overcooked.block.entity.PanBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class PanBlock extends BaseEntityBlock {

    public static final MapCodec<PanBlock> CODEC = simpleCodec(PanBlock::new);

    public PanBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PanBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, net.minecraft.world.InteractionHand hand,
                                              BlockHitResult hitResult) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof PanBlockEntity panBlockEntity)) {
            return ItemInteractionResult.FAIL;
        }

        if (panBlockEntity.hasItem()) {
            player.displayClientMessage(Component.literal("La poêle contient déjà un item."), true);
            return ItemInteractionResult.SUCCESS;
        }

        if (stack.is(Items.BEEF) || stack.is(Items.PORKCHOP)) {
            ItemStack itemToStore = stack.copyWithCount(1);
            panBlockEntity.setStoredItem(itemToStore);

            if (!player.isCreative()) {
                stack.shrink(1);
            }

            player.displayClientMessage(Component.literal("Item ajouté dans la poêle."), true);
            return ItemInteractionResult.SUCCESS;
        }

        player.displayClientMessage(Component.literal("Tu peux seulement mettre du steak cru ou du porc cru."), true);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof PanBlockEntity panBlockEntity)) {
            return InteractionResult.FAIL;
        }

        ItemStack panStack = new ItemStack(ModBlocks.PAN.get().asItem());

        if (panBlockEntity.hasItem()) {
            CompoundTag panTag = new CompoundTag();
            panTag.put("StoredItem", panBlockEntity.getStoredItem().saveOptional(level.registryAccess()));
            panStack.set(DataComponents.CUSTOM_DATA, CustomData.of(panTag));
        }

        level.removeBlock(pos, false);

        if (!player.getInventory().add(panStack)) {
            player.drop(panStack, false);
        }

        player.displayClientMessage(Component.literal("Poêle récupérée."), true);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state,
                            @Nullable net.minecraft.world.entity.LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof PanBlockEntity panBlockEntity)) {
            return;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);

        if (customData == null) {
            return;
        }

        CompoundTag tag = customData.copyTag();

        if (!tag.contains("StoredItem")) {
            return;
        }

        ItemStack storedItem = ItemStack.parseOptional(
                level.registryAccess(),
                tag.getCompound("StoredItem")
        );

        if (!storedItem.isEmpty()) {
            panBlockEntity.setStoredItem(storedItem);
        }
    }
}