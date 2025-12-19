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

/**
 * AWT/Swing window wrapper that owns the JFrame, Canvas render surface, and a
 * BufferStrategy.
 *
 * <p>
 * This class is built for "active rendering": the engine requests a Graphics2D
 * via {@link #beginFrame()}, draws, then swaps via
 * {@link #endFrame(Graphics2D)}.
 * </p>
 *
 * <p>
 * Close behavior is library-friendly: it does not hard-exit the JVM. Instead,
 * an {@link #setOnClose(Runnable)} hook can be used to stop the engine.
 * </p>
 */
public class Window {

	/** Top-level window container. */
	private JFrame frame;

	/** Render/input surface used by the engine. */
	private Canvas canvas;

	/** BufferStrategy used for flipping buffers (double/triple buffering). */
	private BufferStrategy bufferStrategy;

	// ----------------------------
	// Default / Configurable State
	// ----------------------------

	/** Window title shown in the title bar. */
	private String title = "Default Title";

	/** Preferred initial client size (canvas size). */
	private Dimension size = new Dimension(700, 500);

	/** If true, window starts maximized (not exclusive full-screen). */
	private boolean isFullscreen = false;

	/** If true, the user can resize the window. */
	private boolean resizable = false;

	// ----------------------------
	// Render State
	// ----------------------------

	/** Number of buffers used by the BufferStrategy (min 2, commonly 2 or 3). */
	private int bufferCount = 3;

	/** Color used to clear the frame before rendering. */
	private Color clearColor = Color.BLACK;

	// ----------------------------
	// Events / Hooks
	// ----------------------------

	/** Optional callback invoked on canvas resize: (width, height). */
	private BiConsumer<Integer, Integer> onResize = null;

	/** Last observed canvas width (to filter duplicate resize events). */
	private int lastW = -1;

	/** Last observed canvas height (to filter duplicate resize events). */
	private int lastH = -1;

	/** Optional close hook (Engine can pass: () -> engine.stop()). */
	private Runnable onClose = null;

	/** Constructs a window with default values. */
	public Window() {
	}

	/**
	 * Constructs a window with a specific title.
	 *
	 * @param title window title
	 */
	public Window(String title) {
		this.title = title;
	}

	/**
	 * Constructs a window with a title and initial size.
	 *
	 * @param title  window title
	 * @param width  preferred width
	 * @param height preferred height
	 */
	public Window(String title, int width, int height) {
		this.title = title;
		this.size = new Dimension(width, height);
	}

	/**
	 * Constructs a window with a title and fullscreen preference.
	 *
	 * @param title      window title
	 * @param fullscreen if true, starts maximized
	 */
	public Window(String title, boolean fullscreen) {
		this.title = title;
		this.isFullscreen = fullscreen;
	}

	/**
	 * Creates the JFrame + Canvas and initializes the BufferStrategy. Call this
	 * once during engine initialization.
	 *
	 * <p>
	 * Note: BufferStrategy creation must happen after the frame is visible.
	 * </p>
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

		// ----- Canvas (render surface) -----
		canvas = new Canvas();
		canvas.setPreferredSize(size);
		canvas.setFocusable(true);
		canvas.setIgnoreRepaint(true); // important for active rendering

		// Resize listener forwards changes to the engine (scenes/camera/etc.).
		canvas.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int w = canvas.getWidth();
				int h = canvas.getHeight();

				// Avoid spam / duplicate events.
				if (w == lastW && h == lastH)
					return;
				lastW = w;
				lastH = h;

				if (onResize != null)
					onResize.accept(w, h);
			}
		});

		frame.add(canvas);
		frame.pack();
		frame.setLocationRelativeTo(null);

		// Maximized windowed mode (not exclusive full-screen).
		if (isFullscreen) {
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		}

		frame.setVisible(true);

		// Make sure the canvas can receive input.
		canvas.requestFocus();

		// ----- BufferStrategy (must happen after visible) -----
		createOrRecreateBufferStrategy();
	}

	/**
	 * Begins a frame and returns a Graphics2D for drawing.
	 *
	 * <p>
	 * If the buffer strategy isn't ready or has lost contents (alt-tab/minimize),
	 * this may return null and the caller should skip rendering this frame.
	 * </p>
	 *
	 * @return Graphics2D for drawing, or null if not ready this frame
	 */
	public Graphics2D beginFrame() {
		if (bufferStrategy == null) {
			createOrRecreateBufferStrategy();
			if (bufferStrategy == null)
				return null; // still not ready this frame
		}

		// If the buffer contents are lost (alt-tab, minimize, etc.), recreate and skip.
		if (bufferStrategy.contentsLost()) {
			createOrRecreateBufferStrategy();
			return null;
		}

		Graphics2D g = (Graphics2D) bufferStrategy.getDrawGraphics();

		// Clear frame.
		g.setColor(clearColor);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

		return g;
	}

