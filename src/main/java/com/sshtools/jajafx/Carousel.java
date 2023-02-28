package com.sshtools.jajafx;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.Pane;

public class Carousel extends Pane {
	private Flinger devices;
	private Hyperlink scrollLeft;
	private Hyperlink scrollRight;

	public Carousel() {

		devices = new Flinger();
		devices.setMaxWidth(Double.MAX_VALUE);

		scrollLeft = new Hyperlink();
		scrollLeft.setGraphic(new FontIcon(FontAwesomeSolid.CARET_LEFT));
		scrollLeft.getStyleClass().addAll("navigation-icon", "large-icon", "icon-accent");
		scrollLeft.visibleProperty().bind(Bindings.not(devices.leftOrUpDisableProperty()));
		scrollLeft.setOnAction((e) -> devices.slideLeftOrUp());
		scrollRight = new Hyperlink();
		scrollLeft.getStyleClass().addAll("navigation-icon", "large-icon", "icon-accent");
		scrollRight.setGraphic(new FontIcon(FontAwesomeSolid.CARET_RIGHT));
		scrollRight.visibleProperty().bind(Bindings.not(devices.rightOrDownDisableProperty()));
		scrollRight.setOnAction((e) -> devices.slideRightOrDown());

		getChildren().addAll(devices, scrollLeft, scrollRight);
	}

	public final Flinger getDevices() {
		return devices;
	}

	public final Hyperlink getScrollLeft() {
		return scrollLeft;
	}

	public final Hyperlink getScrollRight() {
		return scrollRight;
	}

	public ObservableList<Node> getItems() {
		return devices.getContent().getChildren();
	}

	public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
		return devices.onActionProperty();
	}

	public final void setOnAction(EventHandler<ActionEvent> value) {
		devices.setOnAction(value);
	}

	@Override
	protected void layoutChildren() {
		super.layoutChildren();
		devices.setLayoutX(0);
		devices.setLayoutY(0);
		devices.resize(getWidth(), getHeight());
		scrollRight.setLayoutX(getWidth() - scrollRight.getWidth());
		scrollLeft.setLayoutY(getHeight() - scrollLeft.getHeight());
		scrollRight.setLayoutY(getHeight() - scrollRight.getHeight());
	}
}