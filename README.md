# ParticleOpacity

A client-side Fabric mod project for Minecraft Java Edition **26.1.2**.

## Target environment

- Minecraft: `26.1.2`
- Fabric Loader: `0.20.1`
- Java toolchain / bytecode release: `25`
- Fabric API: `0.155.3+26.1.2`
- Mod Menu: `18.0.2`
- No Cloth Config, YACL, or external GUI library

## Features in this source project

- Mod Menu config button opens a vanilla-widget settings screen.
- Independent block and food/item master toggles.
- Independent 0–100% sliders (default 50%).
- Searchable registry lists with per-entry ON/OFF toggles and Enable All / Disable All.
- JSON persistence at `config/particleopacity.json`.
- Captures the `BlockState` from `TerrainParticle` constructors.
- Tracks edible `ItemStackTemplate` particle sources through the 26.1.2 `BreakingItemParticle.ItemParticleProvider` sprite lookup.
- Switches matching particles to the 26.1.2 `SingleQuadParticle.Layer.TRANSLUCENT_TERRAIN` or `TRANSLUCENT_ITEMS` layer.
- Applies alpha only during `SingleQuadParticle.extract`, then restores the original alpha field.

## Build

This source archive is intended to be compiled using GitHub Actions. The workflow will install Java 25 and Gradle on a hosted runner, so no local Java or Gradle installation is needed.

## Build on GitHub (no local Java/Gradle installation)

Upload the contents of this folder to the root of your GitHub repository, so `build.gradle` is at the repository root. Then create the workflow file `.github/workflows/build.yml` in GitHub. The workflow installs Java 25 and Gradle, runs `gradle build`, and uploads the compiled JAR as an Actions artifact.

The source archive intentionally does not include a GitHub Actions workflow; add it as a separate file after uploading the source. The supplied project has not yet been compiled in this workspace. A successful GitHub build is still required, followed by an in-game launch test with the exact Fabric, Mod Menu, and Sodium versions.

## Important compatibility notes

Minecraft 26.1.2 uses Mojang's unobfuscated class and method names and has a changed particle API. In this version the item-shard particle is `BreakingItemParticle` (not the older `CrackParticle` name), and particle pass selection is performed through `getLayer()` / `SingleQuadParticle.Layer`, not the older `ParticleTextureSheet` / `getType()` API. This project targets those 26.1.2 APIs.

The Sodium strategy is deliberately renderer-neutral: the particle's own vanilla extraction method writes the opacity into the shared quad render state, and the layer is a vanilla translucent layer. The source does not reference Sodium internals or custom vertex-writer classes. That is a compatibility design, **not a claim of verified 100% runtime compatibility**: a launch and visual smoke test with the exact Sodium build should still be performed before release.

The item-source association uses the sprite instance returned immediately before the breaking-item particle is created. This follows the 26.1.2 `ItemParticleProvider` path; if another mod changes that provider flow, that mod may need a small compatibility hook.

## Build status in the supplied workspace

This source archive is provided as a Gradle project. The build environment used to prepare it only has Java 21 and no installed Gradle distribution, and it could not reach the dependency hosts. Therefore a compiled production `.jar` could not honestly be produced or runtime-validated in this session. Use a local JDK 25 machine with internet access and run the commands above to compile it. Any compiler or Mixin errors from the exact current Fabric/Minecraft artifacts should be fixed before putting the resulting jar into a live profile.
