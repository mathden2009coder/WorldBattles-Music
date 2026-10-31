# WorldBattles Music

Forge 1.20.1 companion mod. It detects active match membership in the supplied WB Arenas 0.4.1 build and sends start/stop packets only to the corresponding player.

## Track assignment
- WorldBattles: **ES_Run! — Christoffer Moe Ditlevsen**
- SandWars: **Desert Horizon — Nolan Nova**
- Zombies: **Fog Choke — Rick Abbott**

Music loops during the ACTIVE phase and stops when the player's UUID is no longer in the active match's player collection. Multiple match instances are checked independently.

## Technical integration
The mod does not patch, replace, or modify WB Arenas or world saves. It reads these private runtime fields reflectively:
- WorldBattlesMode.MATCHES[*].phase and .teams
- SandWarsMode.MATCHES[*].phase and .teams
- WorldBattlesArenas.ARENAS[*].phase and .players

The inspected WB Arenas 0.4.1 classes use ACTIVE for gameplay and UUID-keyed collections for participants. If a future WB Arenas build changes these fields, music detection fails closed and logs a warning.

## Build
GitHub Actions compiles the Java mod with Java 17 / Forge 1.20.1. The three OGG tracks are packaged in the distributed JAR under assets/worldbattlesmusic/sounds/music/. The OGG assets are taken from the corrected audio preparation archive supplied for this project.
