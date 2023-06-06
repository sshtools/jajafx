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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

public class Carousel extends Pane {
	private Flinger devices;
	private MaskedView mask;
	private Hyperlink scrollLeft;
	private Hyperlink scrollRight;
//	private HBox hbox;

	public Carousel() {

		devices = new Flinger();
		devices.setMaxWidth(Double.MAX_VALUE);
		
//		hbox = new HBox();

		mask = new MaskedView(devices);
//		mask = new MaskedView(hbox);

		scrollLeft = new Hyperlink();
		var leftIcon = new FontIcon(FontAwesomeSolid.CARET_LEFT);
		scrollLeft.setGraphic(leftIcon);
		leftIcon.getStyleClass().addAll("navigation-icon", "large-icon", "icon-accent");
		scrollLeft.visibleProperty().bind(Bindings.not(devices.leftOrUpDisableProperty()));
		scrollLeft.setOnAction((e) -> devices.slideLeftOrUp());
		scrollRight = new Hyperlink();
		var rightIcon = new FontIcon(FontAwesomeSolid.CARET_RIGHT);
		scrollRight.setGraphic(rightIcon);
		rightIcon.getStyleClass().addAll("navigation-icon", "large-icon", "icon-accent");
		scrollRight.visibleProperty().bind(Bindings.not(devices.rightOrDownDisableProperty()));
		scrollRight.setOnAction((e) -> devices.slideRightOrDown());

		getChildren().addAll(mask, scrollLeft, scrollRight);
	}

	public final Hyperlink getScrollLeft() {
		return scrollLeft;
	}

	public final Hyperlink getScrollRight() {
		return scrollRight;
	}

	public ObservableList<Node> getItems() {
		return devices.getContent().getChildren();
//		return hbox.getChildren();
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
		mask.setLayoutX(0);
		mask.setLayoutY(0);
		mask.resize(getWidth(), getHeight());
		scrollRight.setLayoutX(getWidth() - scrollRight.getWidth());
		scrollLeft.setLayoutY((getHeight() - scrollLeft.getHeight()) / 2f);
		scrollRight.setLayoutY((getHeight() - scrollRight.getHeight()) / 2f);
	}
}