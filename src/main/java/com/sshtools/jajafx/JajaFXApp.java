package com.sshtools.jajafx;

import java.net.URL;
import java.util.List;

import com.install4j.api.UiUtil;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public abstract class JajaFXApp<A extends JajaApp<? extends JajaFXApp<A>>> extends Application {

	public enum DarkMode {
		AUTO, ALWAYS, NEVER
	}

	public static void main(String[] args) {
		launch(args);
	}

	private final URL icon;
	private final A container;

	private final String title;
	private Stage primaryStage;
	private boolean defaultStandardWindowDecorations;
	private boolean showFrameTitle = true;
	private final ObservableList<JajaFXAppWindow> windows = FXCollections.observableArrayList();
	
	protected JajaFXApp(URL icon, String title, A container) {
		this.icon = icon;
		this.container = container;
		this.title = title;
		
		container.init(this);
	}

	public void addCommonStylesheets(ObservableList<String> stylesheets) {
		FXUtil.addIfNotAdded(stylesheets, JajaFXApp.class.getResource("Common.css").toExternalForm());
		var appResource = getClass().getResource("App.css");
		if(appResource != null) {
			FXUtil.addIfNotAdded(stylesheets, appResource.toExternalForm());
		}
	}

	public final A getContainer() {
		return container;
	}

	public URL getIcon() {
		return icon;
	}

	public final boolean isDarkMode() {
		var mode = getDarkMode();
		if (mode.equals(DarkMode.AUTO))
			return UiUtil.isDarkDesktop();
		else if (mode.equals(DarkMode.ALWAYS))
			return true;
		else
			return false;
	}

	public final Boolean isDecorated() {
		return JajaApp.getInstance().standardWindowDecorations.orElse(defaultStandardWindowDecorations);
	}

	public final boolean isDefaultStandardWindowDecorations() {
		return defaultStandardWindowDecorations;
	}
	
	public final boolean isShowFrameTitle() {
		return showFrameTitle;
	}
	
	public final void setDefaultStandardWindowDecorations(boolean defaultStandardWindowDecorations) {
		this.defaultStandardWindowDecorations = defaultStandardWindowDecorations;
	}
	
	public final void setShowFrameTitle(boolean showFrameTitle) {
		this.showFrameTitle = showFrameTitle;
		windows.forEach(JajaFXAppWindow::checkFrameTitle);
	}

	@Override
	public void start(final Stage primaryStage) {
		this.primaryStage = primaryStage;
		listenForDarkModeChanges();

		newAppWindow(primaryStage);
		primaryStage.show();

		var updateService = getContainer().getUpdateService();
		updateService.needsUpdatingProperty().addListener((c, o, n) -> needUpdate());
		updateService.rescheduleCheck();
		
		onStarted();
	}

	@SuppressWarnings("unchecked")
	public <W extends JajaFXAppWindow> W newAppWindow(final Stage primaryStage) {
		primaryStage.setTitle(title);
		
		var appWindow = createAppWindow(primaryStage);
		primaryStage.getIcons().add(new Image(icon.toExternalForm()));
		onConfigurePrimaryStage(primaryStage);
		return (W)appWindow;
	}

	public final List<JajaFXAppWindow> getWindows() {
		return windows;
	}

	public void updateDarkMode(Parent root) {
		if (isDarkMode()) {
			root.getStyleClass().remove("lightMode");
			root.getStyleClass().add("darkMode");
		} else {
			root.getStyleClass().remove("darkMode");
			root.getStyleClass().add("lightMode");
		}
	}

	protected JajaFXAppWindow createAppWindow(final Stage stage) {
		return new JajaFXAppWindow(stage, createContent(stage), this);
	}

	protected abstract Node createContent(Stage stage);

	protected DarkMode getDarkMode() {
		return DarkMode.valueOf(getContainer().getAppPreferences().get("darkMode", DarkMode.AUTO.name()));
	}
	
	protected void listenForDarkModeChanges() {
		getContainer().getAppPreferences().addPreferenceChangeListener(pce -> {
			if (pce.getKey().equals("darkMode")) {
				updateDarkMode();
			}
		});
	}

	protected void needUpdate() {
		//
	}

	protected void onConfigurePrimaryStage(Stage stage) {
		primaryStage.centerOnScreen();
	}

	protected void onStarted() {
	}

	protected void updateDarkMode() {
		windows.forEach(JajaFXAppWindow::updateDarkMode);
	}

}