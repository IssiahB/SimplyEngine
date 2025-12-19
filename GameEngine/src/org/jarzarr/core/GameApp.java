package org.jarzarr.core;

import org.jarzarr.scene.Scene;

/**
 * User application contract for the engine.
 *
 * <p>
 * The engine calls these methods in a predictable lifecycle so the game/app can
 * configure engine settings, bind inputs, create the first scene, and run
 * optional startup/shutdown hooks.
 * </p>
 *
 * <p>
 * Generic parameter A is the enum type representing high-level input actions
 * (ex: MOVE_LEFT, JUMP) used by ActionMap.
 * </p>
 */
public interface GameApp<A extends Enum<A>> {

	/**
	 * Called before window creation. Use this to override defaults in
	 * {@link EngineConfig} (title, size, UPS, etc.).
	 *
	 * @param config mutable engine configuration
	 */
	void configure(EngineConfig config);

	/**
	 * Called after InputManager and ActionMap exist. Use this to register action
	 * bindings (keys/mouse -> enum actions).
	 *
	 * @param ctx engine context containing
	 *            window/input/actions/scenes/camera/assets
	 */
	void bindInputs(EngineContext<A> ctx);

	/**
	 * Called after core subsystems are created and input bindings have been
	 * registered. Return the first scene the engine should run.
	 *
	 * @param ctx engine context containing
	 *            window/input/actions/scenes/camera/assets
	 * @return initial scene to set in the scene manager
	 */
	Scene createInitialScene(EngineContext<A> ctx);

	/**
	 * Optional lifecycle hook called after the initial scene is set and the engine
	 * is ready to begin updates/renders.
	 *
	 * @param ctx engine context
	 */
	default void onStart(EngineContext<A> ctx) {
	}

	/**
	 * Optional lifecycle hook called during engine shutdown/cleanup, before
	 * resources are disposed.
	 *
	 * @param ctx engine context
	 */
	default void onStop(EngineContext<A> ctx) {
	}
}
