package org.jarzarr.ui;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Root of a UI tree. Owns hover/press/focus state and routes raw input to
 * nodes.
 *
 * <p>
 * UIRoot is itself a {@link UINode} but is not interactive.
 * </p>
 *
 * <p>
 * Fixes applied here:
 * </p>
 * <ul>
 * <li>Modal nodes are now part of the UI tree so they actually
 * render/layout.</li>
 * <li>Mouse button iteration starts at 1 to match MouseEvent.getButton().</li>
 * </ul>
 */
public final class UIRoot extends UINode {

	/**
	 * Optional interface for nodes that want simple click callbacks. This is
	 * triggered when mouse is pressed and released on the same node.
	 */
	public interface Clickable {
		void onClick(UIContext ctx, int button);
	}

	/** Currently hovered node (top-most hit). */
	private UINode hoveredNode;

	/** Node that received mouse down; used to deliver mouse up/click. */
	private UINode pressedNode;

	/** Node that currently owns keyboard focus. */
	private UINode focusedNode;

	/**
	 * Modal stack. Only the top modal receives hit testing and focus traversal.
	 * Modals are also added to the root children list so they render/layout.
	 */
	private final Deque<UINode> modalStack = new ArrayDeque<>();

	/** Constructs a root that fills the screen (size is set externally). */
	public UIRoot() {
		x = 0;
		y = 0;
		width = -1;
		height = -1;
	}

	/**
	 * Sets root size in pixels (usually the window canvas size).
	 *
	 * @param w width in pixels
	 * @param h height in pixels
	 */
	public void setSize(int w, int h) {
		this.width = Math.max(1, w);
		this.height = Math.max(1, h);
		markLayoutDirty();
	}

	/**
	 * Pushes a modal node. While active, only the modal subtree receives hit
	 * testing and focus traversal.
	 *
	 * <p>
	 * Fix: Ensure the modal is part of the tree so it renders and gets layout.
	 * </p>
	 *
	 * @param modal modal root node
	 */
	public void pushModal(UINode modal) {
		if (modal == null)
			return;

		// Ensure it is renderable/layoutable by being in the tree.
		if (modal.parent != this) {
			add(modal);
		} else {
			// If already a child, move it to top-most draw order.
			children.remove(modal);
			children.add(modal);
			markLayoutDirty();
		}

		modalStack.push(modal);

		// Clear hover/press if they were outside modal to prevent weird stuck states.
		hoveredNode = null;
		pressedNode = null;
	}

	/**
	 * Pops the top modal. If the removed modal contained hovered/pressed/focused
	 * nodes, those states are cleared.
	 */
	public void popModal() {
		if (modalStack.isEmpty())
			return;

		UINode modal = modalStack.pop();

		// If focus is inside the modal subtree, clear focus.
		if (focusedNode != null && isDescendantOf(focusedNode, modal)) {
			setFocus(null, null);
		}
		if (hoveredNode != null && isDescendantOf(hoveredNode, modal)) {
			hoveredNode = null;
		}
		if (pressedNode != null && isDescendantOf(pressedNode, modal)) {
			pressedNode = null;
		}

		// Remove modal from render tree.
		remove(modal);
	}

	/**
	 * Computes layout if dirty.
	 *
	 * @param ctx ui context
	 */
	public void layoutIfNeeded(UIContext ctx) {
		if (isLayoutDirty())
			computeLayout(ctx, 0, 0, width, height);
	}

