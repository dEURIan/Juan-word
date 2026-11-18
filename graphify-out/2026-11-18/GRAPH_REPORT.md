# Graph Report - modest-borg  (2026-11-16)

## Corpus Check
- cluster-only mode — file stats not available

## Summary
- 1023 nodes · 2204 edges · 142 communities (29 shown, 113 thin omitted)
- Extraction: 92% EXTRACTED · 8% INFERRED · 0% AMBIGUOUS · INFERRED: 184 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `0e0bac82`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- BattlePanel
- ui/BattlePanel.java
- awt
- JuanWordApp
- TileComponent
- Landmark
- Player
- DataManager
- file
- data/DataManager.java
- DataManager
- TestJuanAvatarPanel
- DataManager
- JuanWord
- Enemy
- DifficultySetting
- MapSelectPanel
- CheckBattleLayout.java
- StartPanel
- CaptureAllMapStates.java
- BattleContext
- TestDesignPatterns.java
- BattleSessionBuilder
- .createEnemy
- CsvUtil.java
- TileGridGenerator
- TestMapLayoutPreview
- TestFullscreenAndResponsiveScaling
- TestRegressions
- imageio
- .getExactBBox
- .renderScene
- Screen
- for skeleton additional classes/DataManager.java
- BufferedImage
- AlviolaBossEnemy.java
- DizonBossEnemy.java
- GuardEnemy.java
- KarabawEnemy.java
- LolaEnemy.java
- MaleStudentEnemy.java
- MonkeyEnemy.java
- PaintingEnemy.java
- TestInteractiveMapScreen.java
- .createSilhouette
- CalculateMaxScale
- CheckCompPixels.java
- CheckLockRender.java
- CheckSprites.java
- CompareBanners.java
- CompareLockOptions.java
- ComparePreview.java
- CropCheck.java
- CropExactLocks.java
- DiffImages.java
- FindEnemyBounds.java
- FindExactMatch.java
- FindYellowPixels.java
- InspectBlueBuilding.java
- InspectBounds.java
- InspectButtonAssets.java
- InspectImage.java
- InspectLandmarkSprites.java
- InspectLayers.java
- InspectMapAssets.java
- InspectMenuComposite.java
- InspectMockupLayout.java
- InspectPreviewTitle.java
- InspectTileContent.java
- InspectTitleBounds.java
- InspectTitlePage.java
- MakeComparison.java
- MakeMapTransitionComparison.java
- MeasureBorder.java
- MeasureMockupLandmarks.java
- PrototypeTileAndBgFix.java
- RenderButtonCollage.java
- RenderCloseup.java
- RenderSpritesCheck.java
- TestBannerAndLock.java
- TestButtonSizeVisuals
- TestClipBypass.java
- TestCombineLayers.java
- TestHitTestPerformance.java
- TestLandmarkCropping.java
- TestLayoutOptions
- TestLetterCheck.java
- TestLockRender.java
- TestLockScreenRender.java
- TestMapBannerAndLockVisuals.java
- TestMapCompositeMatch.java
- TestMenuComposition.java
- TestPerfectComposition.java
- TestScreenBanners.java
- TestSilhouette.java
- VerifyCompositeActual.java
- VerifyNoYellowCircle.java
- load-memory.sh
- TestExpandAndBattleComposite
- LetterSpec.java
- add
- atomicboolean
- atomicinteger
- concurrent
- ImageAssetButton
- java.awt.Container
- JDialog
- method
- return
- Color
- DataManager
- Graphics
- Graphics2D
- Image
- JButton
- JLabel
- JuanWord
- Player
- Timer
- Player
- Graphics2D
- Override
- DifficultySetting
- CardLayout
- DataManager
- Graphics
- Image
- JButton
- JLabel
- JPanel
- Override
- Player
- Rectangle
- Color
- Graphics
- Graphics2D
- Image
- JuanWord
- Override
- Rectangle
- Timer
- Override

## God Nodes (most connected - your core abstractions)
1. `Player` - 54 edges
2. `BattleContext` - 49 edges
3. `BattlePanel` - 48 edges
4. `DifficultySetting` - 46 edges
5. `Enemy` - 37 edges
6. `DataManager` - 36 edges
7. `BattleSession` - 29 edges
8. `JuanWordApp` - 27 edges
9. `TileComponent` - 27 edges
10. `JuanWord` - 25 edges

