package com.sshtools.jajafx;

import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class YesNoPage<A extends JajaFXApp<?>> extends AbstractTile<A> {

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
}
