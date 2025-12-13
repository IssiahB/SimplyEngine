package org.jarzarr.core;

import org.jarzarr.scene.Scene;

public interface GameApp<A extends Enum<A>> {

	/** Called before window creation. Configure settings here. */
	void configure(EngineConfig config);

	/** Called after InputManager + ActionMap exist. Bind inputs here. */
	void bindInputs(EngineContext<A> ctx);

	/** Called after everything is ready. Return the first scene. */
	Scene createInitialScene(EngineContext<A> ctx);

	/** Optional lifecycle hooks */
	default void onStart(EngineContext<A> ctx) {
	}

	default void onStop(EngineContext<A> ctx) {
	}
}
