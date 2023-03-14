package com.sshtools.jajafx;

import static com.sshtools.jajafx.FXUtil.maybeQueue;

import java.net.URL;
import java.util.ResourceBundle;

import org.scenicview.ScenicView;

//import org.scenicview.ScenicView;

import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;
import com.jthemedetecor.OsThemeDetector;

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

	public enum DarkMode {
		AUTO, ALWAYS, NEVER
	}

	private Node content;
	private final URL icon;
	private final A container;
	private final String title;

	private Scene scene;
	private OsThemeDetector detector;

	private JMetro jMetro;
	private TitleBar titleBar;

	protected JajaFXApp(URL icon, String title, A container) {
		this.icon = icon;
		this.container = container;
		this.title = title;
	}

	public A getContainer() {
		return container;
	}

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(final Stage primaryStage) {
		detector = OsThemeDetector.getDetector();
		detector.registerListener(isDark -> {
			updateDarkMode();
		});
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
			return detector.isDark();
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
			ui.setTop(titleBar = new TitleBar());
		}
		content = createContent();
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
		scene.getStylesheets().add(JajaFXApp.class.getResource("Common.css").toExternalForm());
		
		try {
			ScenicView.show(scene);
		}
		catch(Throwable e) {
		}

		onScene(scene);
		return scene;
	}

	protected void onScene(Scene scene) {
	}
}