package com.sshtools.jajafx;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TitleBar extends AnchorPane {
	
	@FXML
	private HBox windowIcons;
	@FXML
	private HBox accessories;
	@FXML
	private ImageView lodgo;
	@FXML
	private StackPane titleStack;
	@FXML
	private FontIcon maximize;
	@FXML
	private FontIcon minimize;
	@FXML
	private FontIcon close;

	private HBox currentAccessories;
	
	
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
		
		if(System.getProperty("os.name","").toLowerCase().contains("mac os")) {
			var al = new ArrayList<>(windowIcons.getChildren());
			windowIcons.getChildren().setAll(accessories.getChildren());
			accessories.getChildren().setAll(al);
			currentAccessories = windowIcons;
		}
		else {
			currentAccessories = accessories;
		}
		
		minimize.managedProperty().bind(minimize.visibleProperty());
		maximize.managedProperty().bind(maximize.visibleProperty());
		close.managedProperty().bind(close.visibleProperty());
		
		maximize.setVisible(false);
	}
	
	public final BooleanProperty minimizeVisibleProperty() {
		return minimize.visibleProperty();
	}
	
	public final BooleanProperty maximizeVisibleProperty() {
		return maximize.visibleProperty();
	}
	
	public final BooleanProperty closeVisibleProperty() {
		return close.visibleProperty();
	}
	
	public final StackPane getTitleStack() {
		return titleStack;
	}

	public final HBox getAccessories() {
		return currentAccessories;
	}

	@FXML
	private void minimize() {
		((Stage)getScene().getWindow()).setIconified(true);
	}

	@FXML
	private void maximize() {
		var wnd = (Stage)getScene().getWindow();
		if(wnd.isMaximized())
			wnd.setMaximized(false);
		else
			wnd.setMaximized(true);
	}
	
	@FXML
	private void close() {
		var wnd = (Stage)getScene().getWindow();
		wnd.fireEvent(new WindowEvent(wnd, WindowEvent.WINDOW_CLOSE_REQUEST)); 
	}
}
