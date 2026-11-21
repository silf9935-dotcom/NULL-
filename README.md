# NULL: The Remnant

A psychological-horror survival mod for **Minecraft Java 1.20.1**, **Fabric**, and **Java 17**.

The Remnant is an imitation of a player reconstructed from traces left in a world. It observes, remembers, and gradually learns to reproduce the player's routines. Horror effects are deliberately in-game: the mod does not inspect personal files, access the operating system, modify system settings, or interfere with the ability to exit the game.

## Current gameplay

### World progression

The world advances through five narrative stages. The internal stage values are zero-based; the player-facing stage numbers are one-based.

| Player-facing stage | Internal value | World age | Behavior |
| --- | ---: | --- | --- |
| Quiet | 0 | Days 0–2 | Sparse environmental hints; no entity stalking. |
| Awakening | 1 | Days 3–9 | Distant sounds, first traces, and early memory feedback. |
| Aggression | 2 | Days 10–14 | The Remnant can begin appearing behind environmental cover; it stalks rather than attacks. |
| Fracture | 3 | Days 15–20 | More frequent anomalies, remembered places, and non-damaging shadow manifestations. |
| Finale | 4 | Day 21 onward | The Remnant becomes more persistent and can attack; world-memory echoes intensify. |

Progression is based on the world's age in ticks, not the player's sleeping count. Sleeping can trigger a separate nightmare interruption, but does not skip progression days.

### The Remnant

- Registered server entity with a custom humanoid model and a dedicated remnant.png texture.
- It can spawn at a distance in a dark location hidden from the selected player's direct line of sight.
- Before the finale, it follows and watches without initiating melee attacks.
- Sustained direct eye contact makes it retreat; it tries to move behind actual block cover and becomes visible again after its retreat window.
- At the finale stage, it can use melee attacks.
- It has custom Enderman-like sound cues, does not burn in daylight, does not drop loot, and is removed after a long period without a target.
- Retreat timers, target-relative retreat coordinates, and lifetime tracking are saved with the entity.

### Memory and environmental events

- Block-breaking activity increases world intrusion and awareness.
- Selected ordinary solid blocks can be remembered and later reproduced once as a small, one-block imitation.
- Revisiting remembered chunk areas can trigger narrative echoes.
- Repeated door interactions are tracked and can produce a one-time sign echo.
- A short-lived smoke shadow can appear behind a player in dark conditions and advance a few times toward them; it does not collide or deal damage.
- Rare smoke manifestations and cave-audio cues provide distant-presence events without permanently changing terrain.
- Sleep interruptions only target players who are actually sleeping.
- Event and stalker cooldowns are stored separately, and persistent state is saved per world dimension.

### Client presentation

- Custom entity renderer and model.
- Proximity HUD whisper while the Remnant is nearby.
- Restrained proximity audio cue.
- English and Russian language resource files.

### Commands

- /remnant help — show available commands.
- /remnant lore — read the current chapter.
- /remnant status — operator-only world and entity status.
- /remnant spawn — operator-only safe test spawn near the command sender.
- /remnant despawn — operator-only removal of Remnant entities in the current dimension.
- /remnant reload — operator-only reload of the JSON configuration.

### Configuration

The file config/null_remnant.json is created on first launch.

| Key | Type | Default | Meaning |
| --- | --- | ---: | --- |
| enabled | boolean | true | Master switch for server-side event and memory systems. |
| nightmares | boolean | true | Enable sleep interruptions. |
| subtleAnomalies | boolean | true | Enable ambient anomaly events. |
| eventFrequency | integer | 100 | Event frequency percentage, clamped to 0–200. |
| stalkerFrequency | integer | 100 | Remnant entity spawn frequency percentage, clamped to 0–200. |

Use /remnant reload to reload the configuration without restarting the game. A value of 0 suppresses the corresponding random event director.

## Build

Requirements: Java 17, Gradle 8.6, Minecraft 1.20.1 mappings and Fabric dependencies declared in gradle.properties.

Run:

~~~sh
gradle build
~~~

The GitHub Actions workflow runs gradle build on pushes to main and pull requests targeting main. Check the Actions tab for the latest verified build result.

## Testing checklist

- [ ] Launch a Fabric 1.20.1 client and verify the model and custom texture.
- [ ] Create a new survival world and verify progression across all five stages.
- [ ] Verify that stage 2 and 3 stalking does not deal damage.
- [ ] Verify gaze detection, retreat, line-of-sight cover, reappearance, and eventual despawn.
- [ ] Save and reload while the entity is retreating; verify it does not remain permanently invisible.
- [ ] Verify that stage 4 enables melee behavior.
- [ ] Verify that signs and smoke events do not overwrite existing player blocks.
- [ ] Verify that remembered blocks are reproduced only on safe, empty, supported positions.
- [ ] Verify that world state and cooldowns persist across server restarts.
- [ ] Test multiplayer targeting, sleep interruptions, and command permissions.
- [ ] Test each configuration option, including zero and maximum event frequencies.

## Design boundaries

All horror effects remain inside Minecraft. The mod does not read personal files, access the operating system, modify system settings, or prevent the player from leaving the game. Any future ARG-like file features must be explicitly opt-in and confined to the Minecraft world save.

## Lore

The Remnant is a reconstruction of a player from corrupted world traces. It does not understand leaving; it wants to become the one who returns. It copies routines and structures until imitation becomes ownership.