	/**
	 * Ends a frame: disposes Graphics2D and swaps buffers.
	 *
	 * @param g Graphics2D obtained from beginFrame()
	 */
	public void endFrame(Graphics2D g) {
		if (g != null) {
			g.dispose();
		}

		if (bufferStrategy != null) {
			bufferStrategy.show();

			// Helps reduce tearing on some systems (esp. Linux/X11).
			Toolkit.getDefaultToolkit().sync();
		}
	}

	/**
	 * Releases window resources. Safe to call multiple times. Disposes the JFrame
	 * which releases OS resources.
	 */
	public void dispose() {
		if (frame != null) {
			frame.dispose();
		}
	}

	/**
	 * (Re)creates the BufferStrategy if possible. If called too early (canvas not
	 * displayable), it will do nothing.
	 */
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
			// If called at a bad time, keep strategy null and try later.
			bufferStrategy = null;
		}
	}

	// ----------------------------
	// Getters / Setters (Config)
	// ----------------------------

	/**
	 * Sets a callback invoked when the canvas is resized.
	 *
	 * @param onResize callback receiving (width, height)
	 */
	public void setOnResize(BiConsumer<Integer, Integer> onResize) {
		this.onResize = onResize;
	}

	/**
	 * Sets a callback invoked when the user requests window close. Typical use is
	 * engine::stop.
	 *
	 * @param onClose close callback
	 */
	public void setOnClose(Runnable onClose) {
		this.onClose = onClose;
	}

	/** @return true if the window starts maximized. */
	public boolean isFullscreen() {
		return isFullscreen;
	}

	/**
	 * Sets whether the window starts maximized (windowed fullscreen).
	 *
	 * @param fullscreen true to start maximized
	 */
	public void setFullscreen(boolean fullscreen) {
		this.isFullscreen = fullscreen;
	}

	/** @return true if user can resize the window. */
	public boolean isResizable() {
		return resizable;
	}

	/**
	 * Sets whether the window is resizable. Must be called before createWindow() to
	 * affect the JFrame.
	 *
	 * @param resizable true if resizable
	 */
	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	/** @return number of buffers in the strategy. */
	public int getBufferCount() {
		return bufferCount;
	}

	/**
	 * Sets buffer count for the BufferStrategy. Values < 2 are clamped to 2. Must
	 * be set before createWindow() to apply cleanly.
	 *
	 * @param bufferCount requested buffer count
	 */
	public void setBufferCount(int bufferCount) {
		if (bufferCount < 2)
			bufferCount = 2;
		this.bufferCount = bufferCount;
	}

	/** @return clear color used each frame. */
	public Color getClearColor() {
		return clearColor;
	}

	/**
	 * Sets the clear color used in beginFrame().
	 *
	 * @param clearColor background color
	 */
	public void setClearColor(Color clearColor) {
		this.clearColor = clearColor;
	}

	/** @return canvas render surface (also used for input listeners). */
	public Canvas getCanvas() {
		return canvas;
	}

	/** @return underlying JFrame. */
	public JFrame getFrame() {
		return frame;
	}

	/** @return current title. */
	public String getTitle() {
		return title;
	}

	/**
	 * Sets window title. If the frame already exists, updates it live.
	 *
	 * @param title new title
	 */
	public void setTitle(String title) {
		this.title = title;
		if (frame != null)
			frame.setTitle(title);
	}

	/** @return preferred initial size. */
	public Dimension getSize() {
		return size;
	}

	/**
	 * Sets preferred canvas size. If already created, updates preferred size and
	 * repacks.
	 *
	 * @param width  preferred width
	 * @param height preferred height
	 */
	public void setSize(int width, int height) {
		this.size = new Dimension(width, height);
		if (canvas != null) {
			canvas.setPreferredSize(size);
			if (frame != null)
				frame.pack();
		}
	}

	/**
	 * Returns current canvas width if available, otherwise preferred width.
	 *
	 * @return width in pixels
	 */
	public int getWidth() {
		return canvas != null ? canvas.getWidth() : size.width;
	}

	/**
	 * Returns current canvas height if available, otherwise preferred height.
	 *
	 * @return height in pixels
	 */
	public int getHeight() {
		return canvas != null ? canvas.getHeight() : size.height;
	}
}
