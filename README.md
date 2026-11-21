# NULL: The Remnant

A slow-burn psychological horror mod for **Minecraft Java 1.20.1 / Fabric / Java 17**.

## Premise
A world made to evoke Alpha/Beta Minecraft slowly develops evidence of another presence. The Remnant observes, imitates, and reconstructs the player's habits. It is designed around atmosphere and uncertainty—not constant combat or jump scares.

## Development setup
- Minecraft 1.20.1
- Fabric Loader 0.15.11
- Fabric API 0.92.2+1.20.1
- Java 17
- Gradle / Fabric Loom

Build:
```sh
./gradlew build
```
The resulting jar is in `build/libs/`. A Gradle wrapper is not included yet; use a local Gradle installation compatible with Loom 1.6 or add a wrapper.

## Current prototype
- Fabric entrypoint and server tick event.
- Persistent world state for progression stage, day, event cooldown, and counters.
- Five stage thresholds: day 1–2 calm, day 3–9 awakening, day 10–14 aggression, day 15–20 reality break, day 21+ finale.
- Stage event pools and cooldown check.
- Narrative messages, bedrock cross anomaly, and lore signs.
- A 30% sleep interruption after day 3 (prototype cue; scripted corridor is not implemented yet).

## Safety / player control
Narrative interference is simulated inside the game. The mod does not touch files outside the Minecraft save, modify operating-system settings, collect personal information, or prevent the player from closing the game. Any save-folder ARG files will be opt-in and confined to the current world save.

## Planned modules
1. Remnant entity model, renderer, and observation/mimic AI.
2. Nightmare corridor scene/dimension and door sequence.
3. Client atmosphere controller (fog, audio layers, reversible HUD effects).
4. Memory reconstruction system for selected player actions.
5. Lore archive and optional puzzle files.
6. Finale and multiple endings.
7. Configuration toggles, accessibility options, and test coverage.

## Lore snapshot
The Remnant is an incomplete reconstruction of a previous player, derived from corrupted world/session traces. It doesn't understand leaving a world; it wants to become the player who can return. It learns by copying structures and routines, eventually confusing its reconstruction with ownership.

## License
MIT.
