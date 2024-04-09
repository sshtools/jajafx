package com.sshtools.jajafx.progress;

import java.util.ResourceBundle;

import com.sshtools.jajafx.AbstractTile;
import com.sshtools.jajafx.JajaFXApp;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class PasswordPage<A extends JajaFXApp<?, ?>> extends AbstractTile<A> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(PasswordPage.class.getName());

	@FXML
	private Label text;
	@FXML
	private Label title;
	@FXML
	private PasswordField password;
	@FXML
	private Button ok;
	@FXML
	private CheckBox save;
	@FXML
	private Button cancel;

	@Override
	protected void onConfigure() {
		save.managedProperty().bind(save.visibleProperty());
	}
	
	public boolean isSave() {
		return save.isSelected();
	}
	
	public void setSaveAvailable(boolean saveAvailable) {
		save.setVisible(saveAvailable);
	}
	
	public StringProperty password() {
		return password.textProperty();
	}
	
	public StringProperty titleText() {
		return title.textProperty();
	}
	
	public StringProperty textText() {
		return text.textProperty();
	}
	
	public void onConfirm(EventHandler<ActionEvent> handler) {
		ok.setOnAction(handler);
	}
	
	public void onCancel(EventHandler<ActionEvent> handler) {
		cancel.setOnAction(handler);
	}

}
