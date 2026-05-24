package net.maximlvr.overcooked.item;

import net.maximlvr.overcooked.OverCookedMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OverCookedMod.MOD_ID);

    public static final DeferredItem<Item> PATTY_UNCOOKED = ITEMS.register("patty_uncooked",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> PATTY_COOKED = ITEMS.register("patty_cooked",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BURNED_FOOD = ITEMS.register("burned_food",
            () -> new Item(new Item.Properties()));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
