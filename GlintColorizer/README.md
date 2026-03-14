# Glint Colorizer — Minecraft 1.8.9 Forge Mod

Customize the **enchantment glint** color on every item in the game.  
Three render modes, fully configurable via `config/glintcolorizer.cfg`.

---

## Features

| Mode      | Description                                       |
|-----------|---------------------------------------------------|
| `custom`  | Solid RGBA color you define (default: blue tint)  |
| `rainbow` | Smooth hue-cycle animation                        |
| `default` | Restores vanilla purple glint                     |

---

## Installation

1. Install **Minecraft Forge 1.8.9** (recommended build: `11.15.1.2318`)
2. Drop `GlintColorizer-1.0.0.jar` into your `.minecraft/mods/` folder
3. Launch the game — a config file is generated automatically at  
   `.minecraft/config/glintcolorizer.cfg`

---

## Building from source

Prerequisites: **JDK 8**, internet connection (Gradle downloads Forge MCP)

```bash
# 1 – Clone / extract the project
cd GlintColorizer

# 2 – Set up the Forge workspace (downloads Minecraft assets ~300 MB)
./gradlew setupDecompWorkspace

# 3 – Build the mod jar
./gradlew build

# Output: build/libs/GlintColorizer-1.0.0.jar
```

> On Windows use `gradlew.bat` instead of `./gradlew`.

---

## Configuration (`glintcolorizer.cfg`)

```properties
# ── General ──────────────────────────────────────────────────────────────────
# Enable or disable the mod entirely (true/false)
B:enabled=true

# Render mode: default | custom | rainbow
S:renderMode=custom

# ── Color ────────────────────────────────────────────────────────────────────
# RGB channels (0-255)
I:glintRed=100
I:glintGreen=0
I:glintBlue=255

# Opacity of the glint overlay (0.0-1.0)
S:glintAlpha=0.5

# ── Animation ────────────────────────────────────────────────────────────────
# Rainbow speed multiplier (0.1-10.0)
S:glintSpeed=1.0
```

---

## How it works (technical)

The mod uses a **Forge CoreMod / ASM transformer** (`GlintLoadingPlugin` →  
`GlintTransformer`) to patch `net.minecraft.client.renderer.RenderItem#renderEffect`  
at class-load time.

The vanilla `GlStateManager.color(0.5, 0.25, 0.8, 1.0)` call is replaced with:

```java
GlStateManager.color(
    GlintRenderer.getRed(),
    GlintRenderer.getGreen(),
    GlintRenderer.getBlue(),
    GlintRenderer.getAlpha()
);
```

`GlintRenderer` reads live values from `GlintConfig` and, in rainbow mode,
advances an HSB hue each client tick via `GlintEventHandler`.

---

## License

MIT — free to use, modify, and redistribute.
