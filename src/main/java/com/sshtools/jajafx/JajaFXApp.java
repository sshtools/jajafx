package com.sshtools.jajafx;

import java.net.URL;
import java.util.ResourceBundle;

//import org.scenicview.ScenicView;

import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public abstract class JajaFXApp<A extends JajaApp<? extends JajaFXApp<A>>> extends Application {
	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(JajaApp.class.getName());

	private Node content;
	private final URL icon;
	private final A container;

	protected JajaFXApp(URL icon, A container) {
		this.icon = icon;
		this.container = container;
	}

	public A getContainer() {
		return container;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		primaryStage.setTitle(RESOURCES.getString("title"));
		var scene = createScene(primaryStage);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(icon.toExternalForm()));
		primaryStage.setWidth(760);
		primaryStage.setHeight(680);
		primaryStage.centerOnScreen();
		primaryStage.show();
		primaryStage.setOnCloseRequest((evt) -> {
			System.exit(0);
		});

		var updateService = getContainer().getUpdateService();
		updateService.needsUpdatingProperty().addListener((c, o, n) -> needUpdate());
		updateService.rescheduleCheck();
	}

	protected void needUpdate() {
		//
	}

	protected abstract Node createContent();

	private Scene createScene(final Stage primaryStage) {
		content = createContent();

		var ui = new BorderPane();
		if (!JajaApp.getInstance().standardWindowDecorations) {
			ui.setTop(new TitleBar());
		}
		ui.setCenter(content);

		Scene scene;
		if (JajaApp.getInstance().standardWindowDecorations) {
			scene = new Scene(ui);
		} else {
			var primaryScene = new BorderlessScene(primaryStage, StageStyle.UNDECORATED, ui, 1, 1);

			primaryScene.setMoveControl(ui);
			primaryScene.setDoubleClickMaximizeEnabled(false);
			primaryScene.setSnapEnabled(false);
			primaryScene.removeDefaultCSS();
			primaryScene.setResizable(true);

			primaryScene.getRoot().setStyle("-fx-background-color: tab_pane_background_color;");
			primaryScene.getRoot().getStyleClass().add("borderless-root");

			scene = primaryScene;
		}
		new JMetro(JajaApp.getInstance().darkMode ? Style.DARK : Style.LIGHT).setScene(scene);
		scene.getStylesheets().add(JajaFXApp.class.getResource("Common.css").toExternalForm());
//		ScenicView.show(scene);

		return scene;
	}
}