## Surprising Connections (you probably didn't know these)
- `Enemy` --references--> `AttackType`  [EXTRACTED]
  src/juanword/enemy/Enemy.java → src/juanword/model/AttackType.java
- `BattleContext` --references--> `BattleSession`  [EXTRACTED]
  src/juanword/model/BattleContext.java → src/juanword/model/BattleSession.java
- `BattleSession` --references--> `Enemy`  [EXTRACTED]
  src/juanword/model/BattleSession.java → src/juanword/enemy/Enemy.java
- `BattleSession` --references--> `DifficultySetting`  [EXTRACTED]
  src/juanword/model/BattleSession.java → src/juanword/model/DifficultySetting.java
- `BattleSession` --references--> `Player`  [EXTRACTED]
  src/juanword/model/BattleSession.java → src/juanword/model/Player.java

## Import Cycles
- None detected.

## Communities (142 total, 113 thin omitted)

### Community 0 - "BattlePanel"
Cohesion: 0.05
Nodes (16): TestEnemyDeathAndEntranceAnimation, TestJuanAttackAnimation, Override, AttackType, PUNCH, SLASH, BattleOutcome, DEFEAT (+8 more)

### Community 1 - "ui/BattlePanel.java"
Cohesion: 0.05
Nodes (49): affinetransform, alphacomposite, arraydeque, arraylist, basicstroke, BattlePanel, BattleSession, BattleSessionBuilder (+41 more)

### Community 2 - "awt"
Cohesion: 0.05
Nodes (26): awt, countdownlatch, field, heroanimstate, JuanWord, juanword.BattlePanel.JuanAvatarPanel, CaptureBattlePreview, CaptureBattlePreviewWithNewTiles (+18 more)

### Community 3 - "JuanWordApp"
Cohesion: 0.07
Nodes (27): BattlePanel, Color, Graphics, JButton, JLabel, Override, SpritePanel, BestiaryPanel (+19 more)

### Community 4 - "TileComponent"
Cohesion: 0.08
Nodes (10): TestDesignPatterns, GoldTileDecorator, Override, HealingTileDecorator, Override, Override, StandardTileComponent, TileComponent (+2 more)

### Community 5 - "Landmark"
Cohesion: 0.08
Nodes (7): java.awt.Rectangle, TestLoadLetterAssets, TestMapProgressionAndTransition, TestPerDifficultyBossUnlocks, ImageLoader, Landmark, Override

### Community 7 - "DataManager"
Cohesion: 0.14
Nodes (4): EnemySpec, juanword.model.Player, LetterSpec, DataManager

### Community 8 - "file"
Cohesion: 0.12
Nodes (7): file, java.awt.image.BufferedImage, CreateCollage, InspectMainCharSprites, MakeMapOverlapComparison, TestEnemyVisuals, TestSpriteHitAndTransitions

### Community 9 - "data/DataManager.java"
Cohesion: 0.13
Nodes (15): arrays, collections, files, hashmap, hashset, ioexception, juanword.model.BattleSession, linkedhashmap (+7 more)

### Community 10 - "DataManager"
Cohesion: 0.18
Nodes (3): DataManager, Override, Player

### Community 11 - "TestJuanAvatarPanel"
Cohesion: 0.15
Nodes (10): Graphics, Image, Override, HeroAnimState, ATTACK, IDLE, PRE_ATTACK, PrototypeJuanAnimation (+2 more)

### Community 12 - "DataManager"
Cohesion: 0.23
Nodes (7): DataManager, DifficultySetting, enemyfactory, MapSelectPanel, point, TestMapSelectIntegration, EnemySpec

### Community 13 - "JuanWord"
Cohesion: 0.20
Nodes (7): java.awt.CardLayout, javax.swing.JFrame, javax.swing.JPanel, Screen, JuanWord, windowadapter, windowevent

### Community 15 - "DifficultySetting"
Cohesion: 0.16
Nodes (6): PorterBossEnemy, DifficultySetting, EXTREME, LISOD, SAKTO, SAYON

### Community 17 - "CheckBattleLayout.java"
Cohesion: 0.24
Nodes (6): Container, java.awt.Component, JFrame, CheckBattleLayout, JPanel, TestMapExpandAndTileCentering

