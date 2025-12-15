package org.jarzarr.ui;

import java.awt.Font;

import org.jarzarr.InputManager;
import org.jarzarr.assets.AssetManager;

public final class UIContext {
	public final InputManager input;
	public final AssetManager assets;

	public int screenW;
	public int screenH;

	public float mouseX;
	public float mouseY;
	public double dt;

	public Font defaultFont;

	public UIContext(InputManager input, AssetManager assets) {
		this.input = input;
		this.assets = assets;
	}
}
