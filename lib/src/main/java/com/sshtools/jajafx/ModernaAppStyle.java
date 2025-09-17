package com.sshtools.jajafx;

import javafx.application.Application;

//import com.pixelduke.transit.TransitTheme;

import javafx.scene.Scene;

public class ModernaAppStyle implements AppStyle {

	public void init(Scene scene) {
		scene.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
	}

	@Override
	public void darkMode(boolean darkMode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean darkMode() {
		return false;
	}

	@Override
	public boolean supportsDarkMode() {
		return false;
	}

	@Override
	public boolean supportsAccent() {
		return false;
	}

	@Override
	public boolean supported() {
		return true;
	}
}
