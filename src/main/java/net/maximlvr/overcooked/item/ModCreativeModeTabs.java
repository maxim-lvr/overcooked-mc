package net.maximlvr.overcooked.item;

import net.maximlvr.overcooked.OverCookedMod;
import net.maximlvr.overcooked.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, OverCookedMod.MOD_ID);


    public static final Supplier<CreativeModeTab> CORONA_ITEMS_TAB = CREATIVE_MODE_TAB.register("overcooked_item_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.PAN.get()))
                    .title(Component.translatable("creativetab.asmpthingsmod.corona_items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.PAN);
                        output.accept(ModBlocks.BURNER);
                        output.accept(ModBlocks.TRASH);

                    }).build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
