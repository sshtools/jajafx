package com.sshtools.jajafx;

import java.util.ResourceBundle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class AppLink extends Hyperlink {
	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(AppLink.class.getName());
	private final JajaFXApp<?> app;
	private final StringProperty url = new SimpleStringProperty();
	private boolean urlSet;
	private boolean adjusting;
	private MenuItem copy;
	private MenuItem open;

	{
		urlProperty().addListener((c, o, n) -> {
			if (!adjusting) {
				urlSet = true;
			}
		});
		textProperty().addListener((c, o, n) -> {
			if (!urlSet) {
				adjusting = true;
				try {
					url.set(n);
				} finally {
					adjusting = false;
				}
			}
		});
	}

	public AppLink() {
		/* For scenebuilder */
		this(null);
	}

	public AppLink(JajaFXApp<?> app) {
		this(app, null);
	}

	public AppLink(JajaFXApp<?> app, String text, String url, Node graphic) {
		super(text, graphic);
		this.app = app;
		init();
		if (url != null)
			setUrl(url);
	}

	public AppLink(JajaFXApp<?> app, String text, Node graphic) {
		this(app, text, text, graphic);
	}

	public AppLink(JajaFXApp<?> app, String text) {
		this(app, text, text);
	}

	public AppLink(JajaFXApp<?> app, String text, String url) {
		super(text);
		this.app = app;
		init();
		if (url != null)
			setUrl(url);
	}

	public final BooleanProperty copyProperty() {
		return copy.visibleProperty();
	}

	public final BooleanProperty openProperty() {
		return open.visibleProperty();
	}

	public final void setCopy(boolean copy) {
		this.copy.setVisible(copy);
	}

	public final void setOpen(boolean open) {
		this.open.setVisible(open);
	}

	public final boolean isCopy() {
		return copy.isVisible();
	}

	public boolean isOpen() {
		return open.isVisible();
	}

	public final String getUrl() {
		return url.get();
	}

	public final void setUrl(String url) {
		this.url.set(url);
	}

	public StringProperty urlProperty() {
		return url;
	}

	private JajaFXApp<?> getApp() {
		return app == null ? JajaApp.getInstance().getFXApp() : app;
	}

	private String calcUrl() {
		return getUrl() == null ? getText() : getUrl();
	}

	private void init() {
		var tooltip = new Tooltip();
		tooltip.textProperty().bind(url);

		copy = new MenuItem(RESOURCES.getString("copy"));
		copy.setOnAction(e -> copyToClipboard());

		open = new MenuItem(RESOURCES.getString("open"));
		open.setOnAction(e -> showDocument());

		setContextMenu(new ContextMenu(open, copy));
		setOnAction(e -> {
			if (isOpen())
				showDocument();
			else
				copyToClipboard();
		});
	}

	private void showDocument() {
		getApp().getHostServices().showDocument(calcUrl());
	}

	private void copyToClipboard() {
		var content = new ClipboardContent();
		content.putString(calcUrl());
		Clipboard.getSystemClipboard().setContent(content);
	}

}
