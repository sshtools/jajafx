package com.sshtools.jajafx.transit;

import java.util.Objects;

import com.pixelduke.transit.Style;
import com.pixelduke.transit.TransitTheme;
import com.sshtools.jajafx.AppStyle;

//import com.pixelduke.transit.TransitTheme;

import javafx.scene.Scene;

public class TransitAppStyle implements AppStyle {
	private TransitTheme theme;
	private Scene scene;
	private boolean darkMode;

	public void init(Scene scene) {
		this.scene = scene;
		theme = new TransitTheme(darkMode ? Style.DARK : Style.LIGHT);
		theme.setScene(scene);
	}

	@Override
	public void darkMode(boolean darkMode) {
		if(!Objects.equals(darkMode, this.darkMode)) {
			this.darkMode = darkMode;
			if(scene != null) {
				if (darkMode) {
					theme.setStyle(Style.DARK);
				} else {
					theme.setStyle(Style.LIGHT);
				}
			}
		}
	}

	@Override
	public boolean darkMode() {
		return theme.getStyle() == Style.DARK;
	}

	@Override
	public boolean supportsDarkMode() {
		return true;
	}

	@Override
	public boolean supportsAccent() {
		return true;
	}

	@Override
	public boolean supported() {
		try {
			getClass().getClassLoader().loadClass("com.pixelduke.transit.TransitTheme");
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}
}
