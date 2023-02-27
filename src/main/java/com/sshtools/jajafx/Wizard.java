package com.sshtools.jajafx;

import java.util.ResourceBundle;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class Wizard<C> extends Tiles<C> {
	
	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(Wizard.class.getName());

	private final Button nextButton;
	private final Hyperlink prevButton;
	private final BooleanProperty nextAvailable = new SimpleBooleanProperty();
	private final BooleanProperty prevVisible = new SimpleBooleanProperty(true);
	private final HBox accessories;

	private BorderPane tools;

	public Wizard() {
		// For scenebuilder
		this(null);
	}

	public Wizard(C context) {
		super(context);
		
		nextButton = new Button(RESOURCES.getString("next"));
		nextButton.setId("next");
		nextButton.setOnAction(this::next);
		nextButton.setDefaultButton(true);
		nextButton.setGraphic(new FontIcon(FontAwesomeSolid.ARROW_RIGHT));
		nextButton.disableProperty().bind(Bindings.not(nextAvailable));
//		nextButton.managedProperty().bind(nextButton.visibleProperty());

		prevButton = new Hyperlink(RESOURCES.getString("previous"));
		prevButton.setId("prev");
		prevButton.setOnAction(this::prev);
		prevButton.setGraphic(new FontIcon(FontAwesomeSolid.ARROW_LEFT));
		prevButton.visibleProperty().bind(Bindings.and(
				prevVisible,
				Bindings.greaterThan(indexProperty(), 0)));

		var navButtons = new HBox(8);
		navButtons.getStyleClass().add("spaced");
		navButtons.setAlignment(Pos.CENTER_RIGHT);
		navButtons.getChildren().addAll(/* dbgButton, */ prevButton, nextButton);
		
		
		accessories = new HBox();
		accessories.getStyleClass().add("spaced");
		
		tools = new BorderPane();
		tools.setLeft(accessories);
		tools.setRight(navButtons);
		
		setBottom(tools);

		FXUtil.addIfNotAdded(getStylesheets(), Wizard.class.getResource("Common.css").toExternalForm(), Wizard.class.getResource("Wizard.css").toExternalForm());
	}
	
	public BooleanProperty toolsVisibleProperty() {
		return tools.visibleProperty();
	}

	public BooleanProperty nextVisibleProperty() {
		return nextButton.visibleProperty();
	}

	public BooleanProperty previousVisibleProperty() {
		return prevVisible;
	}
	
	public BooleanProperty nextAvailableProperty() {
		return nextAvailable;
	}

	public HBox getAccessories() {
		return accessories;
	}
}
