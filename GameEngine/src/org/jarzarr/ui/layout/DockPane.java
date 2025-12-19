package org.jarzarr.ui.layout;

import java.awt.Graphics2D;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Dock layout container: TOP, BOTTOM, LEFT, RIGHT, CENTER.
 *
 * <p>
 * Each dock slot can contain at most one node. The layout reserves space in
 * this order:
 * </p>
 * <ol>
 * <li>TOP (full width)</li>
 * <li>BOTTOM (full width)</li>
 * <li>LEFT (remaining height)</li>
 * <li>RIGHT (remaining height)</li>
 * <li>CENTER (whatever remains)</li>
 * </ol>
 *
 * <p>
 * Fixes:
 * </p>
 * <ul>
 * <li>setDock now marks layout dirty</li>
 * <li>remove() keeps internal maps consistent if children are removed
 * directly</li>
 * </ul>
 */
public final class DockPane extends UINode {

	public enum Dock {
		TOP, BOTTOM, LEFT, RIGHT, CENTER
	}

	/** Node -> dock slot. */
	private final Map<UINode, Dock> docks = new HashMap<>();

	/** Dock slot -> node. */
	private final EnumMap<Dock, UINode> slots = new EnumMap<>(Dock.class);

	public DockPane addTop(UINode n) {
		return setDock(Dock.TOP, n);
	}

	public DockPane addBottom(UINode n) {
		return setDock(Dock.BOTTOM, n);
	}

	public DockPane addLeft(UINode n) {
		return setDock(Dock.LEFT, n);
	}

	public DockPane addRight(UINode n) {
		return setDock(Dock.RIGHT, n);
	}

	public DockPane setCenter(UINode n) {
		return setDock(Dock.CENTER, n);
	}

	private DockPane setDock(Dock d, UINode n) {
		if (n == null)
			return this;

		// If slot already has something, remove it.
		UINode existing = slots.get(d);
		if (existing != null) {
			super.remove(existing);
			docks.remove(existing);
		}

		add(n);
		docks.put(n, d);
		slots.put(d, n);

		markLayoutDirty();
		return this;
	}

	/**
	 * Fix: keep dock bookkeeping consistent if a child is removed directly.
	 */
	@Override
	public void remove(UINode child) {
		if (child == null)
			return;

		Dock d = docks.remove(child);
		if (d != null && slots.get(d) == child) {
			slots.remove(d);
		}

		super.remove(child);
	}

	public Dock getDock(UINode n) {
		return docks.getOrDefault(n, Dock.CENTER);
	}

	@Override
	protected void onLayout(UIContext ctx) {
		float cx = padding.left;
		float cy = padding.top;
		float cw = Math.max(0, gw - padding.left - padding.right);
		float ch = Math.max(0, gh - padding.top - padding.bottom);

		// TOP
		UINode top = slots.get(Dock.TOP);
		if (top != null && top.visible) {
			float h = sizeH(top, ch);
			top.x = cx;
			top.y = cy;
			top.width = cw;
			top.height = h;
			cy += h;
			ch -= h;
		}

		// BOTTOM
		UINode bottom = slots.get(Dock.BOTTOM);
		if (bottom != null && bottom.visible) {
			float h = sizeH(bottom, ch);
			bottom.x = cx;
			bottom.y = cy + (ch - h);
			bottom.width = cw;
			bottom.height = h;
			ch -= h;
		}

		// LEFT
		UINode left = slots.get(Dock.LEFT);
		if (left != null && left.visible) {
			float w = sizeW(left, cw);
			left.x = cx;
			left.y = cy;
			left.width = w;
			left.height = ch;
			cx += w;
			cw -= w;
		}

		// RIGHT
		UINode right = slots.get(Dock.RIGHT);
		if (right != null && right.visible) {
			float w = sizeW(right, cw);
			right.x = cx + (cw - w);
			right.y = cy;
			right.width = w;
			right.height = ch;
			cw -= w;
		}

		// CENTER
		UINode center = slots.get(Dock.CENTER);
		if (center != null && center.visible) {
			center.x = cx;
			center.y = cy;
			center.width = cw;
			center.height = ch;
		}
	}

	private float sizeW(UINode n, float fallback) {
		if (n.width > 0)
			return n.width;
		if (n.prefW > 0)
			return n.prefW;
		return fallback;
	}

	private float sizeH(UINode n, float fallback) {
		if (n.height > 0)
			return n.height;
		if (n.prefH > 0)
			return n.prefH;
		return fallback;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
