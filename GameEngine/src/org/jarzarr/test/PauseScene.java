package org.jarzarr.test;

import java.awt.Color;
import java.awt.Font;

import org.jarzarr.core.EngineContext;
import org.jarzarr.test.Main.Action;
import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIRoot;
import org.jarzarr.ui.UIScene;
import org.jarzarr.ui.layout.Center;
import org.jarzarr.ui.layout.DockPane;
import org.jarzarr.ui.layout.GridPanel;
import org.jarzarr.ui.layout.WrapPanel;
import org.jarzarr.ui.widgets.Button;
import org.jarzarr.ui.widgets.CheckBox;
import org.jarzarr.ui.widgets.Label;
import org.jarzarr.ui.widgets.Panel;
import org.jarzarr.ui.widgets.ProgressBar;
import org.jarzarr.ui.widgets.Slider;
import org.jarzarr.ui.widgets.TextField;

public class PauseScene extends UIScene<Action> {

	private static final String FONT_PATH = "assets/fonts/pixel-font.ttf";

	private TextField nameField;
	private CheckBox muteBox;
	private Slider volumeSlider;
	private ProgressBar progress;

	private double t = 0;

	public PauseScene(EngineContext<Action> ctx) {
		super(ctx);
	}

	@Override
	public void onEnter() {
		super.onEnter();

		// Prevent 1-frame layout flash
		int w = ctx.window().getWidth();
		int h = ctx.window().getHeight();
		uictx.screenW = w;
		uictx.screenH = h;
		ui.setSize(w, h);
		ui.layoutIfNeeded(uictx);
	}

