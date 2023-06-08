package com.sshtools.jajafx;

import java.util.ResourceBundle;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class YesNoPage<A extends JajaFXApp<?>> extends AbstractTile<A> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(YesNoPage.class.getName());

	@FXML
	private Label text;
	@FXML
	private Label title;
	@FXML
	private Button yes;
	@FXML
	private Button no;
	@FXML
	private HBox accessories;
	@FXML
	private ScrollPane scrollPane;

	@Override
	protected void onConfigure() {
		preferYes();
	}
	
	public void vgrowMessage(Priority priority) {
		VBox.setVgrow(scrollPane, priority);
	}
	
	public ObservableList<Node> accessories() {
		return accessories.getChildren();
	}

	public StringProperty titleText() {
		return title.textProperty();
	}
	
	public StringProperty textText() {
		return text.textProperty();
	}

	public void onYes(EventHandler<ActionEvent> handler) {
		yes.setOnAction(handler);
	}
	
	public void onNo(EventHandler<ActionEvent> handler) {
		no.setOnAction(handler);
	}

	public void preferYes() {
		prefer(true);
	}

	public void preferNo() {
		prefer(false);
	}
	public void prefer(boolean preferYes) {
		yes.setDefaultButton(preferYes);
		no.setDefaultButton(!preferYes);
		yes.setCancelButton(!preferYes);
		no.setCancelButton(preferYes);
	}
}
