# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Current Dev Status

This project is under a full refactor. When I first write this project, I'm a naive beginning programmer, and the architecture of this project is not idea, so do not respect code conventions, naming conventions and code styles of the old project. 

The old project has been moved to old/ folder, and the repo has been replaced to a new fabric mod template.

The Core logic of this mod is a state machine in old/src/main/java/yan/lx/bedrockminer/utils/TargetBlock.java:TargetBlock class. This state machine needs to be refactored to a Kotlin coroutine for better readability and better logic.

# Refactor Goal

- use Fabirc API project when appropriate, do not create wheel

[//]: # (- server side config support)

[//]: # (  - allow config through minecraft command)

[//]: # (  - persist store config options to disk)

[//]: # (  - do not depend on malilib)

[//]: # (  - communicate with client mod to achieve these goal:)

[//]: # (    - option to disable/enable client Fabric Bedrock Miner mod on server entirely &#40;default enable&#41;)

[//]: # (    - allow list and block list for blocks that can/can't be breaked using client mod &#40;and override client's config&#41;)

[//]: # (      - an option to control, for block not in allow/block list, block or allow them)

[//]: # (      - by default in block mode and block breaking special blocks &#40;listed below&#41;)
- client side GUI config interface using [MaLiLib](https://github.com/sakura-ryoko/malilib)  which provides these config options:
  - allow list and block list for blocks that can/can't be breaked using this mod
    - and an option to control, for block not in allow/block list, block or allow them
  - communicate with server, respect server's allow list and block list when play on server
  - append client local black list to server's black list
  - works on server without server side install this mode
    - unconditionally add special blocks (listed below) to blocklist (and do not allow override)
      - unless server installed this mod and explicitly allow to mine these block
  - config the support block (in old mod, it is hard coded to slime block)
  - config hold and click break mode (in old mod, it is hard coded to hold to break)
  - hotkey to turn on/off breaking mode.
  - checkbox to provide enable legacy right click bedrock on/off behaviour (default off)
- better bedrock breaking flow control
  - allow breaking block from all directions
  - take care of piston's quasi-connectivity
  - support [Carpet Extra](https://github.com/gnembon/fabric-carpet) accurate block placement protocol
    - when server has Carpet Extra: can place piston facing any direction without tick delay
    - on vanilla server: by default only support placing piston facing UP/DOWN (pitch takes effect immediately via `xRot`; horizontal facing requires `yHeadRot` which lags by one tick — see `doc/related minecraft quirk.md`)
- [Mod Menu](https://github.com/TerraformersMC/ModMenu) integration
- English and Simplified Chinese i18n support

Special blocks:

- barrier
- all variant of command block
- structure block
- structure void
- jigsaw block

## Known Minecraft Quirks

See `doc/related minecraft quirk.md` for detailed write-ups. Key quirk affecting design:

- **yHeadRot update delay**: `Direction.orderedByNearest` uses `getViewYRot()` which reads `yHeadRot`, updated only in `LivingEntity#aiStep`. When sending `ServerboundMovePlayerPacket.Rot` to fake player look direction for piston placement, the yaw change does NOT take effect until the player entity ticks once on the server. Pitch changes (via `xRot`) are immediate. This limits vanilla-server piston placement to UP/DOWN only, unless a one-tick delay or Carpet Extra protocol is used.

## Build & Development

```bash
./gradlew build          # Build the mod (runs tests, outputs to build/libs/)
./gradlew runClient      # Launch Minecraft client with the mod for testing
./gradlew runServer      # Launch Minecraft server with the mod
```

The project uses **Fabric Loom** (`net.fabricmc.fabric-loom`) as the Gradle plugin for Minecraft modding. Loom handles Minecraft dependency resolution, mappings, and mixin processing.

**Target**: Minecraft `26.1.2` (snapshot), Java 25, Kotlin 2.4.0. Version properties live in `gradle.properties`.

## Architecture

This is a **Minecraft Fabric mod** that automates breaking bedrock blocks via piston/redstone-torch mechanics. It's currently being rewritten from the old Java codebase (`old/`, gitignored) into a new Kotlin+Java scaffold.

### Source sets

Loom uses split environment source sets (`loom.splitEnvironmentSourceSets()`):

| Source set | Purpose |
|---|---|
| `src/main/` | Shared code (client + server) |
| `src/client/` | Client-only code |

Both source sets contain Kotlin and Java code — Kotlin is the primary language, Java is used for Mixin classes only.

### Entrypoints (`fabric.mod.json`)

- **`com.github.lxyan2333.bedrockminer.BedrockMiner`** (Kotlin `object`, implements `ModInitializer`) — main mod init
- **`com.github.lxyan2333.bedrockminer.client.BedrockMinerClient`** (Kotlin `object`, implements `ClientModInitializer`) — client-side init

### Mixins

Mixins are the mechanism for injecting into Minecraft's code at runtime. Two configs:

- `src/main/resources/bedrock-miner.mixins.json` — common mixins (package: `com.github.lxyan2333.bedrockminer.mixin`)
- `src/client/resources/bedrock-miner.client.mixins.json` — client-only mixins (package: `com.github.lxyan2333.bedrockminer.client.mixin`)

Mixin classes are written in **Java** (required by the Mixin annotation processor) even though business logic is in Kotlin. Mixins must be registered in their JSON config files to take effect.

### Old codebase reference (`old/`)

The `old/` directory contains the previous working implementation in Java under the `yan.lx.bedrockminer` package. It's gitignored but kept for reference during the rewrite. Key design from old code:

- **`BreakingFlowController`** — orchestrates the bedrock-breaking state machine; toggled on/off via right-clicking bedrock with empty hand
- **`TargetBlock`** — per-block state machine managing the piston placement/power/retract cycle
- **`MinecraftClientMixin`** — hooks into `doItemUse` (toggle on/off) and `handleBlockBreaking` (add target blocks)
- **Block-breaking flow**: place piston above bedrock → power with redstone torch → break torch → break piston → place facing-down piston → power again → piston head retracts, breaking the bedrock below

### Dependencies

- `fabric-loader` — mod loader
- `fabric-api` — Fabric API bundle
- `fabric-language-kotlin` — Kotlin stdlib and language adapter for Fabric

## CI

GitHub Actions (`.github/workflows/build.yml`) builds on every push and PR using Java 25 on ubuntu-24.04. Artifacts are uploaded from `build/libs/`.