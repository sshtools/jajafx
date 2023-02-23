package com.sshtools.jajafx;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PromptPage extends AbstractWizardPage<JajaFXApp> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(PromptPage.class.getName());

	@FXML
	Label text;
	@FXML
	TextField prompt;
	@FXML
	Button submit;

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
