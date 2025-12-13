package org.jarzarr.render;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class Camera2D {

    private double x;
    private double y;
    private double zoom = 1.0;

    private int viewportW = 800;
    private int viewportH = 600;

    // --- smoothing ---
    private boolean smoothingEnabled = true;

    // Follow speed in 1/seconds.
    // Higher = snappier. Typical range: 5 to 15.
    private double followSpeed = 5.0;

    // Optional deadzone (prevents tiny jitter when target moves slightly)
    private double deadzoneRadius = 0.0;

    public Camera2D() {}

    public Camera2D(int viewportW, int viewportH) {
        this.viewportW = viewportW;
        this.viewportH = viewportH;
    }

    public void apply(Graphics2D g) {
        AffineTransform tx = new AffineTransform();
        tx.translate(viewportW / 2.0, viewportH / 2.0);
        tx.scale(zoom, zoom);
        tx.translate(-x, -y);
        g.setTransform(tx);
    }

    public Point2D.Double worldToScreen(double worldX, double worldY) {
        double sx = (worldX - x) * zoom + (viewportW / 2.0);
        double sy = (worldY - y) * zoom + (viewportH / 2.0);
        return new Point2D.Double(sx, sy);
    }

    public Point2D.Double screenToWorld(double screenX, double screenY) {
        double wx = (screenX - (viewportW / 2.0)) / zoom + x;
        double wy = (screenY - (viewportH / 2.0)) / zoom + y;
        return new Point2D.Double(wx, wy);
    }

    public void setViewport(int w, int h) {
        viewportW = Math.max(1, w);
        viewportH = Math.max(1, h);
    }

    /** Snap the camera center to target (no smoothing). */
    public void followSnap(double targetX, double targetY) {
        x = targetX;
        y = targetY;
    }

    /**
     * Smooth follow using exponential smoothing. Call this from update(dt).
     * dt is seconds.
     */
    public void followSmooth(double targetX, double targetY, double dt) {
        if (!smoothingEnabled) {
            followSnap(targetX, targetY);
            return;
        }

        double dx = targetX - x;
        double dy = targetY - y;

        // Deadzone: if target is very close, don't move camera to avoid micro-jitter.
        if (deadzoneRadius > 0.0) {
            double dist2 = dx * dx + dy * dy;
            if (dist2 <= deadzoneRadius * deadzoneRadius) {
                return;
            }
        }

        // Exponential smoothing: alpha is stable across different dt values
        double alpha = 1.0 - Math.exp(-followSpeed * dt);

        x += dx * alpha;
        y += dy * alpha;
    }

    // --------------- config ---------------

    public boolean isSmoothingEnabled() { return smoothingEnabled; }
    public void setSmoothingEnabled(boolean enabled) { this.smoothingEnabled = enabled; }

    public double getFollowSpeed() { return followSpeed; }
    public void setFollowSpeed(double followSpeed) {
        this.followSpeed = Math.max(0.01, followSpeed);
    }

    public double getDeadzoneRadius() { return deadzoneRadius; }
    public void setDeadzoneRadius(double deadzoneRadius) {
        this.deadzoneRadius = Math.max(0.0, deadzoneRadius);
    }

    // --------------- basic getters/setters ---------------

    public double getX() { return x; }
    public double getY() { return y; }
    public void setPosition(double x, double y) { this.x = x; this.y = y; }

    public double getZoom() { return zoom; }
    public void setZoom(double zoom) { this.zoom = Math.max(0.05, zoom); }

    public int getViewportW() { return viewportW; }
    public int getViewportH() { return viewportH; }
}
