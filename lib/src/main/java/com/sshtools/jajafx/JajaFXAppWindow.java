package com.sshtools.jajafx;

import java.net.URL;

import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class JajaFXAppWindow {

	private JMetro jMetro;
	private TitleBar titleBar;
	private final Stage stage;
	private boolean showFrameTitle = true;
	private Label titleLabel;
	private ImageView titleImage;

	protected JajaFXApp<?> app;
	protected final Scene scene;
	protected final Node content;

	public JajaFXAppWindow(Stage stage, Node content, JajaFXApp<?> app) {
		this.stage = stage;
		this.app = app;
		this.content = content;
		
		stage.initStyle(app.isDecorated() ? StageStyle.DECORATED :  borderlessStageStyle());

		var ui = new BorderPane();
		if (!app.isDecorated()) {
			ui.setTop(titleBar = createTitleBar());
			checkFrameTitle();
		}
		app.addCommonStylesheets(
				content instanceof Parent ? ((Parent) content).getStylesheets() : content.getParent().getStylesheets());
		ui.setCenter(content);

		if (app.isDecorated()) {
			scene = new Scene(ui);
		} else {
			var primaryScene = new BorderlessScene(stage, borderlessStageStyle(), ui, 1, 1);

			primaryScene.setMoveControl(ui.getTop());
			primaryScene.setDoubleClickMaximizeEnabled(false);
			primaryScene.setSnapEnabled(false);
			primaryScene.removeDefaultCSS();
			primaryScene.setResizable(true);

			primaryScene.getRoot().getStyleClass().add("borderless-root");

			scene = primaryScene;
			scene.setFill(Color.TRANSPARENT);
		}
		stage.focusedProperty().addListener((c, o, n) -> {
			setStageFocusStyles(scene.getRoot(), n);
		});
		setStageFocusStyles(scene.getRoot(), stage.isFocused());
		setStagePlatformStyles(scene.getRoot());
		jMetro = new JMetro(app.isDarkMode() ? Style.DARK : Style.LIGHT);
		jMetro.setScene(scene);
		var stylesheets = scene.getStylesheets();
		app.addCommonStylesheets(stylesheets);

		stage.setWidth(760);
		stage.setHeight(680);
		stage.setScene(scene);

		app.getWindows().add(this); 
		updateDarkMode();
		
		stage.setOnHidden(this::onClose);
	}

	public ImageView getTitleImage() {
        return titleImage;
    }

    public void setTitleImage(ImageView titleImage) {
        this.titleImage = titleImage;
        checkFrameTitle();
    }

    public void updateDarkMode() {
		if (app.isDarkMode()) {
			jMetro.setStyle(Style.DARK);
		} else {
			jMetro.setStyle(Style.LIGHT);
		}
		app.updateRootStyles(scene.getRoot());
	}

	public void onClose(WindowEvent evt) {
		app.getWindows().remove(this);
	}
	
	public Node content() {
		return content;
	}

	public Scene scene() {
		return scene;
	}

	public final Stage stage() {
		return stage;
	}

	public final TitleBar titleBar() {
		return titleBar;
	}

	public StageStyle borderlessStageStyle() {
		return StageStyle.TRANSPARENT;
	}

	protected TitleBar createTitleBar() {
		return Platforms.style().titleBar();
	}

	protected ImageView createTitleImage() {
		var titleImage = new ImageView(new Image(getTitleBarImage().toExternalForm()));
		titleImage.setFitHeight(150);
		titleImage.setFitWidth(200);
		titleImage.setPreserveRatio(true);
		return titleImage;
	}

	protected URL getTitleBarImage() {
		return JajaFXApp.class.getResource("jadaptive-logo.png");
	}

	protected void setStagePlatformStyles(Node ui) {
		Platforms.style().configureStageRootStyles(ui);
	}
	
	protected void setStageFocusStyles(Node ui, Boolean n) {
		var sc = ui.getStyleClass();
		if (n) {
			sc.add("stage-focused");
			sc.remove("stage-unfocused");
		} else {
			sc.remove("stage-focused");
			sc.add("stage-unfocused");
		}
	}

	final void checkFrameTitle() {
		if (titleBar != null) {
			if (showFrameTitle && titleLabel == null) {
				if (titleImage != null) {
					titleBar.getTitleStack().getChildren().remove(titleImage);
				}
				titleLabel = new Label();
				titleLabel.getStyleClass().add("title-label");
				titleLabel.textProperty().bind(stage.titleProperty());
				titleBar.getTitleStack().getChildren().add(titleLabel);
			} else if (!showFrameTitle && titleImage == null) {
				if (titleLabel != null) {
					titleBar.getTitleStack().getChildren().remove(titleLabel);
				}
				titleImage = createTitleImage();
				titleBar.getTitleStack().getChildren().add(titleImage);
			}
		}
	}
}
