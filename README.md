# SimplyEngine

**SimplyEngine** is a lightweight, pure-Java 2D game engine designed to expose and implement core engine fundamentals without relying on third-party libraries. It uses only the Java Standard Library (AWT/Swing) and is built as a reusable engine library rather than a single hard-coded game.

---

## Features

- Fixed-timestep game loop (deterministic updates)
- Configurable window and rendering pipeline
- Triple-buffered rendering via BufferStrategy
- Polled keyboard and mouse input
- Action binding system with action consumption
- Scene / state management with overlay support
- Centralized EngineContext
- 2D camera with world/screen separation
- Smooth camera following (frame-rate independent)

---

## Design Goals

- Pure Java (no external dependencies)
- Deterministic behavior
- Clean separation of engine and game logic
- Library-first architecture
- Explicit, debuggable systems

---

## High-Level Architecture

GameApp (your game)
    ↓
Engine
    ↓
EngineContext
    ├─ Window
    ├─ InputManager
    ├─ ActionMap
    ├─ SceneManager
    └─ Camera2D
         ↓
      Scene(s)

---

## Getting Started

### Entry Point

```java
public class Main {
    public static void main(String[] args) {
        new Engine<>(new MyGameApp(), Action.class).start();
    }
}
```

---

## GameApp Interface

```java
public interface GameApp<A extends Enum<A>> {

    void configure(EngineConfig config);

    void bindInputs(EngineContext<A> ctx);

    Scene createInitialScene(EngineContext<A> ctx);

    default void onStart(EngineContext<A> ctx) {}
    default void onStop(EngineContext<A> ctx) {}
}
```

---

## EngineConfig

```java
public class EngineConfig {
    public String title;
    public int width;
    public int height;
    public boolean resizable;
    public boolean fullscreen;

    public int bufferCount;
    public Color clearColor;

    public double updatesPerSecond;
    public int maxCatchUpUpdates;
}
```

---

## Game Loop

SimplyEngine uses a fixed-timestep update loop.
Updates run at a fixed rate while rendering runs as fast as possible.
Excess accumulated time is capped to prevent the spiral of death.

---

## Window & Rendering

Rendering is handled using JFrame, Canvas, and BufferStrategy.

```java
Graphics2D g = window.beginFrame();
// draw
window.endFrame(g);
```

---

## Input System

The InputManager provides polled input for:
- Keyboard
- Mouse buttons
- Mouse movement
- Mouse wheel

Each input tracks Down, Pressed, and Released states per update tick.

---

## Action Binding

```java
public enum Action {
    MOVE_LEFT,
    MOVE_RIGHT,
    JUMP,
    PAUSE
}
```

Bindings are defined in GameApp.bindInputs.

```java
ctx.actions().bind(Action.MOVE_LEFT, new KeyTrigger(KeyEvent.VK_A));
```

---

## Action Consumption

Actions may be consumed so other systems do not react during the same update tick.

```java
ctx.actions().consumePressed(Action.PAUSE);
```

---

## Scene System

Scenes represent modes of the game such as gameplay, menus, or overlays.

```java
public interface Scene {
    void onEnter();
    void onExit();
    void update(double dt);
    void render(Graphics2D g);
    default boolean isOverlay() { return false; }
}
```

---

## EngineContext

EngineContext bundles engine services:

- Window
- InputManager
- ActionMap
- SceneManager
- Camera2D

---

## Camera System

The Camera2D separates world space from screen space and supports zooming and smoothing.

```java
ctx.camera().followSmooth(playerX, playerY, dt);
```

---

## Project Status

SimplyEngine is an early-stage engine focused on correctness and structure.
It is designed to be extended and learned from.

---

## License

See the LICENSE file for details.
