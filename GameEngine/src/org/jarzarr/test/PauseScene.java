package org.jarzarr.test;

import java.awt.Color;
import java.awt.Font;

import org.jarzarr.core.EngineContext;
import org.jarzarr.test.Main.Action;
import org.jarzarr.ui.Insets;
import org.jarzarr.ui.UIRoot;
import org.jarzarr.ui.UIScene;
import org.jarzarr.ui.layout.Center;
import org.jarzarr.ui.layout.StackPanel;
import org.jarzarr.ui.layout.StackPanel.Direction;
import org.jarzarr.ui.widgets.Button;
import org.jarzarr.ui.widgets.Label;
import org.jarzarr.ui.widgets.Panel;

public class PauseScene extends UIScene<Action> {

	private static final String FONT_PATH = "assets/fonts/pixel-font.ttf";

	public PauseScene(EngineContext<Action> ctx) {
		super(ctx);
	}

	@Override
	protected void buildUI(UIRoot root) {
		// Font
		Font pixel = ctx.assets().getFont(FONT_PATH, 32f);
		uictx.defaultFont = pixel;

		// Overlay background
		Panel overlay = new Panel().background(new Color(0, 0, 0, 170));
		overlay.width = -1; // fill parent in our UINode rule
		overlay.height = -1;

		// Card
		Panel card = new Panel().background(new Color(20, 20, 20, 220)).border(new Color(255, 255, 255, 40), 1f);
		card.width = 520;
		card.height = 300;
		card.padding = Insets.all(18);

		Label title = new Label("PAUSED").align(Label.AlignH.CENTER, Label.AlignV.TOP);
		title.prefH = 48;

		Label hint1 = new Label("ESC TO RESUME");
		Label hint2 = new Label("LEFT CLICK TO PLAY SOUND");

		Button resume = new Button("Resume").onClick(() -> {
			ctx.scenes().pop();
		});

		Button quit = new Button("Quit").onClick(() -> {
			// You can decide engine behavior. For test: stop the loop cleanly
			// (If you prefer System.exit, do that in your app code, not engine.)
			// Engine.stop() isn't exposed here, so keep it simple:
			ctx.window().getFrame().dispatchEvent(
					new java.awt.event.WindowEvent(ctx.window().getFrame(), java.awt.event.WindowEvent.WINDOW_CLOSING));
		});

		StackPanel col = new StackPanel(Direction.VERTICAL).spacing(12);
		col.add(title);
		col.add(hint1);
		col.add(hint2);
		col.add(resume);
		col.add(quit);

		card.add(col);

		Center center = new Center().set(card);
		overlay.add(center);

		root.add(overlay);
	}

	@Override
	public void update(double dt) {
		// Keep ESC behavior (still works)
		if (ctx.actions().isPressed(Action.PAUSE)) {
			ctx.scenes().pop();
			ctx.actions().consumePressed(Action.PAUSE);
			return;
		}
		super.update(dt);
	}
}
