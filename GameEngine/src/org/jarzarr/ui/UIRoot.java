package org.jarzarr.ui;

import java.awt.Graphics2D;
import java.util.ArrayDeque;
import java.util.Deque;

public final class UIRoot extends UINode {

	public interface Clickable {
		void onClick(UIContext ctx, int button);
	}

	private UINode hoveredNode;
	private UINode pressedNode;
	private UINode focusedNode;

	private final Deque<UINode> modalStack = new ArrayDeque<>();

	public UIRoot() {
		x = 0;
		y = 0;
		width = -1; // means "use parent"
		height = -1;
	}

	public void setSize(int w, int h) {
		// Root uses screen as "parent"
		this.width = w;
		this.height = h;
		markLayoutDirty();
	}

	public void pushModal(UINode modal) {
		if (modal != null)
			modalStack.push(modal);
	}

	public void popModal() {
		if (!modalStack.isEmpty())
			modalStack.pop();
	}

	public void layoutIfNeeded(UIContext ctx) {
		if (isLayoutDirty())
			computeLayout(ctx, 0, 0, width, height);
	}

	public boolean handleInput(UIContext ctx) {
		boolean consumed = false;

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

		// Mouse buttons: your InputManager uses MouseEvent button codes (1..)
		for (int btn = 0; btn < 8; btn++) {
			if (ctx.input.isMousePressed(btn)) {
				if (hoveredNode != null && hoveredNode.enabled) {
					pressedNode = hoveredNode;
					pressedNode.setPressed(true);
					setFocus(pressedNode, ctx);
					consumed |= pressedNode.onMouseDown(ctx, btn);
				}
			}
			if (ctx.input.isMouseReleased(btn)) {
				if (pressedNode != null) {
					pressedNode.setPressed(false);
					consumed |= pressedNode.onMouseUp(ctx, btn);

					if (pressedNode == hoveredNode && pressedNode instanceof Clickable clickable) {
						clickable.onClick(ctx, btn);
						consumed = true;
					}
					pressedNode = null;
				}
			}
		}

		// Wheel
		int wheel = ctx.input.getScroll();
		if (wheel != 0 && hoveredNode != null) {
			consumed |= hoveredNode.onMouseWheel(ctx, wheel);
		}

		// Keyboard to focused node (scan 0..511)
		if (focusedNode != null && focusedNode.enabled) {
			for (int key = 0; key < 512; key++) {
				if (ctx.input.isKeyPressed(key))
					consumed |= focusedNode.onKeyDown(ctx, key);
				if (ctx.input.isKeyReleased(key))
					consumed |= focusedNode.onKeyUp(ctx, key);
			}
		}

		return consumed;
	}

	private void setFocus(UINode n, UIContext ctx) {
		if (focusedNode == n)
			return;
		if (focusedNode != null)
			focusedNode.setFocused(false);
		focusedNode = n;
		if (focusedNode != null)
			focusedNode.setFocused(true);
	}

	private UINode hitTarget(float mx, float my) {
		if (!modalStack.isEmpty()) {
			UINode modal = modalStack.peek();
			return modal != null ? modal.hitTest(mx, my) : null;
		}
		return hitTest(mx, my);
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
		/* root draws nothing */ }

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
