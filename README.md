# NULL: The Remnant

Psychological horror prototype for **Minecraft Java 1.20.1 / Fabric / Java 17**.

## Implemented in this repository
- Fabric entry point and world tick event.
- Per-world persistent progression state (day, stage, fear, awareness, intrusion, event timestamps).
- Five escalation stages with day thresholds: 0–2, 3–9, 10–14, 15–20, 21+.
- Stage-specific event pools: whispers, distant footsteps, strange messages, lore signs, bedrock-cross anomaly, sleep interruption.
- Horror director: periodic low-frequency messages and awareness progression.
- Anomaly event hook: rare subtle messages and a very small chance of a nearby stone-to-cobblestone alteration from stage 2 onward.
- English and Russian language files.

## Build
Requires Java 17 and Gradle with Fabric Loom 1.6 available:
```sh
gradle build
```
The repository currently does **not** include a Gradle wrapper. If compilation is successful, the jar is under `build/libs`.

## Prototype limitations
This is not yet a complete playable horror mod. A custom Remnant entity, navigation/AI, renderer, client fog/audio, nightmare corridor, ARG puzzles, dedicated assets, finale, configuration screen, and tests are still not implemented. Source has been committed, but it has not been compiled in this environment; Minecraft/Fabric API compatibility must be verified with a local build.

## Design boundaries
All effects are confined to Minecraft. The mod will not read personal files, access the operating system, alter system settings, or block game exit. Any save-file ARG content will be opt-in and limited to the current world save.

## Lore
The Remnant is a reconstruction of a player from corrupted world traces. It does not understand leaving; it wants to become the one who returns. It copies the player's routines and builds places it mistakes for memories.
