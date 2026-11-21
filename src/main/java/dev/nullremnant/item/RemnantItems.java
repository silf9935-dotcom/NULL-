package dev.nullremnant.item;

import dev.nullremnant.entity.RemnantEntities;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Registers the mod's items and its dedicated creative inventory tab.
 */
public final class RemnantItems {
    public static final Item REMNANT_SPAWN_EGG = Registry.register(
        Registries.ITEM,
        new Identifier("null_remnant", "remnant_spawn_egg"),
        new SpawnEggItem(RemnantEntities.REMNANT, 0x17151F, 0x8D1B2D, new Item.Settings())
    );

    public static final ItemGroup ITEM_GROUP = Registry.register(
        Registries.ITEM_GROUP,
        new Identifier("null_remnant", "main"),
        FabricItemGroup.builder(new Identifier("null_remnant", "main"))
            .displayName(Text.translatable("itemGroup.null_remnant"))
            .icon(() -> new ItemStack(REMNANT_SPAWN_EGG))
            .entries((context, entries) -> entries.add(REMNANT_SPAWN_EGG))
            .build()
    );

    private RemnantItems() {
    }

    public static void register() {
        // Also expose the egg in vanilla's spawn egg tab for convenient discovery.
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
            .register(entries -> entries.add(REMNANT_SPAWN_EGG));
    }
}
