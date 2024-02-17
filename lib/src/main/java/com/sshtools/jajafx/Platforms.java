package com.sshtools.jajafx;

import java.net.URL;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class Platforms {

	private final static class Default {
		private final static PlatformStyle DEFAULT = createPlatformStyle();

		private static PlatformStyle createPlatformStyle() {
			if (isLinux()) {
				return new LinuxPlatformStyle();
			} else if (isWindows()) {
				return new WindowsPlatformStyle();
			} else if (isMacOs()) {
				return new MacOsPlatformStyle();
			} else {
				return new DefaultPlatformStyle();
			}
		}
	}

	public static PlatformStyle style() {
		return Default.DEFAULT;
	}

	public final static boolean isLinux() {
		return System.getProperty("jajafx.fakeTitleBarOs", System.getProperty("os.name", "")).toLowerCase()
				.contains("linux");
	}

	public final static boolean isMacOs() {
		return System.getProperty("jajafx.fakeTitleBarOs", System.getProperty("os.name", "")).toLowerCase()
				.contains("mac os");
	}

	public final static boolean isWindows() {
		return System.getProperty("jajafx.fakeTitleBarOs", System.getProperty("os.name", "")).toLowerCase()
				.contains("windows");
	}

	private abstract static class AbstractPlatformStyle implements PlatformStyle {

		private String styleName;

		protected AbstractPlatformStyle(String styleName) {
			this.styleName = styleName;
		}

		@Override
		public void configureStageRootStyles(Node root) {
			root.getStyleClass().add(styleName);
		}

		@Override
		public Node accessory(Node accessory) {
			var g = new HBox(accessory);
			g.getStyleClass().add("accessory-wrapper");
			g.setAlignment(Pos.CENTER);
			g.setPrefWidth(22);
			return g;
		}
	}

	private final static class DefaultPlatformStyle extends AbstractPlatformStyle {
		protected DefaultPlatformStyle() {
			super("platform-default");
		}

		@Override
		public TitleBar titleBar() {
			return new TitleBar();
		}

		@Override
		public URL css() {
			return Platforms.class.getResource("Default.css");
		}
	}

	private final static class LinuxPlatformStyle extends AbstractPlatformStyle {
		protected LinuxPlatformStyle() {
			super("platform-linux");
		}

		@Override
		public TitleBar titleBar() {
			return new LinuxTitleBar();
		}

		@Override
		public URL css() {
			return Platforms.class.getResource("Linux.css");
		}

		@Override
		public Node accessory(Node accessory) {
			var g = new HBox(accessory);
			g.getStyleClass().add("accessory-wrapper");
			g.setAlignment(Pos.CENTER);
			g.setPrefWidth(38);
			return g;
		}
	}

	private final static class WindowsPlatformStyle extends AbstractPlatformStyle {

		protected WindowsPlatformStyle() {
			super("platform-windows");
		}

		@Override
		public TitleBar titleBar() {
			return new WindowsTitleBar();
		}

		@Override
		public Node accessory(Node accessory) {
			var acc = super.accessory(accessory);
			((HBox)acc).setPrefWidth(44);
			return acc;
		}

		@Override
		public URL css() {
			return Platforms.class.getResource("Windows.css");
		}

	}

	private final static class MacOsPlatformStyle extends AbstractPlatformStyle {
		protected MacOsPlatformStyle() {
			super("platform-macos");
		}

		@Override
		public TitleBar titleBar() {
			return new MacOSTitleBar();
		}

		@Override
		public URL css() {
			return Platforms.class.getResource("MacOs.css");
		}

		@Override
		public Node accessory(Node accessory) {
			var acc = super.accessory(accessory);
			((HBox)acc).setPrefHeight(24);
			((HBox)acc).setPrefWidth(24);
			return acc;
		}
	}
}
