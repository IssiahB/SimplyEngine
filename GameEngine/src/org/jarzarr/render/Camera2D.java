package org.jarzarr.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Simple 2D camera for world-to-screen rendering.
 *
 * <p>
 * The camera represents a view into world space defined by a position (x, y), a
 * zoom level, and a viewport size. It can be applied directly to a
 * {@link Graphics2D} via an {@link AffineTransform}.
 * </p>
 *
 * <p>
 * The camera supports:
 * </p>
 * <ul>
 * <li>World ↔ screen coordinate conversion</li>
 * <li>Zooming</li>
 * <li>Immediate or smooth following of a target</li>
 * <li>Optional deadzone to reduce micro-jitter</li>
 * </ul>
 */
public class Camera2D {

	/** Camera center position in world coordinates. */
	private double x;
	private double y;

	/** Zoom factor (1.0 = 1:1). Values < 1 zoom out, > 1 zoom in. */
	private double zoom = 1.0;

	/** Viewport width in pixels (usually the window canvas width). */
	private int viewportW = 800;

	/** Viewport height in pixels (usually the window canvas height). */
	private int viewportH = 600;

	// ----------------------------
	// Smoothing / Follow behavior
	// ----------------------------

	/** Enables or disables smooth camera following. */
	private boolean smoothingEnabled = true;

	/**
	 * Follow speed in 1/seconds. Higher values result in snappier movement. Typical
	 * range: 5.0 – 15.0.
	 */
	private double followSpeed = 5.0;

	/**
	 * Optional deadzone radius (world units). If the target remains within this
	 * radius, the camera will not move, preventing small jitter when targets
	 * vibrate slightly.
	 */
	private double deadzoneRadius = 0.0;

	/** Creates a camera with default viewport size. */
	public Camera2D() {
	}

	/**
	 * Creates a camera with a specific viewport size.
	 *
	 * @param viewportW viewport width in pixels
	 * @param viewportH viewport height in pixels
	 */
	public Camera2D(int viewportW, int viewportH) {
		this.viewportW = viewportW;
		this.viewportH = viewportH;
	}

	/**
	 * Applies the camera transform to the given Graphics2D.
	 *
	 * <p>
	 * This method should be called once before rendering world-space objects. UI
	 * rendering should typically reset or ignore this transform.
	 * </p>
	 *
	 * @param g graphics context for the current frame
	 */
	public void apply(Graphics2D g) {
		AffineTransform tx = new AffineTransform();

		// Move origin to screen center
		tx.translate(viewportW / 2.0, viewportH / 2.0);

		// Apply zoom
		tx.scale(zoom, zoom);

		// Translate world so camera position is centered
		tx.translate(-x, -y);

		g.setTransform(tx);
	}

	/**
	 * Converts world coordinates to screen coordinates.
	 *
	 * @param worldX x position in world space
	 * @param worldY y position in world space
	 * @return corresponding point in screen space
	 */
	public Point2D.Double worldToScreen(double worldX, double worldY) {
		double sx = (worldX - x) * zoom + (viewportW / 2.0);
		double sy = (worldY - y) * zoom + (viewportH / 2.0);
		return new Point2D.Double(sx, sy);
	}

	/**
	 * Converts screen coordinates to world coordinates.
	 *
	 * @param screenX x position in screen space
	 * @param screenY y position in screen space
	 * @return corresponding point in world space
	 */
	public Point2D.Double screenToWorld(double screenX, double screenY) {
		double wx = (screenX - (viewportW / 2.0)) / zoom + x;
		double wy = (screenY - (viewportH / 2.0)) / zoom + y;
		return new Point2D.Double(wx, wy);
	}

	/**
	 * Updates the viewport size. Should be called when the window is resized.
	 *
	 * @param w new viewport width
	 * @param h new viewport height
	 */
	public void setViewport(int w, int h) {
		viewportW = Math.max(1, w);
		viewportH = Math.max(1, h);
	}

	/**
	 * Instantly snaps the camera center to the target position. No smoothing is
	 * applied.
	 *
	 * @param targetX target world x
	 * @param targetY target world y
	 */
	public void followSnap(double targetX, double targetY) {
		x = targetX;
		y = targetY;
	}

	/**
	 * Smoothly follows a target using exponential smoothing.
	 *
	 * <p>
	 * This should be called from update(dt). The smoothing behavior is stable
	 * across different frame rates.
	 * </p>
	 *
	 * @param targetX target world x
	 * @param targetY target world y
	 * @param dt      delta time in seconds
	 */
	public void followSmooth(double targetX, double targetY, double dt) {
		if (!smoothingEnabled) {
			followSnap(targetX, targetY);
			return;
		}

		double dx = targetX - x;
		double dy = targetY - y;

		// Deadzone prevents micro-jitter when target barely moves
		if (deadzoneRadius > 0.0) {
			double dist2 = dx * dx + dy * dy;
			if (dist2 <= deadzoneRadius * deadzoneRadius) {
				return;
			}
		}

		// Exponential smoothing (dt-independent)
		double alpha = 1.0 - Math.exp(-followSpeed * dt);

		x += dx * alpha;
		y += dy * alpha;
	}

	// ----------------------------
	// Smoothing configuration
	// ----------------------------

	/** @return true if smoothing is enabled */
	public boolean isSmoothingEnabled() {
		return smoothingEnabled;
	}

	/**
	 * Enables or disables smooth following.
	 *
	 * @param enabled true to enable smoothing
	 */
	public void setSmoothingEnabled(boolean enabled) {
		this.smoothingEnabled = enabled;
	}

	/** @return follow speed (1/seconds) */
	public double getFollowSpeed() {
		return followSpeed;
	}

	/**
	 * Sets follow speed for smooth tracking.
	 *
	 * @param followSpeed new follow speed (clamped to > 0)
	 */
	public void setFollowSpeed(double followSpeed) {
		this.followSpeed = Math.max(0.01, followSpeed);
	}

	/** @return deadzone radius in world units */
	public double getDeadzoneRadius() {
		return deadzoneRadius;
	}

	/**
	 * Sets the deadzone radius used during smooth following.
	 *
	 * @param deadzoneRadius radius in world units (>= 0)
	 */
	public void setDeadzoneRadius(double deadzoneRadius) {
		this.deadzoneRadius = Math.max(0.0, deadzoneRadius);
	}

	// ----------------------------
	// Basic getters / setters
	// ----------------------------

	/** @return camera x position in world space */
	public double getX() {
		return x;
	}

	/** @return camera y position in world space */
	public double getY() {
		return y;
	}

	/**
	 * Sets camera position directly.
	 *
	 * @param x world x
	 * @param y world y
	 */
	public void setPosition(double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** @return current zoom factor */
	public double getZoom() {
		return zoom;
	}

	/**
	 * Sets zoom factor.
	 *
	 * @param zoom zoom value (clamped to >= 0.05)
	 */
	public void setZoom(double zoom) {
		this.zoom = Math.max(0.05, zoom);
	}

	/** @return viewport width in pixels */
	public int getViewportW() {
		return viewportW;
	}

	/** @return viewport height in pixels */
	public int getViewportH() {
		return viewportH;
	}
}
