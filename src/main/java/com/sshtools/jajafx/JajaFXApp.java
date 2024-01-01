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
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
	private boolean defaultStandardWindowDecorations;
	private boolean showFrameTitle = true;
	private Label titleLabel;
	private ImageView titleImage;

	protected JajaFXApp(URL icon, String title, A container) {
		this.icon = icon;
		this.container = container;
		this.title = title;
		
		container.init(this);
	}
	
	public boolean isShowFrameTitle() {
		return showFrameTitle;
	}

	public void setShowFrameTitle(boolean showFrameTitle) {
		this.showFrameTitle = showFrameTitle;
		checkFrameTitle();
	}

	public final boolean isDefaultStandardWindowDecorations() {
		return defaultStandardWindowDecorations;
	}

	public final void setDefaultStandardWindowDecorations(boolean defaultStandardWindowDecorations) {
		this.defaultStandardWindowDecorations = defaultStandardWindowDecorations;
	}

	public URL getIcon() {
		return icon;
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
		primaryStage.setOnCloseRequest((evt) -> {
			System.exit(0);
		});
		var scene = createScene(primaryStage);
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(icon.toExternalForm()));
		onConfigureStage(primaryStage);
		primaryStage.show();

		var updateService = getContainer().getUpdateService();
		updateService.needsUpdatingProperty().addListener((c, o, n) -> needUpdate());
		updateService.rescheduleCheck();
		
		onStarted();
	}
	
	protected void onConfigureStage(Stage stage) {
		primaryStage.setWidth(760);
		primaryStage.setHeight(680);
		primaryStage.centerOnScreen();
	}
	
	protected void onStarted() {
	}
	
	protected void updateDarkMode() {
		updateDarkMode(jMetro, scene.getRoot());
	}

	protected void updateDarkMode(JMetro jMetro, Parent root) {
		maybeQueue(() -> {
			if(isDarkMode()) {
				jMetro.setStyle(Style.DARK);
			}
			else {
				jMetro.setStyle(Style.LIGHT);
			}
			applyStylesToRoot(root);
		});
	}
	
	public void applyStylesToRoot(Parent root) {
		maybeQueue(() -> {
			if(isDarkMode()) {
				jMetro.setStyle(Style.DARK);
				root.getStyleClass().remove("lightMode");
				root.getStyleClass().add("darkMode");
			}
			else {
				jMetro.setStyle(Style.LIGHT);
				root.getStyleClass().remove("darkMode");
				root.getStyleClass().add("lightMode");
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
	
	private void checkFrameTitle() {
		if(titleBar != null) {
			if(showFrameTitle && titleLabel == null) {
				if(titleImage != null) {
					titleBar.getTitleStack().getChildren().remove(titleImage);
				}
				titleLabel = new Label();
				titleLabel.getStyleClass().add("title-label");
				titleLabel.textProperty().bind(getPrimaryStage().titleProperty());
				titleBar.getTitleStack().getChildren().add(titleLabel);
			}
			else if(!showFrameTitle && titleImage == null) {
				if(titleLabel != null) {
					titleBar.getTitleStack().getChildren().remove(titleLabel);
				}
				titleImage = createTitleImage();
				titleBar.getTitleStack().getChildren().add(titleImage);
			}
		}
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
	
	private Scene createScene(final Stage primaryStage) {

		var ui = new BorderPane();
		if (!isDecorated()) {
			ui.setTop(titleBar = createTitleBar());
			checkFrameTitle();
		}
		content = createContent();
		addCommonStylesheets(content instanceof Parent ? ((Parent)content).getStylesheets() : content.getParent().getStylesheets());
		ui.setCenter(content);

		if (isDecorated()) {
			scene = new Scene(ui);
		} else {
			var primaryScene = new BorderlessScene(primaryStage, borderlessStageStyle(), ui, 1, 1);

			primaryScene.setMoveControl(ui.getTop());
			primaryScene.setDoubleClickMaximizeEnabled(false);
			primaryScene.setSnapEnabled(false);
			primaryScene.removeDefaultCSS();
			primaryScene.setResizable(true);

			//primaryScene.getRoot().setStyle("-fx-background-color: background_color;");
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
			e.printStackTrace();
		}

		onScene(scene);
		return scene;
	}

	public Boolean isDecorated() {
		return JajaApp.getInstance().standardWindowDecorations.orElse(defaultStandardWindowDecorations);
	}

	protected StageStyle borderlessStageStyle() {
		return StageStyle.UNDECORATED;
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