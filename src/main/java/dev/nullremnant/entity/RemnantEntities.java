package dev.nullremnant.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class RemnantEntities {
    public static final EntityType<RemnantEntity> REMNANT = Registry.register(
        Registries.ENTITY_TYPE,
        new Identifier("null_remnant", "remnant"),
        EntityType.Builder.create(RemnantEntity::new, SpawnGroup.MONSTER)
            .setDimensions(0.65f, 1.95f)
            .maxTrackingRange(64)
            .build("remnant")
    );

    private RemnantEntities() {
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(REMNANT, RemnantEntity.createAttributes());
    }
}