### Community 18 - "StartPanel"
Cohesion: 0.33
Nodes (3): javax.swing.JButton, Override, StartPanel

### Community 19 - "CaptureAllMapStates.java"
Cohesion: 0.17
Nodes (6): juanword.JuanWord, mouseevent, CaptureAllMapStates, RenderBothScreens, TestEntranceVisual, swingutilities

### Community 20 - "BattleContext"
Cohesion: 0.20
Nodes (3): JanitorBossEnemy, Override, BattleContext

### Community 21 - "TestDesignPatterns.java"
Cohesion: 0.18
Nodes (6): atomicreference, executors, executorservice, AltarBoyEnemy, Override, standardtilecomponent

### Community 22 - "BattleSessionBuilder"
Cohesion: 0.20
Nodes (3): CheckExactComponentBounds, TestBorderContainment, BattleSessionBuilder

### Community 23 - ".createEnemy"
Cohesion: 0.22
Nodes (3): TestBlueBuildingProgression, TestFourStageProgression, EnemyFactory

### Community 24 - "CsvUtil.java"
Cohesion: 0.24
Nodes (6): atomicmovenotsupportedexception, bufferedreader, bufferedwriter, java.nio.charset.Charset, CsvUtil, standardcopyoption

### Community 26 - "TestMapLayoutPreview"
Cohesion: 0.36
Nodes (4): Color, Graphics2D, Image, TestMapLayoutPreview

### Community 27 - "TestFullscreenAndResponsiveScaling"
Cohesion: 0.39
Nodes (3): Component, JComponent, TestFullscreenAndResponsiveScaling

### Community 29 - "imageio"
Cohesion: 0.29
Nodes (3): imageio, AsciiBottomRight, InspectCropOriginal

### Community 30 - ".getExactBBox"
Cohesion: 0.38
Nodes (3): Rectangle, CropAlphabet, InspectAlphabet

### Community 31 - ".renderScene"
Cohesion: 0.48
Nodes (3): Graphics2D, Image, PrototypeMapScreen

### Community 32 - "Screen"
Cohesion: 0.33
Nodes (4): Screen, BATTLE, MAP_SELECT, START

### Community 33 - "for skeleton additional classes/DataManager.java"
Cohesion: 0.40
Nodes (4): datetimeformatter, localdatetime, standardcharsets, util

## Knowledge Gaps
- **17 isolated node(s):** `LetterSpec`, `PUNCH`, `SLASH`, `DEFEAT`, `ONGOING` (+12 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 292 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **113 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `BattlePanel` connect `BattlePanel` to `ui/BattlePanel.java`, `Landmark`, `file`, `JuanWord`, `MapSelectPanel`?**
  _High betweenness centrality (0.088) - this node is a cross-community bridge._
- **Why does `Player` connect `Player` to `BattlePanel`, `ui/BattlePanel.java`, `TileComponent`, `data/DataManager.java`, `JuanWord`, `MapSelectPanel`, `StartPanel`, `BattleSessionBuilder`?**
  _High betweenness centrality (0.081) - this node is a cross-community bridge._
- **Why does `BattleContext` connect `BattleContext` to `BattlePanel`, `ui/BattlePanel.java`, `AlviolaBossEnemy.java`, `DizonBossEnemy.java`, `GuardEnemy.java`, `KarabawEnemy.java`, `LolaEnemy.java`, `MaleStudentEnemy.java`, `MonkeyEnemy.java`, `PaintingEnemy.java`, `TileComponent`, `DataManager`, `DataManager`, `DifficultySetting`, `TestDesignPatterns.java`?**
  _High betweenness centrality (0.073) - this node is a cross-community bridge._
- **What connects `LetterSpec`, `PUNCH`, `SLASH` to the rest of the system?**
  _17 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `BattlePanel` be split into smaller, more focused modules?**
  _Cohesion score 0.052594670406732116 - nodes in this community are weakly interconnected._
- **Should `ui/BattlePanel.java` be split into smaller, more focused modules?**
  _Cohesion score 0.05 - nodes in this community are weakly interconnected._
- **Should `awt` be split into smaller, more focused modules?**
  _Cohesion score 0.05289193302891933 - nodes in this community are weakly interconnected._