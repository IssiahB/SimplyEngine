# SimplyEngine

**SimplyEngine** is a lightweight 2D game engine written **entirely in pure Java**, with **zero third‑party libraries**.

It exists for one core reason:

> To have a game engine that is fully understandable, debuggable, and modifiable by the developer using it.

No hidden magic.  
No native bindings.  
No external dependencies.  
Just Java, the standard library, and code you can read end‑to‑end.

---

## Philosophy

SimplyEngine was created to solve a personal problem:

Most engines abstract *too much*. They work — until they don’t — and when something breaks, you’re debugging someone else’s architecture.

SimplyEngine is:
- 100% Java (AWT, Swing, javax.sound, etc.)
- Explicit over clever
- Stack‑based instead of state‑based
- Designed to be read, not hidden

If you understand Java, you can understand this engine completely.

---

## Features

- Fixed‑timestep game loop
- Scene stack (set / push / pop)
- Scene transitions (fade in/out)
- Input system (keyboard + mouse → actions)
- Asset manager (images, sounds, fonts)
- SpriteSheets and Animations
- Camera2D with zoom + smooth follow
- UI system (layouts + widgets)
- No external libraries required

---

## Documentation

📚 **Javadocs**  
https://issiahb.github.io/SimplyEngine/

---

## Download

⬇️ **Engine JAR (library)**  
Download the prebuilt engine JAR from the GitHub releases page:

https://github.com/IssiahB/SimplyEngine/releases

(Or build it yourself directly from source using Eclipse.)

---

## Project Structure

Typical layout when using SimplyEngine in a game project:

```
MyGame/
 ├─ src/
 │   └─ mygame/
 │       └─ Main.java
 ├─ resources/
 │   └─ assets/
 │       ├─ images/
 │       ├─ audio/
 │       └─ fonts/
 └─ SimplyEngine.jar
```

The `resources/` folder must be on the **classpath**.

---

## Creating a Game (Minimal Example)

### 1) Define Actions

```java
public enum Action {
    MOVE_LEFT, MOVE_RIGHT, PAUSE
}
```

---

### 2) Create a GameApp

```java
public class Main implements GameApp<Action> {

    public static void main(String[] args) {
        new Engine<>(new Main(), Action.class).start();
    }

    @Override
    public void configure(EngineConfig config) {
        config.title = "My Game";
        config.width = 800;
        config.height = 600;
        config.resizable = true;
    }

    @Override
    public void bindInputs(EngineContext<Action> ctx) {
        ctx.actions().bind(Action.MOVE_LEFT, new KeyTrigger(KeyEvent.VK_A));
        ctx.actions().bind(Action.MOVE_RIGHT, new KeyTrigger(KeyEvent.VK_D));
        ctx.actions().bind(Action.PAUSE, new KeyTrigger(KeyEvent.VK_ESCAPE));
    }

    @Override
    public Scene createInitialScene(EngineContext<Action> ctx) {
        return new GameScene(ctx);
    }
}
```

---

### 3) Create a Scene

```java
public class GameScene implements Scene {

    private final EngineContext<Action> ctx;
    private double x = 100;

    public GameScene(EngineContext<Action> ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onEnter() {}

    @Override
    public void update(double dt) {
        if (ctx.actions().isDown(Action.MOVE_LEFT)) {
            x -= 200 * dt;
        }
        if (ctx.actions().isDown(Action.MOVE_RIGHT)) {
            x += 200 * dt;
        }

        if (ctx.actions().isPressed(Action.PAUSE)) {
            ctx.scenes().push(new PauseScene(ctx));
            ctx.actions().consumePressed(Action.PAUSE);
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.fillRect((int)x, 200, 40, 40);
    }

    @Override
    public void onExit() {}
}
```

---

## Assets

SimplyEngine loads assets from the classpath.

```java
BufferedImage img = ctx.assets().getImage("assets/images/player.png");
Sound sound = ctx.assets().getSound("assets/audio/click.wav");
Font font = ctx.assets().getFont("assets/fonts/pixel.ttf", 24f);
```

Assets must exist inside your game project’s `resources/` folder.

---

## Animations

```java
SpriteSheet sheet = new SpriteSheet(image, 20, 20);
Animation walk = Animation.buildRowAnim(sheet, 0, 8, 12.0);

walk.update(dt);
g.drawImage(walk.frame(), x, y, null);
```

Animations are time‑based and engine‑driven.

---

## Camera

```java
ctx.camera().setZoom(4.0);
ctx.camera().followSmooth(playerX, playerY, dt);
```

Camera transforms are applied before world rendering.

---

## UI System

SimplyEngine includes a fully custom UI layer:

- DockPane
- StackPanel
- GridPanel
- WrapPanel
- Buttons, Labels, Sliders, TextFields

UI renders in screen‑space and supports keyboard + mouse focus.

```java
public class PauseScene extends UIScene<Action> {

    @Override
    protected void buildUI(UIRoot root) {
        Button resume = new Button("Resume")
            .onClick(() -> ctx.scenes().pop());
        root.add(resume);
    }
}
```

---

## Scene Transitions

```java
ctx.scenes().transitionPush(
    new PauseScene(ctx),
    new FadeTransition(0.25)
);
```

Transitions are scene wrappers — not hacks — and preserve stack integrity.

---

## What This Engine Is (and Isn’t)

✔ Good for:
- Learning engine architecture
- Small to medium 2D games
- Prototypes and personal projects
- Full control and understanding

✖ Not intended for:
- AAA games
- Hardware‑accelerated rendering
- Massive worlds without modification

This engine values **clarity over scale**.

---

## License

MIT License — use it, modify it, ship it.

---

## Final Note

SimplyEngine is not about being flashy.

It’s about **knowing exactly what your engine is doing**, at all times.

If that matters to you — welcome.

— Issiah Banda
