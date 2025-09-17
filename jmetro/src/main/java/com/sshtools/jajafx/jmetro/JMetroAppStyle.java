package com.sshtools.jajafx.jmetro;

import java.util.Objects;

import com.sshtools.jajafx.AppStyle;

//import com.pixelduke.transit.TransitTheme;

import javafx.scene.Scene;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class JMetroAppStyle implements AppStyle { 
	private JMetro jMetro;
	private Scene scene;
	private boolean darkMode;

	public void init(Scene scene) {
		jMetro = new JMetro(darkMode ? Style.DARK : Style.LIGHT);
		jMetro.setScene(scene);
	}

	@Override
	public void darkMode(boolean darkMode) {
		if(!Objects.equals(darkMode, this.darkMode)) {
			this.darkMode = darkMode;
			if(scene != null) {
				if (darkMode) {
					jMetro.setStyle(Style.DARK);
				} else {
					jMetro.setStyle(Style.LIGHT);
				}
			}
		}
	}

	@Override
	public boolean supported() {
		try {
			getClass().getClassLoader().loadClass("jfxtras.styles.jmetro.JMetro");
			return true;
		}
		catch(Exception e) {
			return false;
		}
	}

	@Override
	public boolean darkMode() {
		return jMetro.getStyle() == Style.DARK;
	}

	@Override
	public boolean supportsDarkMode() {
		return true;
	}

	@Override
	public boolean supportsAccent() {
		return true;
	}
}
