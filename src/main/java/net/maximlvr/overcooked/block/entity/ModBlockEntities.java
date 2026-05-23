package net.maximlvr.overcooked.block.entity;

import net.maximlvr.overcooked.OverCookedMod;
import net.maximlvr.overcooked.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OverCookedMod.MOD_ID);

    public static final Supplier<BlockEntityType<PanBlockEntity>> PAN_BE =
            BLOCK_ENTITIES.register("pan_be",
                    () -> BlockEntityType.Builder.of(PanBlockEntity::new, ModBlocks.PAN.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}