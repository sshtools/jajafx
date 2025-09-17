package com.sshtools.jajafx.updateable;

import java.net.URL;
import java.util.prefs.Preferences;

import com.install4j.api.UiUtil;
import com.sshtools.jajafx.JajaFXApp;

import javafx.stage.Stage;

public abstract class UpdateableJajaFXApp<A, W extends UpdateableJajaFXAppWindow<? extends UpdateableJajaFXApp<A, W>>> extends JajaFXApp<A, W> {

	public static void main(String[] args) {
		launch(args);
	}
	
	protected UpdateableJajaFXApp(URL icon, String title, A container, Preferences appPreferences) {
		super(icon, title, container, appPreferences);
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

	@Override
	protected UpdateableJajaFXAppWindow<?> createAppWindow(final Stage stage) {
		var wnd = new UpdateableJajaFXAppWindow<UpdateableJajaFXApp<A, W>>(stage, this);
		@SuppressWarnings("unchecked")
        var cnt = createContent(stage, (W)wnd);
		wnd.setContent(cnt);
		return wnd;
	}


}