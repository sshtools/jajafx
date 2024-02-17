package com.sshtools.jajafx;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class WindowsTitleBar extends TitleBar {

	@FXML
	private ImageView close;	
	@FXML
	private ImageView minimize;	
	@FXML
	private ImageView maximize;
	@FXML
	private HBox windowIcons;

	public WindowsTitleBar() {
		super("WindowsTitleBar.fxml");
		getStylesheets().add(MacOSTitleBar.class.getResource("WindowsTitleBar.css").toExternalForm());
	}

	@FXML
	private void closeMousePressed() {
		close.getStyleClass().add("pressed");
	}

	@FXML
	private void closeMouseReleased() {
		close.getStyleClass().remove("pressed");
	}

	@FXML
	private void minimizeMousePressed() {
		minimize.getStyleClass().add("pressed");
	}

	@FXML
	private void minimizeMouseReleased() {
		minimize.getStyleClass().remove("pressed");
	}

	@FXML
	private void maximizeMousePressed() {
		maximize.getStyleClass().add("pressed");
	}

	@FXML
	private void maximizeMouseReleased() {
		maximize.getStyleClass().remove("pressed");
	}

}
