package com.sshtools.jajafx;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class YesNoPage extends AbstractWizardPage<JajaFXApp> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(YesNoPage.class.getName());

	@FXML
	Label text;
	@FXML
	Button yes;
	@FXML
	Button no;

	@Override
	protected void onConfigure() {
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
