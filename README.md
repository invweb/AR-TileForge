# AR-TileForge

Procedural AR tile map generator for tabletop strategy games. Generate and place tiles directly in physical space using AR technology.

![Build Status](https://img.shields.io/badge/build-success-brightgreen)
![Android](https://img.shields.io/badge/android-14+-blue)
![Kotlin](https://img.shields.io/badge/kotlin-2.2+-orange)

## Features

### 🌐 AR-Based Tile Placement
- Detects flat surfaces (tables, floors, walls) using ARCore
- Place tiles directly in physical space with natural gestures
- Automatic scaling to match real-world dimensions (5cm per tile)

### 🎲 Procedural Map Generation
- 6 tile types: Plains, Hills, Water, Forest, Mountain, Wasteland
- Smart placement rules for realistic terrain transitions
- Auto-generate starting maps with neighboring tiles

### 🖼️ Minimalist Design
- Clean, flat design style
- Color-coded tile types for instant recognition
- Visual borders for clear tile separation

### 📤 Export to Game Engines
- **JSON**: Coordinate-based map data for any engine
- **PNG**: Sprite sheet atlas with tile mappings
- Ready for import into libGDX, KorGE, Unity, or Godot

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + AndroidX XR Compose
- **AR**: ARCore (Android)
- **Rendering**: Compose 2D (with libGDX option for 3D)
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
- ARCore-supported device
- Android Studio Hedgehog or later

### Build from Source

```bash
# Clone the repository
git clone https://github.com/yourusername/AR-TileForge.git
cd AR-TileForge

# Build the APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

### Required Permissions

The app requires camera access for AR functionality:
- `android.hardware.camera.ar` - ARCore feature
- `android.hardware.camera` - Camera access

## Usage

### 1. Start the App
- Open AR-TileForge
- Allow camera permissions when prompted

### 2. Detect Surface
- Point camera at a flat surface (table, floor, or wall)
- Wait for plane detection confirmation

### 3. Place Tiles
- Select tile type from the palette at bottom
- Tap to place tiles on the detected surface
- Tiles auto-arrange in grid pattern

### 4. Customize
- Change tile type from palette
- Clear entire map with "Clear Map" button
- View tile count and map bounds

### 5. Export
- Tap "Export" to save your map
- Find exported files in `Android/data/com.zx_tole.artileforge/files/exports/`

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
├── MainActivity.kt              # Main entry point with AR integration
├── tile/                        # Tile system
│   ├── TileType.kt             # Enum with tile types
│   ├── TileData.kt             # Data class for tile state
│   ├── TileGenerator.kt        # Procedural generation logic
│   ├── TileRenderer.kt         # Compose renderer for tiles
│   └── ARTilePlane.kt          # AR plane container
├── ui/                          # UI components
│   ├── TilePalette.kt          # Tile type selector
│   └── TileLayerControls.kt    # Clear/Export controls
├── export/                      # Export functionality
│   ├── TileMapSerializer.kt    # JSON serialization
│   └── SpriteSheetExporter.kt  # PNG generation
└── ui/theme/                    # Theme configuration
    └── Color.kt                # Tile color palette
```

## Future Enhancements

### Phase 2 Features
- [ ] ARCore integration for surface detection
- [ ] Drag & drop placement gestures
- [ ] Pinch-to-zoom for map scaling
- [ ] Undo/Redo functionality
- [ ] Advanced placement rules
- [ ] Tile rotation support

### Phase 3 Features
- [ ] 3D rendering with libGDX
- [ ] Procedural terrain textures
- [ ] Lighting and shadows
- [ ] KMP shared logic (Android/iOS/Desktop)
- [ ] Custom tile uploads

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## Support

For support, join our Discord community or open an issue on GitHub.

## Acknowledgments

- ARCore for excellent AR development tools
- Android Compose team for amazing UI framework

---

Made with ❤️ for tabletop gamers and AR enthusiasts
