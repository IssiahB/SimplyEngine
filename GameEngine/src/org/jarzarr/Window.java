package org.jarzarr;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.function.BiConsumer;

import javax.swing.JFrame;

public class Window {

	private JFrame frame;
	private Canvas canvas;
	private BufferStrategy bufferStrategy;

	// Default Values
	private String title = "Default Title";
	private Dimension size = new Dimension(700, 500);
	private boolean isFullscreen = false;
	private boolean resizable = false;

	// Render Values
	private int bufferCount = 3;
	private Color clearColor = Color.BLACK;
	
	//
	private BiConsumer<Integer, Integer> onResize = null;
	private int lastW = -1;
	private int lastH = -1;

	// Library-friendly close hook (Engine can pass: () -> engine.stop())
	private Runnable onClose = null;

	public Window() {
	}
	
	public Window(String title) {
		this.title = title;
	}

	public Window(String title, int width, int height) {
		this.title = title;
		this.size = new Dimension(width, height);
	}

	public Window(String title, boolean fullscreen) {
		this.title = title;
		this.isFullscreen = fullscreen;
	}

	/**
	 * Creates the frame + canvas and initializes the BufferStrategy. Call this once
	 * during Engine.init().
	 */
	public void createWindow() {
		// ----- Frame -----
		frame = new JFrame(title);

		// Library-friendly: don't hard-exit the whole JVM by default.
		// Let the caller decide what happens on close (stop loop, exit, etc.)
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(resizable);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (onClose != null)
					onClose.run();
			}
		});

		// ----- Canvas (your render surface) -----
		canvas = new Canvas();
		canvas.setPreferredSize(size);
		canvas.setFocusable(true);
		canvas.setIgnoreRepaint(true); // important for active rendering
		
		canvas.addComponentListener(new ComponentAdapter() {
		    @Override
		    public void componentResized(ComponentEvent e) {
		        int w = canvas.getWidth();
		        int h = canvas.getHeight();

		        // Avoid spam / duplicate events
		        if (w == lastW && h == lastH) return;
		        lastW = w;
		        lastH = h;

		        if (onResize != null) onResize.accept(w, h);
		    }
		});


		frame.add(canvas);
		frame.pack();
		frame.setLocationRelativeTo(null);

		if (isFullscreen) {
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // "maximized", not exclusive fullscreen
		}

		frame.setVisible(true);

		// Make sure the canvas can receive input
		canvas.requestFocus();

		// ----- BufferStrategy (must happen after visible) -----
		createOrRecreateBufferStrategy();
	}

	/**
	 * Begin a frame: returns a Graphics2D you can draw with. Your
	 * Engine.render(...) should draw using this, then call endFrame(g).
	 */
	public Graphics2D beginFrame() {
		if (bufferStrategy == null) {
			createOrRecreateBufferStrategy();
			if (bufferStrategy == null)
				return null; // still not ready this frame
		}

		// If the buffer contents are lost (alt-tab, minimize, etc.), recreate and skip
		// frame
		if (bufferStrategy.contentsLost()) {
			createOrRecreateBufferStrategy();
			return null;
		}

		Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();

		// Clear frame (your "clearColor")
		g.setColor(clearColor);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		return g;
	}

	/**
	 * End a frame: disposes graphics and swaps buffers.
	 */
	public void endFrame(Graphics2D g) {
		if (g != null) {
			g.dispose();
		}

		if (bufferStrategy != null) {
			bufferStrategy.show();

			// Helps reduce tearing on some systems (esp. Linux/X11)
			Toolkit.getDefaultToolkit().sync();
		}
	}

	/**
	 * Call when shutting down engine.
	 */
	public void dispose() {
		// BufferStrategy doesn't require explicit disposal, but disposing frame
		// releases resources.
		if (frame != null) {
			frame.dispose();
		}
	}

	private void createOrRecreateBufferStrategy() {
		if (canvas == null)
			return;

		// If the component isn't displayable yet, BufferStrategy can't be created.
		if (!canvas.isDisplayable())
			return;

		try {
			canvas.createBufferStrategy(bufferCount);
			bufferStrategy = canvas.getBufferStrategy();
		} catch (IllegalStateException ex) {
			// Can happen if called at a bad time; safest is to keep strategy null and try
			// later.
			bufferStrategy = null;
		}
	}

	// ----------------------------
	// Getters / Setters (Config)
	// ----------------------------
	
	public void setOnResize(BiConsumer<Integer, Integer> onResize) {
	    this.onResize = onResize;
	}

	public void setOnClose(Runnable onClose) {
		this.onClose = onClose;
	}

	public boolean isFullscreen() {
		return isFullscreen;
	}

	public void setFullscreen(boolean fullscreen) {
		this.isFullscreen = fullscreen;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public int getBufferCount() {
		return bufferCount;
	}

	public void setBufferCount(int bufferCount) {
		if (bufferCount < 2)
			bufferCount = 2;
		this.bufferCount = bufferCount;
	}

	public Color getClearColor() {
		return clearColor;
	}

	public void setClearColor(Color clearColor) {
		this.clearColor = clearColor;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public JFrame getFrame() {
		return frame;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		if (frame != null)
			frame.setTitle(title);
	}

	public Dimension getSize() {
		return size;
	}

	public void setSize(int width, int height) {
		this.size = new Dimension(width, height);
		if (canvas != null) {
			canvas.setPreferredSize(size);
			if (frame != null)
				frame.pack();
		}
	}

	public int getWidth() {
		return canvas != null ? canvas.getWidth() : size.width;
	}

	public int getHeight() {
		return canvas != null ? canvas.getHeight() : size.height;
	}
}
