package com.sshtools.jajafx;

//import com.pixelduke.transit.TransitTheme;

import javafx.scene.Scene;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

public class AppStyle {
	private JMetro jMetro;
//	private TransitTheme theme;

	public enum Mode {
		DARK, LIGHT
	}
	
	public AppStyle(Scene scene, boolean darkMode) {

		jMetro = new JMetro(darkMode ? Style.DARK : Style.LIGHT);
		jMetro.setScene(scene);
		
//		theme = new TransitTheme(darkMode ? com.pixelduke.transit.Style.DARK : com.pixelduke.transit.Style.LIGHT);
//		theme.setScene(scene);
	}

	public void updateDarkMode(boolean darkMode) {
		if (darkMode) {
			jMetro.setStyle(Style.DARK);
		} else {
			jMetro.setStyle(Style.LIGHT);
		}
//		if (darkMode) {
//			theme.setStyle(com.pixelduke.transit.Style.DARK);
//		} else {
//			theme.setStyle(com.pixelduke.transit.Style.LIGHT);
//		}
		
	}
}
