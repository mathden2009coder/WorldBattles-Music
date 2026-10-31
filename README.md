# WorldBattles Music

Forge 1.20.1 client/server companion mod for per-player match music.

## Current integration

This project inspects the match state in the optional WorldBattles Arenas mod 0.4.x. Music is sent only to players in active matches. Match state is polled server-side; a player leaving an active match loses the track on the next server tick.

Track mapping:
- WorldBattles: ES_Run! — Christoffer Moe Ditlevsen
- SandWars: Desert Horizon — Nolan Nova
- Zombies: Fog Choke — Rick Abbott

## Build

GitHub Actions builds the mod with Java 17 and ForgeGradle. The resulting JAR is uploaded as a workflow artifact. Audio assets are expected at:

- src/main/resources/assets/worldbattlesmusic/sounds/music/worldbattles.ogg
- src/main/resources/assets/worldbattlesmusic/sounds/music/sandwars.ogg
- src/main/resources/assets/worldbattlesmusic/sounds/music/zombies.ogg

The source archive provided for this project contains these OGG files; they will be committed in a separate binary-assets step.

## Important

This is an independent companion mod. It does not patch or replace WB Arenas, and it does not modify world data.
