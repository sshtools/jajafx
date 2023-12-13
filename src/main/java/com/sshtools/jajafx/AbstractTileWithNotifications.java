package com.sshtools.jajafx;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.controlsfx.control.NotificationPane;

import javafx.scene.Node;

public class AbstractTileWithNotifications<C> extends AbstractTile<C> {

	private NotificationPane notifications;
	private final ResourceBundle resources;
	
	protected AbstractTileWithNotifications(ResourceBundle resources) {
		this.resources = resources;
	}

	@Override
	protected final void onConfigure() {
		notificationSetup();
		onConfigureWithNotifications();
	}

	protected void onConfigureWithNotifications() {
	}

	private void notificationSetup() {
		var wasRoot = getScene().getRoot();
		notifications = new NotificationPane(wasRoot);
		getScene().setRoot(notifications);

		// setup up notification pane properties
//		notifications.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
	}
	
	protected void clearNotifications() {
		notifications.hide();
	}

	protected void message(Node graphic, String style, String key, Object... args) {
		notifications.getStyleClass().removeAll("notification-danger", "notification-warning", "notification-info",
				"notification-success");
		notifications.setGraphic(graphic);

		var msg = MessageFormat.format(resources.getString(key), args);
		notifications.getStyleClass().add(style);
		notifications.show(msg);
	}

}
