# NULL: The Remnant

Psychological horror prototype for **Minecraft Java 1.20.1 / Fabric / Java 17**.

## Implemented
- Fabric entrypoint and per-world persistent state.
- Five escalation stages with day thresholds: 0–2, 3–9, 10–14, 15–20, 21+.
- Narrative event pools: whispers, messages, lore signs, bedrock-cross anomaly, sleep interruption.
- Horror director and rare environment anomaly hooks.
- Registered Remnant hostile entity using a temporary vanilla zombie appearance, custom health/speed/damage, target and wander goals, pursuit, gaze reaction, retreat teleport, and despawn.
- Stalker director can spawn the entity at a distance beginning at stage 2.
- Player block-break activity contributes to an in-world memory/awareness counter and can produce rare stage-gated narrative feedback.
- At stage 3+, a persistent memory anchor may produce a sign echo when the player revisits that location.
- First sleep interruption includes a unique nightmare hint; later interruptions escalate at higher stages, with a subtle vanilla sound cue.
- Stalker appearance and nightmare cues use vanilla Enderman audio as a temporary sound placeholder.
- Stage 5 transition records a one-time finale flag and emits a finale introduction.
- Custom humanoid model with idle/walk motion and client renderer (texture currently borrowed from vanilla zombie; model UVs are prototype quality).
- Nearby entity whisper HUD cue, shown only while the Remnant is close.
- Commands: `/remnant lore`; operator-only `/remnant status` and `/remnant spawn`.
- English/Russian language files, event catalog and lore archive.
- JSON configuration file generated in the Minecraft config folder: master enable, nightmare toggle, subtle anomaly toggle, event/stalker frequency sliders (0–200%).
- GitHub Actions build workflow.

## Build
Java 17 and Gradle 8.6:
```sh
gradle build
```
A GitHub Actions workflow runs this on pushes and pull requests. The build has not yet been verified in this session.

## Not yet implemented
- Original custom Remnant model/texture, animations, and sound assets.
- Nightmare corridor or dream scene.
- Client-side fog, post-processing, audio layering, HUD/GUI effects.
- Action memory / structure reconstruction system.
- ARG puzzle chain or optional save-folder artifacts.
- Finale, endings, config UI, accessibility controls, and gameplay tests.

## Boundaries
Horror effects remain in-game. The mod will not read personal files, access the operating system, modify system settings, or prevent the player from exiting. Any future ARG file features will be opt-in and limited to the Minecraft world save.

## Lore
The Remnant is a reconstruction of a player from corrupted world traces. It does not understand leaving; it wants to become the one who returns. It copies routines and structures until imitation becomes ownership.
