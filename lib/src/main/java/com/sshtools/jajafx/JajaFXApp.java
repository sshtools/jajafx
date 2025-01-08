package com.sshtools.jajafx;

import java.net.URL;
import java.util.List;
import java.util.prefs.Preferences;

import org.scenicview.ScenicView;

import com.install4j.api.UiUtil;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public abstract class JajaFXApp<A, W extends JajaFXAppWindow<? extends JajaFXApp<A, W>>> extends Application {

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
	private final ObservableList<JajaFXAppWindow<?>> windows = FXCollections.observableArrayList();
	private final Preferences appPreferences;
	
	@SuppressWarnings("unchecked")
	protected JajaFXApp(URL icon, String title, A container, Preferences appPreferences) {
		this.icon = icon;
		this.container = container;
		this.title = title;
		this.appPreferences = appPreferences;
		if(container instanceof JajaApp jja) {
			jja.init(this);
		}
	}

	public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void addCommonStylesheets(ObservableList<String> stylesheets) {
		FXUtil.addIfNotAdded(stylesheets, JajaFXApp.class.getResource("Common.css").toExternalForm());
		FXUtil.addIfNotAdded(stylesheets, Platforms.style().css().toExternalForm());
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

	public final boolean isDecorated() {
		return JajaApp.getInstance() == null ? false : JajaApp.getInstance().standardWindowDecorations.orElse(defaultStandardWindowDecorations);
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

		onStarted();
		
		try {
			if(Boolean.getBoolean("jaja.debugScene"))
				ScenicView.show(primaryStage.getScene());
		}
		catch(Throwable e) {
		}
	}

	@SuppressWarnings("unchecked")
	public <W extends JajaFXAppWindow<?>> W newAppWindow(final Stage primaryStage) {
		primaryStage.setTitle(title);
		
		var appWindow = createAppWindow(primaryStage);
		primaryStage.getIcons().add(new Image(icon.toExternalForm()));
		onConfigurePrimaryStage(appWindow, primaryStage);
		return (W)appWindow;
	}

	@SuppressWarnings("unchecked")
	public final  <W extends JajaFXAppWindow<?>> List<W> getWindows() {
		return (List<W>)windows;
	}

	public void updateRootStyles(Parent root) {
		if (isDarkMode()) {
			root.getStyleClass().remove("lightMode");
			root.getStyleClass().add("darkMode");
		} else {
			root.getStyleClass().remove("darkMode");
			root.getStyleClass().add("lightMode");
		}
	}

	protected JajaFXAppWindow<?> createAppWindow(final Stage stage) {
		var wnd = new JajaFXAppWindow<JajaFXApp<A, W>>(stage, this);
		@SuppressWarnings("unchecked")
        var cnt = createContent(stage, (W)wnd);
		wnd.setContent(cnt);
		return wnd;
	}

	protected abstract Node createContent(Stage stage, W window);

	protected DarkMode getDarkMode() {
		return DarkMode.valueOf(appPreferences.get("darkMode", DarkMode.AUTO.name()).toUpperCase());
	}
	
	protected void listenForDarkModeChanges() {
	    appPreferences.addPreferenceChangeListener(pce -> {
			if (pce.getKey().equals("darkMode")) {
				updateDarkMode();
			}
		});
	}

	public void needUpdate() {
		//
	}

	protected void onConfigurePrimaryStage(JajaFXAppWindow<?> wnd, Stage stage) {
		primaryStage.centerOnScreen();
	}

	protected void onStarted() {
	}

	protected void updateDarkMode() {
		windows.forEach(JajaFXAppWindow::updateDarkMode);
	}

}