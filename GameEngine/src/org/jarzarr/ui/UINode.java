package org.jarzarr.ui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public abstract class UINode {
	// Local bounds within parent content area
	public float x, y, width, height;

	public boolean visible = true;
	public boolean enabled = true;

	public Insets padding = Insets.ZERO;

	// Preferred size (-1 means "unspecified")
	public float prefW = -1;
	public float prefH = -1;

	// Computed global bounds (screen space)
	protected float gx, gy, gw, gh;

	protected UINode parent;
	protected final List<UINode> children = new ArrayList<>();

	// Interaction state
	protected boolean hovered;
	protected boolean pressed;
	protected boolean focused;

	private boolean layoutDirty = true;

	public void add(UINode child) {
		if (child == null)
			return;
		if (child.parent != null)
			child.parent.remove(child);
		child.parent = this;
		children.add(child);
		markLayoutDirty();
	}

	public void remove(UINode child) {
		if (child == null)
			return;
		if (children.remove(child)) {
			child.parent = null;
			markLayoutDirty();
		}
	}

	public List<UINode> children() {
		return children;
	}

	public void markLayoutDirty() {
		layoutDirty = true;
		if (parent != null)
			parent.markLayoutDirty();
	}

	public boolean isLayoutDirty() {
		return layoutDirty;
	}

	protected void clearLayoutDirty() {
		layoutDirty = false;
	}

	// ---- layout ----
	public final void computeLayout(UIContext ctx, float parentGX, float parentGY, float parentGW, float parentGH) {
		if (!visible)
			return;

		gx = parentGX + x;
		gy = parentGY + y;

		// If width/height not explicitly set (> 0), fill parent.
		// If prefW/prefH set, use those (useful for leaf widgets).
		gw = (width > 0) ? width : ((prefW > 0) ? prefW : parentGW);
		gh = (height > 0) ? height : ((prefH > 0) ? prefH : parentGH);

		onLayout(ctx);

		float cx = gx + padding.left;
		float cy = gy + padding.top;
		float cw = Math.max(0, gw - padding.horizontal());
		float ch = Math.max(0, gh - padding.vertical());

		for (UINode c : children) {
			c.computeLayout(ctx, cx, cy, cw, ch);
		}

		clearLayoutDirty();
	}

	protected void onLayout(UIContext ctx) {
		// containers override
	}

	// ---- update/render ----
	public void update(UIContext ctx) {
		if (!visible)
			return;
		for (UINode c : children)
			c.update(ctx);
	}

	public void render(UIContext ctx, Graphics2D g) {
		if (!visible)
			return;
		onRender(ctx, g);
		for (UINode c : children)
			c.render(ctx, g);
	}

	protected abstract void onRender(UIContext ctx, Graphics2D g);

	// ---- hit test ----
	public final boolean contains(float mx, float my) {
		return visible && mx >= gx && my >= gy && mx <= (gx + gw) && my <= (gy + gh);
	}

	public UINode hitTest(float mx, float my) {
		if (!contains(mx, my))
			return null;

		for (int i = children.size() - 1; i >= 0; i--) {
			UINode c = children.get(i);
			UINode hit = c.hitTest(mx, my);
			if (hit != null)
				return hit;
		}
		return isInteractive() ? this : null;
	}

	protected boolean isInteractive() {
		return false;
	}

	// ---- events (return true to consume) ----
	public boolean onMouseEnter(UIContext ctx) {
		return false;
	}

	public boolean onMouseExit(UIContext ctx) {
		return false;
	}

	public boolean onMouseMove(UIContext ctx, float mx, float my) {
		return false;
	}

	public boolean onMouseDown(UIContext ctx, int button) {
		return false;
	}

	public boolean onMouseUp(UIContext ctx, int button) {
		return false;
	}

	public boolean onMouseWheel(UIContext ctx, int amount) {
		return false;
	}

	public boolean onKeyDown(UIContext ctx, int keyCode) {
		return false;
	}

	public boolean onKeyUp(UIContext ctx, int keyCode) {
		return false;
	}

	// ---- state ----
	void setHovered(boolean v, UIContext ctx) {
		if (hovered == v)
			return;
		hovered = v;
		if (v)
			onMouseEnter(ctx);
		else
			onMouseExit(ctx);
	}

	void setPressed(boolean v) {
		pressed = v;
	}

	void setFocused(boolean v) {
		focused = v;
	}

	public float globalX() {
		return gx;
	}

	public float globalY() {
		return gy;
	}

	public float globalW() {
		return gw;
	}

	public float globalH() {
		return gh;
	}
}