	/**
	 * Routes raw input into the UI tree:
	 * <ul>
	 * <li>hover tracking</li>
	 * <li>mouse down/up/click</li>
	 * <li>wheel</li>
	 * <li>Tab focus traversal</li>
	 * <li>keyboard + text input to focused node</li>
	 * </ul>
	 *
	 * @param ctx ui context
	 * @return true if any node consumed input
	 */
	public boolean handleInput(UIContext ctx) {
		boolean consumed = false;

		// ---- mouse ----
		float mx = ctx.input.getMouseX();
		float my = ctx.input.getMouseY();
		ctx.mouseX = mx;
		ctx.mouseY = my;

		UINode hit = hitTarget(mx, my);
		if (hit != hoveredNode) {
			if (hoveredNode != null)
				hoveredNode.setHovered(false, ctx);
			hoveredNode = hit;
			if (hoveredNode != null)
				hoveredNode.setHovered(true, ctx);
		}
		if (hoveredNode != null)
			consumed |= hoveredNode.onMouseMove(ctx, mx, my);

		// FIX: MouseEvent button ids typically start at 1 (BUTTON1).
		for (int btn = 1; btn < 8; btn++) {
			if (ctx.input.isMousePressed(btn)) {
				if (hoveredNode != null && hoveredNode.enabled) {
					pressedNode = hoveredNode;
					pressedNode.setPressed(true);

					if (pressedNode.canFocus())
						setFocus(pressedNode, ctx);

					consumed |= pressedNode.onMouseDown(ctx, btn);
				}
			}
			if (ctx.input.isMouseReleased(btn)) {
				if (pressedNode != null) {
					pressedNode.setPressed(false);
					consumed |= pressedNode.onMouseUp(ctx, btn);

					// Click fires when press and release happen on the same node.
					if (pressedNode == hoveredNode && pressedNode instanceof Clickable clickable) {
						clickable.onClick(ctx, btn);
						consumed = true;
					}
					pressedNode = null;
				}
			}
		}

		int wheel = ctx.input.getScroll();
		if (wheel != 0 && hoveredNode != null) {
			consumed |= hoveredNode.onMouseWheel(ctx, wheel);
		}

		// ---- focus navigation: Tab / Shift+Tab ----
		if (ctx.input.isKeyPressed(KeyEvent.VK_TAB)) {
			boolean backwards = ctx.input.isKeyDown(KeyEvent.VK_SHIFT);
			focusNext(ctx, backwards);
			consumed = true;
		}

		// ---- keyboard + text ----
		if (focusedNode != null && focusedNode.enabled) {
			for (int key = 0; key < 512; key++) {
				if (ctx.input.isKeyPressed(key))
					consumed |= focusedNode.onKeyDown(ctx, key);
				if (ctx.input.isKeyReleased(key))
					consumed |= focusedNode.onKeyUp(ctx, key);
			}

			// Typed characters to focused node
			String typed = ctx.input.getTypedText();
			if (typed != null && !typed.isEmpty()) {
				consumed |= focusedNode.onTextInput(ctx, typed);
			}
		}

		return consumed;
	}

	/**
	 * Assigns focus to a node. Clears focus from the old node.
	 *
	 * @param n   node to focus (nullable)
	 * @param ctx ui context (nullable when clearing during modal pop)
	 */
	private void setFocus(UINode n, UIContext ctx) {
		if (focusedNode == n)
			return;
		if (focusedNode != null)
			focusedNode.setFocused(false);
		focusedNode = n;
		if (focusedNode != null)
			focusedNode.setFocused(true);
	}

	/**
	 * Moves focus to the next/previous focusable node in the active focus tree.
	 *
	 * @param ctx       ui context
	 * @param backwards true for Shift+Tab behavior
	 */
	private void focusNext(UIContext ctx, boolean backwards) {
		List<UINode> focusables = new ArrayList<>(32);
		collectFocusable(hitTreeRoot(), focusables);

		if (focusables.isEmpty()) {
			setFocus(null, ctx);
			return;
		}

		int idx = 0;
		if (focusedNode != null) {
			int current = focusables.indexOf(focusedNode);
			if (current >= 0)
				idx = current;
		}

		int next = backwards ? (idx - 1) : (idx + 1);
		if (next < 0)
			next = focusables.size() - 1;
		if (next >= focusables.size())
			next = 0;

		setFocus(focusables.get(next), ctx);
	}

	/**
	 * Collects focusable nodes depth-first in visual order.
	 *
	 * @param node start node
	 * @param out  output list
	 */
	private void collectFocusable(UINode node, List<UINode> out) {
		if (node == null)
			return;

		// Depth-first in visual order
		for (UINode c : node.children()) {
			collectFocusable(c, out);
		}

		if (node.canFocus())
			out.add(node);
	}

	/**
	 * Returns the root node for focus traversal: if a modal is active, traversal is
	 * restricted to the top modal subtree.
	 */
	private UINode hitTreeRoot() {
		if (!modalStack.isEmpty())
			return modalStack.peek();
		return this;
	}

	/**
	 * Returns the node that should receive hit testing: if a modal is active, only
	 * the top modal subtree can be hit.
	 */
	private UINode hitTarget(float mx, float my) {
		if (!modalStack.isEmpty()) {
			UINode modal = modalStack.peek();
			return modal != null ? modal.hitTest(mx, my) : null;
		}
		return hitTest(mx, my);
	}

	/**
	 * Returns true if node n is within the subtree rooted at root.
	 */
	private boolean isDescendantOf(UINode n, UINode root) {
		if (n == null || root == null)
			return false;
		if (n == root)
			return true;
		UINode p = n.parent;
		while (p != null) {
			if (p == root)
				return true;
			p = p.parent;
		}
		return false;
	}

	/** Root has no visual rendering by default. */
	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
	}

	/** Root itself is not interactive. */
	@Override
	protected boolean isInteractive() {
		return false;
	}
}
