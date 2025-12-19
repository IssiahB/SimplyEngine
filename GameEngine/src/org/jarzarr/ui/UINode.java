package org.jarzarr.ui;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.jarzarr.ui.layout.Anchors;

/**
 * Base class for all UI elements in the engine.
 *
 * <p>
 * UINode forms a tree. Layout is computed top-down and produces global
 * (screen-space) bounds for each node.
 * </p>
 *
 * <p>
 * Coordinate model:
 * </p>
 * <ul>
 * <li>x/y/width/height are local within the parent's content area</li>
 * <li>gx/gy/gw/gh are computed absolute screen-space bounds</li>
 * </ul>
 *
 * <p>
 * Layout model:
 * </p>
 * <ul>
 * <li>{@link #onMeasure(UIContext)} runs before sizing finalizes (preferred
 * sizes)</li>
 * <li>{@link #onLayout(UIContext)} runs after sizing finalizes (containers
 * position children)</li>
 * </ul>
 */
public abstract class UINode {
	// ----------------------------
	// Public layout inputs
	// ----------------------------

	/** Local x position within parent content area. */
	public float x, y;

	/**
	 * Local size. If <= 0, treated as "unset" and may fall back to pref or parent
	 * size.
	 */
	public float width, height;

	/** If false, node is skipped for layout/update/render/hit-test. */
	public boolean visible = true;

	/** If false, node should not respond to interactions. */
	public boolean enabled = true;

	/** Padding applied inside this node before laying out children. */
	public Insets padding = Insets.ZERO;

	/** Preferred width (-1 means unspecified). Typically set by onMeasure(). */
	public float prefW = -1;

	/** Preferred height (-1 means unspecified). Typically set by onMeasure(). */
	public float prefH = -1;

	/**
	 * Optional anchors used by AnchorPane-like containers. If non-null, an anchor
	 * container may position/size this node.
	 */
	public Anchors anchors = null;

	// ----------------------------
	// Computed layout outputs
	// ----------------------------

	/** Computed global bounds in screen space. */
	protected float gx, gy, gw, gh;

	// ----------------------------
	// Tree structure
	// ----------------------------

	/** Parent node (null for root). */
	protected UINode parent;

	/** Child nodes in draw order (last is rendered last and hit-tested first). */
	protected final List<UINode> children = new ArrayList<>();

	// ----------------------------
	// Interaction state
	// ----------------------------

	/** True if the mouse is currently over this node. */
	protected boolean hovered;

	/** True if this node is currently pressed (mouse down started here). */
	protected boolean pressed;

	/** True if this node currently has keyboard focus. */
	protected boolean focused;

	/** Dirty flag to indicate layout must be recomputed. */
	private boolean layoutDirty = true;

	/**
	 * Adds a child to this node. If it already has a parent, it is removed first.
	 *
	 * @param child child node to add
	 */
	public void add(UINode child) {
		if (child == null)
			return;
		if (child.parent != null)
			child.parent.remove(child);
		child.parent = this;
		children.add(child);
		markLayoutDirty();
	}

	/**
	 * Removes a child from this node.
	 *
	 * @param child child node to remove
	 */
	public void remove(UINode child) {
		if (child == null)
			return;
		if (children.remove(child)) {
			child.parent = null;
			markLayoutDirty();
		}
	}

	/**
	 * Returns the live children list (modifying it directly is allowed but you
	 * should call markLayoutDirty() afterward).
	 *
	 * @return children list
	 */
	public List<UINode> children() {
		return children;
	}

	/**
	 * Marks this node (and its ancestors) as needing a layout recompute.
	 */
	public void markLayoutDirty() {
		layoutDirty = true;
		if (parent != null)
			parent.markLayoutDirty();
	}

	/** @return true if this node needs layout recomputation */
	public boolean isLayoutDirty() {
		return layoutDirty;
	}

	/** Clears the layout dirty flag after layout has been computed. */
	protected void clearLayoutDirty() {
		layoutDirty = false;
	}

	// ---- sizing helpers for end-users ----

	/**
	 * Fluent size setter.
	 *
	 * @param w width
	 * @param h height
	 * @return this node
	 */
	public UINode size(float w, float h) {
		this.width = w;
		this.height = h;
		markLayoutDirty();
		return this;
	}

	/**
	 * Fluent position setter.
	 *
	 * @param x local x
	 * @param y local y
	 * @return this node
	 */
	public UINode pos(float x, float y) {
		this.x = x;
		this.y = y;
		markLayoutDirty();
		return this;
	}

	/** Fill parent when used inside AnchorPane (recommended). */
	public UINode fillParent() {
		this.anchors = Anchors.fill();
		return this;
	}

	/** Anchor top-left with fixed size. */
	public UINode anchorTopLeft(float left, float top) {
		this.anchors = Anchors.topLeft(left, top);
		return this;
	}

	/** Anchor top-right with fixed size. */
	public UINode anchorTopRight(float right, float top) {
		this.anchors = Anchors.topRight(right, top);
		return this;
	}

	/** Anchor bottom-left with fixed size. */
	public UINode anchorBottomLeft(float left, float bottom) {
		this.anchors = Anchors.bottomLeft(left, bottom);
		return this;
	}

	/** Anchor bottom-right with fixed size. */
	public UINode anchorBottomRight(float right, float bottom) {
		this.anchors = Anchors.bottomRight(right, bottom);
		return this;
	}

