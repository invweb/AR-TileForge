# AR-TileForge

Procedural tile map generator for tabletop strategy games. Create and edit grid-based maps with 6 terrain types, then export for use in game engines.

![Build Status](https://img.shields.io/badge/build-success-brightgreen)
![Android](https://img.shields.io/badge/android-14+-blue)
![Kotlin](https://img.shields.io/badge/kotlin-2.2+-orange)

## ✨ Features

### 🎲 Procedural Map Generation
- 6 tile types: Plains, Hills, Water, Forest, Mountain, Wasteland
- Auto-generate starting maps with a central tile and 4 neighbors
- Configurable placement rules (coming soon)

### 🎯 Interactive Tile Editor
- **Tap to Select**: Choose tile type from palette at bottom (white border highlights selection)
- **Tap to Place**: Tap empty gray cells to place a new tile
- **Tap to Change**: Tap an existing tile to change it to the selected type
- **Instant Feedback**: Tiles highlight green when their type matches the current selection
- **Pan & Zoom**: Pinch-to-zoom and drag-to-pan for large maps
- **Rotation Mode**: Toggle rotation ON/OFF — in rotation mode, tapping rotates tiles 90°
- **Undo/Redo**: Full action history with Undo/Redo buttons
- **Save/Load**: Save project as JSON file and restore it later via file picker
- **Clear Map**: One-tap cleanup with toast confirmation

### 📤 Export to Game Engines
- **JSON**: Coordinate-based map data with bounds — importable into libGDX, KorGE, Unity, Godot
- **PNG**: Sprite sheet atlas with labeled tile types, transparent background

### 🖼️ Minimalist Design
- Clean, flat design style
- Color-coded tile types for instant recognition
- Visual borders for clear tile separation

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose (Material 3)
- **Rendering**: Compose 2D
- **Export**: JSON + PNG generation

## Tile Types

| Type | Color | Description |
|------|-------|-------------|
| Plains | `#A8E6CF` | Standard grass terrain |
| Hills | `#D5E1D6` | Elevated ground |
| Water | `#89CFF0` | Rivers, lakes, ponds |
| Forest | `#567d46` | Dense tree coverage |
| Mountain | `#8B7355` | High elevation terrain |
| Wasteland | `#E8D4B8` | Barren, arid land |

## Installation

### Prerequisites
- Android 14+ (API 34+)
- Android Studio Hedgehog or later

### Build from Source

```bash
# Clone the repository
git clone https://github.com/invweb/AR-TileForge.git
cd AR-TileForge

# Build the APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Usage

### 1. Start the App
- Open AR-TileForge — a starting map is generated automatically with a Plains center tile and 4 random neighbors

### 2. Edit the Map
- **Select a tile type** from the palette at the bottom (the selected button shows a **white border**)
- **Place a tile**: Tap an empty gray cell in the grid
- **Change a tile**: Tap any placed tile — it updates to the selected type
  - If the tile already matches the selected type, it highlights with a **green border** as confirmation
- **Rotate tiles**: Toggle the Rotate button in the top bar to ON, then tap tiles to rotate them 90°
- **Pan & Zoom**: Pinch to zoom in/out, drag to pan across the map
- **Undo/Redo**: Use the back/forward arrow buttons to undo or redo changes
- **Save/Load**: Tap Save to download a JSON project file; tap Load to pick a JSON file and restore your map
- **Clear the map**: Tap the "Clear Map" button (toast confirmation appears)
- **Export**: Tap "Export" to save the current map as JSON + PNG

### 3. Export
- Files are saved to `Android/data/com.zx_tole.artileforge/files/exports/`

### 4. Save / Load
- **Save**: Tap the Save button (disk icon) to download the current map as a JSON file via the system file picker
- **Load**: Tap the Load button (download icon), select a previously saved JSON file — your map is restored with full undo/redo history cleared and rebuilt
- JSON filename: `tilemap_<timestamp>.json`
- PNG filename: `tilesprite_<timestamp>.png`

## Export Format

### JSON Export
```json
{
  "version": "1.0",
  "tileSizeCm": 5.0,
  "tiles": [
    {
      "type": "Plains",
      "x": 0,
      "y": 0,
      "rotation": 0
    },
    {
      "type": "Water",
      "x": 1,
      "y": 0,
      "rotation": 0
    }
  ],
  "bounds": {
    "minX": 0,
    "maxX": 5,
    "minY": 0,
    "maxY": 3
  }
}
```

### PNG Sprite Sheet
- 64x64 pixel tiles
- Atlas format with labeled types
- Transparent background for overlay use

## Project Structure

```
app/src/main/java/com/zx_tole/artileforge/
├── MainActivity.kt              # Main entry point & tile grid UI
├── tile/                        # Tile system
│   ├── TileType.kt             # Enum with 6 terrain types + colors
│   ├── TileData.kt             # Data class: type, coordinates, rotation
│   ├── TileGenerator.kt        # Procedural generation + placement rules
│   └── TileRenderer.kt         # Compose renderer with rotation support
├── ui/                          # UI components
│   ├── TilePalette.kt          # Tile type selector with visual feedback
│   ├── TileLayerControls.kt    # Clear & Export buttons
│   └── theme/                  # Material 3 theme configuration
└── export/                      # Export functionality
    ├── TileMapSerializer.kt    # JSON serialization
    └── SpriteSheetExporter.kt  # PNG sprite sheet generation
```

## Roadmap

### ✅ Completed
- [x] Interactive tile editor with tap-to-place / tap-to-change
- [x] Visual feedback (selection highlight, type-match green border)
- [x] 6 terrain types with minimalist flat design
- [x] JSON + PNG export with toast notifications
- [x] Procedural map generation (center tile + 4 neighbors)
- [x] `TileRenderer` with rotation support
- [x] Undo / Redo (action history with stack)
- [x] Drag-to-pan and pinch-to-zoom for large maps
- [x] Save / Load project files (JSON import/export)
- [x] Tile rotation mode (toggle, click to rotate 90°)
- [x] Placement rules (Forests near Water, Mountains chained, Hills near Mountains)

### 🟡 Next Priority
- [ ] Rotate tiles via long-press
- [ ] Snapping grid / visual guide lines
- [ ] Multiple map support (tabs or list)
- [ ] Tile duplication via copy/paste
- [ ] Placement rules toggle UI (enable/disable per rule)

### 🔮 Future
- [ ] 3D rendering with libGDX / AndroidXR
- [ ] Procedural terrain textures & lighting
- [ ] KMP shared logic (Android / iOS / Desktop)
- [ ] Custom tile image uploads (PNG assets)
- [ ] ARCore surface detection & world-scale placement

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Acknowledgments

- Android Compose team for the amazing declarative UI framework

---

Made with ❤️ for tabletop gamers
