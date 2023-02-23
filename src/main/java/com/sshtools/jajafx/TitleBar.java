package com.sshtools.jajafx;

import java.io.IOException;
import java.io.UncheckedIOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TitleBar extends AnchorPane {
	
	@FXML
	private HBox leftWindowIcons;
	@FXML
	private HBox rightWindowIcons;
	
	public TitleBar() {
		var loader = new FXMLLoader(getClass().getResource("TitleBar.fxml"));
		loader.setController(this);
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		var ss = getStylesheets();
		ss.add(TitleBar.class.getResource("TitleBar.css").toExternalForm());
		ss.add(TitleBar.class.getResource("Common.css").toExternalForm());
		
		if(System.getProperty("os.name") != "mac" && System.getProperty("os.name") != "darwin") {
			leftWindowIcons.setVisible(false);
			rightWindowIcons.setVisible(true);
		}
		else {
			leftWindowIcons.setVisible(true);
			rightWindowIcons.setVisible(false);
		}
	}
	
	@FXML
	private void minimize() {
		((Stage)getScene().getWindow()).setIconified(true);
	}
	
	@FXML
	private void close() {
		((Stage)getScene().getWindow()).close();
	}
}