	@Override
	protected void buildUI(UIRoot root) {
		// Fonts / theme
		Font pixel = ctx.assets().getFont(FONT_PATH, 32f);
		uictx.defaultFont = pixel;
		uictx.theme.font = pixel;
		uictx.theme.fontSmall = (pixel != null) ? pixel.deriveFont(18f) : null;

		Font small = (uictx.theme.fontSmall != null) ? uictx.theme.fontSmall : pixel;

		// Overlay (fills root)
		Panel overlay = new Panel().background(uictx.theme.overlay);
		overlay.fillParent();
		root.add(overlay);

		// Dock layout inside overlay
		DockPane dock = new DockPane();
		dock.fillParent();
		overlay.add(dock);

		// -------------------------
		// TOP BAR (DockPane.TOP)
		// -------------------------
		Panel topBar = new Panel().background(new Color(0, 0, 0, 160)).border(new Color(255, 255, 255, 30), 1f);
		topBar.height = 52;
		topBar.padding = Insets.hv(14, 10);

		DockPane topDock = new DockPane();
		topDock.fillParent();
		topBar.add(topDock);

		Label leftTitle = new Label("PAUSED").font(small).align(Label.AlignH.LEFT, Label.AlignV.CENTER);

		Label rightTitle = new Label("UI DEMO (DockPane + Grid + Wrap)").font(small).color(uictx.theme.mutedText)
				.align(Label.AlignH.RIGHT, Label.AlignV.CENTER);

		topDock.addLeft(leftTitle);
		topDock.addRight(rightTitle);

		dock.addTop(topBar);

		// -------------------------
		// BOTTOM BAR (DockPane.BOTTOM)
		// -------------------------
		Panel bottomBar = new Panel().background(new Color(0, 0, 0, 160)).border(new Color(255, 255, 255, 30), 1f);
		bottomBar.height = 48;
		bottomBar.padding = Insets.hv(14, 10);

		DockPane bottomDock = new DockPane();
		bottomDock.fillParent();
		bottomBar.add(bottomDock);

		Label footerLeft = new Label("TAB / SHIFT+TAB: FOCUS").font(small).color(uictx.theme.mutedText)
				.align(Label.AlignH.LEFT, Label.AlignV.CENTER);

		Label footerRight = new Label("ESC: RESUME").font(small).color(uictx.theme.mutedText).align(Label.AlignH.RIGHT,
				Label.AlignV.CENTER);

		bottomDock.addLeft(footerLeft);
		bottomDock.addRight(footerRight);

		dock.addBottom(bottomBar);

		// -------------------------
		// CENTER CONTENT (DockPane.CENTER)
		// -------------------------
		// Center container for the card
		Center center = new Center();
		center.fillParent();
		dock.setCenter(center);

		Panel card = new Panel().background(uictx.theme.panelBg).border(uictx.theme.panelBorder, 1f);
		card.size(760, 520);
		card.padding = Insets.all(18);

		center.set(card);

		// Header text inside card
		Label title = new Label("SETTINGS").align(Label.AlignH.CENTER, Label.AlignV.TOP);

		Label hint = new Label("GRID LAYS OUT THE FORM. WRAP LAYS OUT THE BUTTONS.").font(small)
				.color(uictx.theme.mutedText);

		// Form grid: 5 rows x 2 cols
		// Col weights: label column smaller, control column larger
		GridPanel grid = new GridPanel(5, 2).gaps(14, 14).colWeights(0.35f, 0.65f);

		// Row 0: Player Name
		Label nameLbl = new Label("Player Name").font(small).color(uictx.theme.mutedText);
		nameField = new TextField().placeholder("TYPE A LOT TO TEST SCROLL…").font(small);
		nameField.prefW = 520;

		grid.add(nameLbl, 0, 0);
		grid.add(nameField, 0, 1);

		// Row 1: Mute
		Label muteLbl = new Label("Mute").font(small).color(uictx.theme.mutedText);
		muteBox = new CheckBox("Mute (demo)", false).font(small)
				.onChanged(() -> System.out.println("Mute: " + muteBox.isChecked()));

		grid.add(muteLbl, 1, 0);
		grid.add(muteBox, 1, 1);

		// Row 2: Volume
		Label volLbl = new Label("Volume").font(small).color(uictx.theme.mutedText);
		volumeSlider = new Slider("Volume", 0.65f).font(small)
				.onChanged(() -> System.out.println("Volume: " + volumeSlider.value()));

		grid.add(volLbl, 2, 0);
		grid.add(volumeSlider, 2, 1);

		// Row 3: Progress (demo)
		Label progLbl = new Label("Activity").font(small).color(uictx.theme.mutedText);
		progress = new ProgressBar("Menu Activity", 0.0f).font(small);

		grid.add(progLbl, 3, 0);
		grid.add(progress, 3, 1);

		// Row 4: Buttons (WrapPanel spans two columns)
		WrapPanel buttons = new WrapPanel().gaps(12, 12);
		buttons.prefH = 80;

		Font btnFont = small;

		Button resume = new Button("Resume").font(btnFont).onClick(() -> ctx.scenes().pop());
		Button print = new Button("Print State").font(btnFont).onClick(() -> {
			System.out.println("Name: " + (nameField != null ? nameField.getText() : ""));
			System.out.println("Mute: " + (muteBox != null && muteBox.isChecked()));
			System.out.println("Volume: " + (volumeSlider != null ? volumeSlider.value() : 0f));
		});
		Button reset = new Button("Reset").font(btnFont).onClick(() -> {
			if (nameField != null)
				nameField.setText("");
			if (muteBox != null)
				muteBox.checked(false);
			if (volumeSlider != null)
				volumeSlider.value(0.5f);
		});
		Button quit = new Button("Quit").font(btnFont).onClick(() -> ctx.window().getFrame().dispatchEvent(
				new java.awt.event.WindowEvent(ctx.window().getFrame(), java.awt.event.WindowEvent.WINDOW_CLOSING)));

		// Slightly smaller buttons so wrapping looks good
		resume.prefW = 200;
		print.prefW = 200;
		reset.prefW = 200;
		quit.prefW = 200;

		buttons.add(resume);
		buttons.add(print);
		buttons.add(reset);
		buttons.add(quit);

		grid.add(buttons, 4, 0, 1, 2); // span both columns

		// Build card content with a simple vertical stack using manual positions:
		// easiest: put title + hint + grid in a small internal DockPane
		DockPane cardDock = new DockPane();
		cardDock.fillParent();
		card.add(cardDock);

		// top block
		Panel topBlock = new Panel();
		topBlock.prefH = 90;
		topBlock.padding = Insets.hv(10, 0);

		DockPane topBlockDock = new DockPane();
		topBlockDock.fillParent();
		topBlock.add(topBlockDock);

		topBlockDock.addTop(title);
		topBlockDock.setCenter(hint);

		cardDock.addTop(topBlock);

		// center form grid
		cardDock.setCenter(grid);
	}

	@Override
	public void update(double dt) {
		// ESC closes pause menu
		if (ctx.actions().isPressed(Action.PAUSE)) {
			ctx.scenes().pop();
			ctx.actions().consumePressed(Action.PAUSE);
			return;
		}

		// Animate progress bar
		t += dt;
		if (progress != null) {
			float v = (float) ((t % 2.5) / 2.5);
			progress.value(v);
		}

		super.update(dt);
	}
}
