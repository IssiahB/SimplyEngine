package org.jarzarr;

import java.awt.Graphics2D;

import org.jarzarr.core.EngineConfig;
import org.jarzarr.core.EngineContext;
import org.jarzarr.core.GameApp;
import org.jarzarr.input.ActionMap;
import org.jarzarr.render.Camera2D;
import org.jarzarr.scene.Scene;
import org.jarzarr.scene.SceneManager;

public class Engine<A extends Enum<A>> extends Loop {

	private final GameApp<A> app;
	private final Class<A> actionEnumClass;

	private final EngineConfig config = new EngineConfig();

	private Window window;
	private InputManager input;
	private ActionMap<A> actions;
	private SceneManager scenes;
	private Camera2D camera;

	private EngineContext<A> ctx;

	public Engine(GameApp<A> app, Class<A> actionEnumClass) {
		this.app = app;
		this.actionEnumClass = actionEnumClass;
	}

	@Override
	protected void init() {
		app.configure(config);
		
		setTargetUPS(config.updatesPerSecond);
        setMaxCatchUpUpdates(config.maxCatchUpUpdates);
        setStopDelay(config.engineStopDelay);
        setDebug(config.debug);

		window = new Window(config.title, config.width, config.height);
		window.setResizable(config.resizable);
		window.setFullscreen(config.fullscreen);
		window.setBufferCount(config.bufferCount);
		window.setClearColor(config.clearColor);

		// close requests stop; cleanup handles disposal
		window.setOnClose(this::stop);
		window.createWindow();

		// Input Mapping
		input = new InputManager(window.getCanvas());
		actions = new ActionMap<>(actionEnumClass, input);
		scenes = new SceneManager();
		camera = new Camera2D(window.getWidth(), window.getHeight());

		ctx = new EngineContext<>(window, input, actions, scenes, camera);
		
		window.setOnResize((w, h) -> ctx.scenes().onResize(w, h));

		app.bindInputs(ctx);
		Scene initial = app.createInitialScene(ctx);
		scenes.set(initial);
		
		app.onStart(ctx);
	}

	@Override
	protected void update(double dt) {
		ctx.setLastDt(dt);

		scenes.update(dt);

		actions.update();
		input.update();
	}

	@Override
	protected void render() {
		Graphics2D g = window.beginFrame();
		if (g == null)
			return;

		scenes.render(g);

		window.endFrame(g);
	}

	@Override
	protected void cleanup() {
		app.onStop(ctx);
		if (config.disposeWindowOnStop && window != null)
			window.dispose();
	}
}