	/** Stretch horizontally between left/right, fixed top. */
	public UINode stretchTop(float left, float right, float top) {
		this.anchors = Anchors.stretchTop(left, right, top);
		return this;
	}

	/** Stretch horizontally between left/right, fixed bottom. */
	public UINode stretchBottom(float left, float right, float bottom) {
		this.anchors = Anchors.stretchBottom(left, right, bottom);
		return this;
	}

	// ---- layout ----

	/**
	 * Computes layout for this node and its subtree.
	 *
	 * <p>
	 * parentGX/GY/GW/GH represent the parent's content area bounds in screen space.
	 * </p>
	 *
	 * @param ctx      ui context
	 * @param parentGX parent content global x
	 * @param parentGY parent content global y
	 * @param parentGW parent content global width
	 * @param parentGH parent content global height
	 */
	public final void computeLayout(UIContext ctx, float parentGX, float parentGY, float parentGW, float parentGH) {
		if (!visible)
			return;

		// Allow nodes (Label/TextField) to set pref sizes before final sizing.
		onMeasure(ctx);

		gx = parentGX + x;
		gy = parentGY + y;

		// Key rule: treat width/height <= 0 as "unset"
		gw = (width > 0) ? width : ((prefW > 0) ? prefW : parentGW);
		gh = (height > 0) ? height : ((prefH > 0) ? prefH : parentGH);

		// Container-specific child sizing/positioning happens here.
		onLayout(ctx);

		// Child content area = node bounds minus padding.
		float cx = gx + padding.left;
		float cy = gy + padding.top;
		float cw = Math.max(0, gw - padding.horizontal());
		float ch = Math.max(0, gh - padding.vertical());

		for (UINode c : children) {
			c.computeLayout(ctx, cx, cy, cw, ch);
		}

		clearLayoutDirty();
	}

	/**
	 * Override to compute preferred sizes from content BEFORE sizing is finalized.
	 * (Example: Label autosize, TextField min widths)
	 */
	protected void onMeasure(UIContext ctx) {
	}

	/**
	 * Override for containers to position children (set child.x/y/width/height).
	 * Called after this node's gx/gy/gw/gh are finalized.
	 */
	protected void onLayout(UIContext ctx) {
	}

	// ---- update/render ----

	/**
	 * Updates this node and its children (logic only).
	 *
	 * @param ctx ui context (dt, input, theme, etc.)
	 */
	public void update(UIContext ctx) {
		if (!visible)
			return;
		for (UINode c : children)
			c.update(ctx);
	}

	/**
	 * Renders this node then its children.
	 *
	 * @param ctx ui context
	 * @param g   graphics context in screen space
	 */
	public void render(UIContext ctx, Graphics2D g) {
		if (!visible)
			return;
		onRender(ctx, g);
		for (UINode c : children)
			c.render(ctx, g);
	}

	/**
	 * Node-specific render implementation.
	 *
	 * @param ctx ui context
	 * @param g   graphics context
	 */
	protected abstract void onRender(UIContext ctx, Graphics2D g);

	// ---- hit test ----

	/**
	 * Returns true if the given point is inside this node's global bounds.
	 *
	 * @param mx mouse x in screen space
	 * @param my mouse y in screen space
	 * @return true if contained and visible
	 */
	public final boolean contains(float mx, float my) {
		return visible && mx >= gx && my >= gy && mx <= (gx + gw) && my <= (gy + gh);
	}

	/**
	 * Returns the deepest top-most interactive node under the cursor.
	 *
	 * <p>
	 * Children are hit-tested from end to start (visual top-most first).
	 * </p>
	 *
	 * @param mx mouse x
	 * @param my mouse y
	 * @return hit node or null
	 */
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

	/**
	 * Override to mark a node as interactive (eligible for hit testing). Containers
	 * typically return false unless they handle events themselves.
	 */
	protected boolean isInteractive() {
		return false;
	}

	/**
	 * Override to control keyboard focus eligibility. Default: interactive nodes
	 * are focusable.
	 */
	protected boolean isFocusable() {
		return isInteractive();
	}

	// ---- events ----
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

	/** Text input (characters) from keyTyped buffer. */
	public boolean onTextInput(UIContext ctx, String text) {
		return false;
	}

	// ---- state ----

	/** Internal: toggles hover state and fires enter/exit events. */
	void setHovered(boolean v, UIContext ctx) {
		if (hovered == v)
			return;
		hovered = v;
		if (v)
			onMouseEnter(ctx);
		else
			onMouseExit(ctx);
	}

	/** Internal: toggles pressed state. */
	void setPressed(boolean v) {
		pressed = v;
	}

	/** Internal: toggles focus state. */
	void setFocused(boolean v) {
		focused = v;
	}

	/** Internal: returns focus state. */
	boolean getFocused() {
		return focused;
	}

	/** Internal: global x. */
	float globalX() {
		return gx;
	}

	/** Internal: global y. */
	float globalY() {
		return gy;
	}

	/** Internal: global width. */
	float globalW() {
		return gw;
	}

	/** Internal: global height. */
	float globalH() {
		return gh;
	}

	/** Internal helper for focus traversal. */
	boolean canFocus() {
		return visible && enabled && isFocusable();
	}
}
