package com.sshtools.jajafx.progress;

import java.util.ResourceBundle;

import com.sshtools.jajafx.AbstractTile;
import com.sshtools.jajafx.JajaFXApp;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class PromptPage<A extends JajaFXApp<?>> extends AbstractTile<A> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(PromptPage.class.getName());

	@FXML
	private Label text;
	@FXML
	private TextField prompt;
	@FXML
	private Button submit;

	@Override
	protected void onConfigure() {
	}

	public Label text() {
		return text;
	}

	public TextField prompt() {
		return prompt;
	}

	public Button submit() {
		return submit;
	}

}
