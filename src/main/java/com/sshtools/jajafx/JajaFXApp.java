package com.sshtools.jajafx;

import static com.sshtools.jajafx.FXUtil.maybeQueue;

import java.net.URL;

import org.scenicview.ScenicView;

//import org.scenicview.ScenicView;

import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;
import com.install4j.api.UiUtil;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public abstract class JajaFXApp<A extends JajaApp<? extends JajaFXApp<A>>> extends Application {

	public enum DarkMode {
		AUTO, ALWAYS, NEVER
	}

	private Node content;
	private final URL icon;
	private final A container;
	private final String title;

	private Scene scene;

	private JMetro jMetro;
	private TitleBar titleBar;
	private Stage primaryStage;

	protected JajaFXApp(URL icon, String title, A container) {
		this.icon = icon;
		this.container = container;
		this.title = title;
		
		container.init(this);
	}

	public final Stage getPrimaryStage() {
		return primaryStage;
	}

	public A getContainer() {
		return container;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		this.primaryStage = primaryStage;
		getContainer().getAppPreferences().addPreferenceChangeListener(pce -> {
			if (pce.getKey().equals("darkMode")) {
				updateDarkMode();
			}
		});

		primaryStage.setTitle(title);
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
		
		onStarted();
	}
	
	protected void onStarted() {
		
	}

	private void updateDarkMode() {
		maybeQueue(() -> {
			if(isDarkMode()) {
				jMetro.setStyle(Style.DARK);
				scene.getRoot().getStyleClass().remove("lightMode");
				scene.getRoot().getStyleClass().add("darkMode");
			}
			else {
				jMetro.setStyle(Style.LIGHT);
				scene.getRoot().getStyleClass().remove("darkMode");
				scene.getRoot().getStyleClass().add("lightMode");
			}
		});
	}

	public boolean isDarkMode() {
		var mode = DarkMode.valueOf(getContainer().getAppPreferences().get("darkMode", DarkMode.AUTO.name()));
		if (mode.equals(DarkMode.AUTO))
			return UiUtil.isDarkDesktop();
		else if (mode.equals(DarkMode.ALWAYS))
			return true;
		else
			return false;
	}
	
	public TitleBar getTitleBar() {
		return titleBar;
	}

	protected void needUpdate() {
		//
	}

	protected abstract Node createContent();

	private Scene createScene(final Stage primaryStage) {

		var ui = new BorderPane();
		if (!JajaApp.getInstance().standardWindowDecorations) {
			ui.setTop(titleBar = createTitleBar());
		}
		content = createContent();
		addCommonStylesheets(content instanceof Parent ? ((Parent)content).getStylesheets() : content.getParent().getStylesheets());
		ui.setCenter(content);

		if (JajaApp.getInstance().standardWindowDecorations) {
			scene = new Scene(ui);
		} else {
			var primaryScene = new BorderlessScene(primaryStage, StageStyle.UNDECORATED, ui, 1, 1);

			primaryScene.setMoveControl(ui.getTop());
			primaryScene.setDoubleClickMaximizeEnabled(false);
			primaryScene.setSnapEnabled(false);
			primaryScene.removeDefaultCSS();
			primaryScene.setResizable(true);

			primaryScene.getRoot().setStyle("-fx-background-color: background_color;");
			primaryScene.getRoot().getStyleClass().add("borderless-root");

			scene = primaryScene;
		}
		jMetro = new JMetro(isDarkMode() ? Style.DARK : Style.LIGHT);
		updateDarkMode();
		jMetro.setScene(scene);
		var stylesheets = scene.getStylesheets();
		addCommonStylesheets(stylesheets);
		
		try {
			if(Boolean.getBoolean("jaja.debugScene"))
				ScenicView.show(scene);
		}
		catch(Throwable e) {
		}

		onScene(scene);
		return scene;
	}

	public void addCommonStylesheets(ObservableList<String> stylesheets) {
		FXUtil.addIfNotAdded(stylesheets, JajaFXApp.class.getResource("Common.css").toExternalForm());
		var appResource = getClass().getResource("App.css");
		if(appResource != null) {
			FXUtil.addIfNotAdded(stylesheets, appResource.toExternalForm());
		}
	}

	protected TitleBar createTitleBar() {
		return new TitleBar();
	}

	protected void onScene(Scene scene) {
	}
}