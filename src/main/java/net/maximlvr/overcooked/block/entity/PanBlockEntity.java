package net.maximlvr.overcooked.block.entity;

import net.maximlvr.overcooked.block.ModBlocks;
import net.maximlvr.overcooked.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class PanBlockEntity extends BlockEntity {

    public static final int MAX_COOKING_PROGRESS = 80; // 4 secondes
    public static final int WARNING_START_PROGRESS = 60; // 3 secondes après cuisson
    public static final int MAX_BURNING_PROGRESS = 160; // 5 secondes après cuisson

    private ItemStack storedItem = ItemStack.EMPTY;
    private int cookingProgress = 0;
    private int burningProgress = 0;

    public PanBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PAN_BE.get(), pos, blockState);
    }

    public boolean hasItem() {
        return !storedItem.isEmpty();
    }

    public ItemStack getStoredItem() {
        return storedItem;
    }

    public int getCookingProgress() {
        return cookingProgress;
    }

    public int getMaxCookingProgress() {
        return MAX_COOKING_PROGRESS;
    }

    public int getBurningProgress() {
        return burningProgress;
    }

    public int getWarningStartProgress() {
        return WARNING_START_PROGRESS;
    }

    public int getMaxBurningProgress() {
        return MAX_BURNING_PROGRESS;
    }

    public void setCookingProgress(int cookingProgress) {
        this.cookingProgress = Math.max(0, Math.min(cookingProgress, MAX_COOKING_PROGRESS));
        setChanged();
        sync();
    }

    public void setBurningProgress(int burningProgress) {
        this.burningProgress = Math.max(0, Math.min(burningProgress, MAX_BURNING_PROGRESS));
        setChanged();
        sync();
    }

    public void setStoredItem(ItemStack stack) {
        this.storedItem = stack.copy();

        if (this.storedItem.isEmpty()) {
            this.cookingProgress = 0;
            this.burningProgress = 0;
        }

        setChanged();
        sync();
    }

    public ItemStack removeStoredItem() {
        ItemStack stack = storedItem.copy();

        storedItem = ItemStack.EMPTY;
        cookingProgress = 0;
        burningProgress = 0;

        setChanged();
        sync();

        return stack;
    }

    private static boolean isCookable(ItemStack stack) {
        return stack.is(ModItems.PATTY_UNCOOKED.get());
    }

    private static boolean isCooked(ItemStack stack) {
        return stack.is(ModItems.PATTY_COOKED.get());
    }

    private static boolean isOnBurner(Level level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(ModBlocks.BURNER.get());
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PanBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        if (!isOnBurner(level, pos)) {
            return;
        }

        if (isCookable(blockEntity.storedItem)) {
            blockEntity.cookingProgress++;

            if (blockEntity.cookingProgress >= MAX_COOKING_PROGRESS) {
                blockEntity.storedItem = new ItemStack(ModItems.PATTY_COOKED.get());
                blockEntity.cookingProgress = MAX_COOKING_PROGRESS;
                blockEntity.burningProgress = 0;
            }

            blockEntity.setChanged();

            if (blockEntity.cookingProgress % 2 == 0 || blockEntity.cookingProgress >= MAX_COOKING_PROGRESS) {
                blockEntity.sync();
            }

            return;
        }

        if (isCooked(blockEntity.storedItem)) {
            blockEntity.burningProgress++;

            if (blockEntity.burningProgress >= MAX_BURNING_PROGRESS) {
                blockEntity.storedItem = new ItemStack(ModItems.BURNED_FOOD.get());
                blockEntity.burningProgress = MAX_BURNING_PROGRESS;
            }

            blockEntity.setChanged();

            if (blockEntity.burningProgress % 2 == 0 || blockEntity.burningProgress >= MAX_BURNING_PROGRESS) {
                blockEntity.sync();
            }
        }
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(
                    worldPosition,
                    getBlockState(),
                    getBlockState(),
                    Block.UPDATE_ALL
            );
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("StoredItem", storedItem.saveOptional(registries));
        tag.putInt("CookingProgress", cookingProgress);
        tag.putInt("BurningProgress", burningProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        storedItem = ItemStack.parseOptional(registries, tag.getCompound("StoredItem"));
        cookingProgress = tag.getInt("CookingProgress");
        burningProgress = tag.getInt("BurningProgress");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}