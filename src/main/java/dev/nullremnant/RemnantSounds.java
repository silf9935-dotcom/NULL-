package dev.nullremnant;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class RemnantSounds {
    public static final SoundEvent AMBIENT = register("remnant_ambient");
    public static final SoundEvent HURT = register("remnant_hurt");
    public static final SoundEvent DEATH = register("remnant_death");
    public static final SoundEvent PRESENCE = register("remnant_presence");
    public static final SoundEvent GLITCH = register("remnant_glitch");
    private RemnantSounds() {}
    private static SoundEvent register(String path) {
        Identifier id = new Identifier(RemnantMod.ID, path);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }
    public static void register() { RemnantMod.LOGGER.info("Registered Remnant sound events."); }
}
