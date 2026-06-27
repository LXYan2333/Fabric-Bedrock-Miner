# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Current Dev Status

This project is under a full refactor. When I first write this project, I'm a naive beginning programmer, and the architecture of this project is not idea, so do not respect code conventions, naming conventions and code styles of the old project. 

The old project has been moved to old/ folder, and the repo has been replaced to a new fabric mod template.

The Core logic of this mod is a state machine in old/src/main/java/yan/lx/bedrockminer/utils/TargetBlock.java:TargetBlock class. This state machine needs to be refactored to a Kotlin coroutine for better readability and better logic.

# Refactor Goal

- use Fabirc API project when appropriate, do not create wheel

- server side config support
  - allow config through minecraft command
  - persist store config options to disk
  - do not depend on malilib
  - communicate with client mod to achieve these goal:
    - option to disable/enable client Fabric Bedrock Miner mod on server entirely (default enable)
    - allow list and block list for blocks that can/can't be breaked using client mod (and override client's config)
      - an option to control, for block not in allow/block list, block or allow them
      - by default in block mode and block breaking special blocks (listed below)
- client side GUI config interface using [MaLiLib](https://github.com/sakura-ryoko/malilib)  which provides these config options:
  - allow list and block list for blocks that can/can't be breaked using this mod
    - and an option to control, for block not in allow/block list, block or allow them
  - communicate with server, respect server's allow list and block list when play on server
  - works on server without server side install this mode
    - unconditionally add special blocks (listed below) to blocklist (and do not allow override)
      - unless server installed this mod and allow to mine these block
  - config the support block (in old mod, it is hard coded to slime block)
  - hotkey to turn on/off breaking mode.
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

```console
$ ./gradlew build
$ ./gradlew :1.21.1:build
$ ./gradlew :1.21.11:build
$ ./gradlew :26.1.2:build
etc...
```

The project uses **Stonecutter** plus **Fabric Loom**. Stonecutter creates one Gradle subproject per Minecraft version under `versions/`; Loom handles Minecraft dependency resolution, mappings, and mixin processing.

**Active source target**: `26.1.2`. Keep `stonecutter.gradle.kts` on `stonecutter active "26.1.2"` unless the user explicitly asks to switch it. Java runtime for local builds should be `/home/lxyan/.local/jdk/26`; generated mod Java compatibility still varies by Minecraft version in `build.gradle.kts`.

## Adding Minecraft Version Support With Stonecutter

Follow this procedure when adding another Minecraft version:

1. Add the version to `settings.gradle.kts` in the `stonecutter { create(rootProject) { versions(...) } }` list. Keep `vcsVersion` and `stonecutter active` deliberate; this repo currently treats `26.1.2` as the clean active source base.
2. Add a matching table in `stonecutter.properties.toml`. Include:
   - `mod.mc_compat`
   - `mod.mc_releases`
   - `deps.fabric_api`
   - `deps.malilib_mc`
   - `deps.malilib`
   - `deps.mod_menu`
3. Verify dependency coordinates before coding. MaLiLib uses versioned artifact names such as `malilib-fabric-1.21` and `malilib-fabric-1.21.11`, Check metadata with curl when needed, for example:
   ```console
   $ curl -fsSL http://masa.dy.fi/maven/sakura-ryoko/fi/dy/masa/malilib/malilib-fabric-1.21/maven-metadata.xml
   ```
4. Build the new target alone first:
   ```console
   $ ./gradlew :<mc-version>:build
   ```
5. Fix API differences with Stonecutter inline conditions in source:
   ```kotlin
   //? if >=1.21.11 {
   newApi()
   //?} else
   //oldApi()
   ```
6. If the same inline condition appears more than once, abstract it into a compat class instead of duplicating branches. Existing examples:
   - `compat/IdentifierCompat.kt` for `Identifier` vs `ResourceLocation`
   - `compat/CommandCompat.kt` for command permission and ID argument APIs
   - `client/compat/MinecraftClientCompat.kt` for client interaction, packets, inventory slot, and chat API drift
   - `compat/NetworkCompat.kt` for Fabric networking payload registry API drift
7. Do not use Stonecutter `replacements` for Java/Kotlin source API rewrites such as class names, method names, fields, imports, or package names. Source-level differences should be visible as inline conditions or compat classes. The only current `replacements` use is for access widener mapping text: `classTweaker v2 named` -> `classTweaker v2 official` for `26.1+`.
8. Keep version-specific access widener and mixin/resource behavior generated through Stonecutter and Gradle resource expansion. If a mixin JSON has placeholders such as `${mixin_java}`, ensure the relevant `processResources` task expands that file.
9. After the new target builds, run the full build:
   ```bash
   JAVA_HOME=/home/lxyan/.local/jdk/26 PATH=/home/lxyan/.local/jdk/26/bin:$PATH ./gradlew build
   ```
10. Confirm `stonecutter.gradle.kts` still points at the intended active source version before committing.

Important notes:

- Prefer keeping active source code in the newest `26.1.2` API shape and use inline conditions for older versions.
- Avoid changing active-source names just to satisfy an older version; older names belong in the `else` branch or a compat class.
- For Minecraft resource IDs, use `IdentifierCompat` rather than directly importing `Identifier` or `ResourceLocation` in feature code.
- For MaLiLib API drift, inspect `litematica`'s source can be helpful.
- CI uses GitHub Actions with `actions/setup-java` and Oracle JDK 26. Artifact paths are `versions/*/build/libs/*.jar`.

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

- **`BreakingFlowController`** — orchestrates the bedrock-breaking state machine;
- **`TargetBlock`** — per-block state machine managing the piston placement/power/retract cycle
- **`MinecraftClientMixin`** — hooks into `doItemUse` (toggle on/off) and `handleBlockBreaking` (add target blocks)
- **Block-breaking flow**: place piston above bedrock → power with redstone torch → break torch → break piston → place facing-down piston → power again → piston head retracts, breaking the bedrock below

### Dependencies

- `fabric-loader` — mod loader
- `fabric-api` — Fabric API bundle
- `fabric-language-kotlin` — Kotlin stdlib and language adapter for Fabric

## CI

GitHub Actions (`.github/workflows/build.yml`) builds on every push and PR using Oracle JDK 26 on ubuntu-24.04. Artifacts are uploaded from `versions/*/build/libs/*.jar`.
