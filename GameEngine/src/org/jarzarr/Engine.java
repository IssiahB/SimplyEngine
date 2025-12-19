package org.jarzarr;

import java.awt.Graphics2D;

import org.jarzarr.assets.AssetManager;
import org.jarzarr.core.EngineConfig;
import org.jarzarr.core.EngineContext;
import org.jarzarr.core.GameApp;
import org.jarzarr.input.ActionMap;
import org.jarzarr.render.Camera2D;
import org.jarzarr.scene.Scene;
import org.jarzarr.scene.SceneManager;

/**
 * Main engine entry-point that wires together the window, input, action
 * mapping, scene system, camera, and assets, then runs the fixed-timestep loop.
 *
 * <p>
 * Generic parameter A is the user's enum that defines high-level actions (ex:
 * MOVE_LEFT, JUMP) mapped from raw input devices.
 * </p>
 *
 * <p>
 * Lifecycle:
 * </p>
 * <ul>
 * <li>{@link #init()} -> build systems + create initial scene</li>
 * <li>{@link #update(double)} -> fixed timestep simulation + input polling</li>
 * <li>{@link #render()} -> render current scene to the window backbuffer</li>
 * <li>{@link #cleanup()} -> shutdown callbacks + dispose resources</li>
 * </ul>
 */
public class Engine<A extends Enum<A>> extends Loop {

	/**
	 * User application implementation providing configuration, input bindings, and
	 * scenes.
	 */
	private final GameApp<A> app;

	/** Runtime type token for the action enum used by {@link ActionMap}. */
	private final Class<A> actionEnumClass;

	/**
	 * Engine configuration populated by {@link GameApp#configure(EngineConfig)}.
	 * Stored here so init() can apply configuration before building subsystems.
	 */
	private final EngineConfig config = new EngineConfig();

	/** OS window + render surface + swap chain wrapper. */
	private Window window;

	/** Low-level device input (keyboard/mouse) state tracker. */
	private InputManager input;

	/** High-level action mapper that translates input into enum actions. */
	private ActionMap<A> actions;

	/** Scene stack/manager responsible for update/render routing. */
	private SceneManager scenes;

	/** Asset loader/cache owner (textures, sounds, etc.). */
	private AssetManager assets;

	/** Default 2D camera used by rendering and scene logic. */
	private Camera2D camera;

	/**
	 * Shared engine context passed into the app and scenes. This is the "service
	 * locator" for engine-owned systems.
	 */
	private EngineContext<A> ctx;

	/**
	 * Constructs an Engine with a user {@link GameApp} and the action enum class.
	 *
	 * @param app             user application implementation
	 * @param actionEnumClass the enum class backing the action map
	 */
	public Engine(GameApp<A> app, Class<A> actionEnumClass) {
		this.app = app;
		this.actionEnumClass = actionEnumClass;
	}

	/**
	 * Initializes the engine once, on the loop thread, before updates/render start.
	 * Creates the window, core subsystems, binds inputs, and sets the initial
	 * scene.
	 */
	@Override
	protected void init() {
		// Let the user's app fill in config defaults/overrides.
		app.configure(config);

		// Apply loop settings from config before starting the main loop behavior.
		setTargetUPS(config.updatesPerSecond);
		setMaxCatchUpUpdates(config.maxCatchUpUpdates);
		setStopDelay(config.engineStopDelay);
		setDebug(config.debug);

		// Window creation and render configuration.
		window = new Window(config.title, config.width, config.height);
		window.setResizable(config.resizable);
		window.setFullscreen(config.fullscreen);
		window.setBufferCount(config.bufferCount);
		window.setClearColor(config.clearColor);

		// Close requests stop; cleanup handles disposal.
		window.setOnClose(this::stop);
		window.createWindow();

		// Core subsystems.
		input = new InputManager(window.getCanvas());
		actions = new ActionMap<>(actionEnumClass, input);
		scenes = new SceneManager();
		assets = new AssetManager();
		camera = new Camera2D(window.getWidth(), window.getHeight());

		// Shared context exposed to app/scenes.
		ctx = new EngineContext<>(window, input, actions, scenes, camera, assets);

		// Forward resize events to the active scene(s).
		window.setOnResize((w, h) -> ctx.scenes().onResize(w, h));

		// Let app register action bindings.
		app.bindInputs(ctx);

		// Create and set initial scene.
		Scene initial = app.createInitialScene(ctx);
		scenes.set(initial);

		// App start callback after systems + initial scene exist.
		app.onStart(ctx);
	}

	/**
	 * Fixed timestep update (dt is typically 1/UPS). Updates scene simulation
	 * first, then refreshes action state and raw input.
	 *
	 * @param dt fixed delta time in seconds
	 */
	@Override
	protected void update(double dt) {
		// Store last dt so other parts of the engine/app can read it if desired.
		ctx.setLastDt(dt);

		// Update game logic.
		scenes.update(dt);

		// Update input mapping and raw input states after simulation step.
		actions.update();
		input.update();
	}

	/**
	 * Renders one frame. This may run as fast as possible (not fixed) depending on
	 * the loop. Grabs a Graphics2D from the window, delegates rendering to the
	 * scene manager, then presents buffers.
	 */
	@Override
	protected void render() {
		Graphics2D g = window.beginFrame();
		if (g == null)
			return;

		scenes.render(g);

		window.endFrame(g);
	}

	/**
	 * Called once after the loop stops. Notifies the app and releases owned
	 * resources. Window disposal is optional depending on config.
	 */
	@Override
	protected void cleanup() {
		app.onStop(ctx);

		// Dispose assets (textures/sounds/etc.) if created.
		if (assets != null)
			assets.dispose();

		// Optionally dispose the window on stop (library-friendly default can differ).
		if (config.disposeWindowOnStop && window != null)
			window.dispose();
	}
}
