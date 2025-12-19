package org.jarzarr.ui.layout;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import org.jarzarr.ui.UIContext;
import org.jarzarr.ui.UINode;

/**
 * Grid layout container with optional row/column weighting and row/col spans.
 *
 * <p>
 * Cells are registered via {@link #add(UINode, int, int)} or
 * {@link #add(UINode, int, int, int, int)}. Each cell assigns a node to a grid
 * coordinate with optional span.
 * </p>
 *
 * <p>
 * Fixes:
 * </p>
 * <ul>
 * <li>gaps/weights now mark layout dirty</li>
 * <li>removing a child now removes its cell record (prevents stale cells)</li>
 * </ul>
 */
public final class GridPanel extends UINode {

	/** Cell metadata for a node inside the grid. */
	public static final class Cell {
		public final UINode node;
		public final int row, col, rowSpan, colSpan;

		public Cell(UINode node, int row, int col, int rowSpan, int colSpan) {
			this.node = node;
			this.row = row;
			this.col = col;
			this.rowSpan = Math.max(1, rowSpan);
			this.colSpan = Math.max(1, colSpan);
		}
	}

	private final int rows;
	private final int cols;

	/** Horizontal gap between columns (pixels). */
	private float gapX = 10;

	/** Vertical gap between rows (pixels). */
	private float gapY = 10;

	/** Column weights (relative shares). If null/empty, columns are equal. */
	private float[] colWeights;

	/** Row weights (relative shares). If null/empty, rows are equal. */
	private float[] rowWeights;

	/** Registered cells corresponding to children. */
	private final List<Cell> cells = new ArrayList<>();

	public GridPanel(int rows, int cols) {
		this.rows = Math.max(1, rows);
		this.cols = Math.max(1, cols);
	}

	public GridPanel gaps(float gx, float gy) {
		this.gapX = gx;
		this.gapY = gy;
		markLayoutDirty();
		return this;
	}

	public GridPanel colWeights(float... w) {
		this.colWeights = w;
		markLayoutDirty();
		return this;
	}

	public GridPanel rowWeights(float... w) {
		this.rowWeights = w;
		markLayoutDirty();
		return this;
	}

	public GridPanel add(UINode node, int row, int col) {
		return add(node, row, col, 1, 1);
	}

	public GridPanel add(UINode node, int row, int col, int rowSpan, int colSpan) {
		if (node == null)
			return this;

		super.add(node);
		cells.add(new Cell(node, row, col, rowSpan, colSpan));
		markLayoutDirty();
		return this;
	}

	/**
	 * Fix: keep cell registry consistent when children are removed directly.
	 */
	@Override
	public void remove(UINode child) {
		if (child == null)
			return;

		super.remove(child);

		for (int i = cells.size() - 1; i >= 0; i--) {
			if (cells.get(i).node == child) {
				cells.remove(i);
			}
		}
	}

	@Override
	protected void onLayout(UIContext ctx) {
		float innerX = padding.left;
		float innerY = padding.top;
		float innerW = Math.max(0, gw - padding.left - padding.right);
		float innerH = Math.max(0, gh - padding.top - padding.bottom);

		float[] cw = computeSizes(cols, innerW, gapX, colWeights);
		float[] rh = computeSizes(rows, innerH, gapY, rowWeights);

		float[] colX = new float[cols];
		float[] rowY = new float[rows];

		float x = innerX;
		for (int c = 0; c < cols; c++) {
			colX[c] = x;
			x += cw[c] + (c < cols - 1 ? gapX : 0);
		}

		float y = innerY;
		for (int r = 0; r < rows; r++) {
			rowY[r] = y;
			y += rh[r] + (r < rows - 1 ? gapY : 0);
		}

		for (Cell cell : cells) {
			if (cell.node == null || !cell.node.visible)
				continue;

			int r0 = clamp(cell.row, 0, rows - 1);
			int c0 = clamp(cell.col, 0, cols - 1);
			int r1 = clamp(r0 + cell.rowSpan - 1, 0, rows - 1);
			int c1 = clamp(c0 + cell.colSpan - 1, 0, cols - 1);

			float nx = colX[c0];
			float ny = rowY[r0];

			float nw = 0;
			for (int c = c0; c <= c1; c++)
				nw += cw[c];
			nw += gapX * (c1 - c0);

			float nh = 0;
			for (int r = r0; r <= r1; r++)
				nh += rh[r];
			nh += gapY * (r1 - r0);

			cell.node.x = nx;
			cell.node.y = ny;
			cell.node.width = nw;
			cell.node.height = nh;
		}
	}

	/**
	 * Computes per-row/column sizes given total space and optional weights.
	 *
	 * @param count   number of segments
	 * @param total   total available space (pixels)
	 * @param gap     gap between segments (pixels)
	 * @param weights relative weights (null/empty => equal)
	 * @return array of size count containing segment sizes
	 */
	private static float[] computeSizes(int count, float total, float gap, float[] weights) {
		float gapsTotal = gap * (count - 1);
		float usable = Math.max(0, total - gapsTotal);

		float[] out = new float[count];

		// No weights => equal
		if (weights == null || weights.length == 0) {
			float each = (count > 0) ? (usable / count) : 0;
			for (int i = 0; i < count; i++)
				out[i] = each;
			return out;
		}

		// Normalize weights. Missing/<=0 weights become 1.
		float sum = 0;
		float[] ww = new float[count];
		for (int i = 0; i < count; i++) {
			float wi = (i < weights.length ? weights[i] : 0f);
			if (wi <= 0f)
				wi = 1f;
			ww[i] = wi;
			sum += wi;
		}

		for (int i = 0; i < count; i++)
			out[i] = (sum > 0f) ? (usable * (ww[i] / sum)) : 0f;

		return out;
	}

	private static int clamp(int v, int lo, int hi) {
		if (v < lo)
			return lo;
		if (v > hi)
			return hi;
		return v;
	}

	@Override
	protected void onRender(UIContext ctx, Graphics2D g) {
	}

	@Override
	protected boolean isInteractive() {
		return false;
	}
}
