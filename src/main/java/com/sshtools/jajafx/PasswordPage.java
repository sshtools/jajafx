package com.sshtools.jajafx;

import java.util.ResourceBundle;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;

public class PasswordPage<A extends JajaFXApp<?>> extends AbstractWizardPage<A> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(PasswordPage.class.getName());

	@FXML
	Label text;
	@FXML
	Label title;
	@FXML
	PasswordField password;
	@FXML
	Button ok;
	@FXML
	CheckBox save;
	@FXML
	Button cancel;

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
	
	public void setConfirm(EventHandler<ActionEvent> handler) {
		ok.setOnAction(handler);
	}
	
	public void setCancel(EventHandler<ActionEvent> handler) {
		cancel.setOnAction(handler);
	}

	@Override
	public void shown() {
		getWizard().toolsVisibleProperty().set(false);
	}

	@Override
	public void hidden() {
		getWizard().toolsVisibleProperty().set(true);
	}

}
