package org.jarzarr.core;

import java.awt.Color;

public class EngineConfig {

	// Window
	public String title = "Jarzarr";
	public int width = 800;
	public int height = 600;
	public boolean resizable = false;
	public boolean fullscreen = false;

	// Rendering
	public int bufferCount = 3;
	public Color clearColor = Color.BLACK;

	// Loop
	public double updatesPerSecond = 60.0;
	public int maxCatchUpUpdates = 8; // your spiral-of-death protection
	public int engineStopDelay = 100;

	// Close behavior
	public boolean disposeWindowOnStop = true;
	
	// Utilities
	public boolean debug = false;
}
