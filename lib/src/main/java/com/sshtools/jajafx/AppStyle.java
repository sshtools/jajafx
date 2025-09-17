package com.sshtools.jajafx;

import java.util.ServiceLoader;

import javafx.scene.Scene;

public interface AppStyle {
	
	public final static class Defaults {
		public final static AppStyle DEFAULT = findDefault();
		
		private final static AppStyle findDefault() {
			return ServiceLoader.load(AppStyle.class).stream().filter(s->s.get().supported()).
					map(s->s.get()).findFirst().orElseThrow(() -> new IllegalStateException("No app styles."));
		}
	}

	public enum Mode {
		DARK, LIGHT
	}
	
	@Deprecated
	default void updateDarkMode(boolean darkMode) {
		darkMode(darkMode);
	}
	
	boolean supported();

	void init(Scene scene);
	
	void darkMode(boolean darkMode);
	
	boolean darkMode();
	
	boolean supportsDarkMode();
	
	boolean supportsAccent();

	static AppStyle getDefault() {
		return Defaults.DEFAULT;
	}
	
}
