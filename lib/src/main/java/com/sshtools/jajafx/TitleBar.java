package com.sshtools.jajafx;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class TitleBar extends AnchorPane {
	
	@FXML
	protected HBox windowIcons;
	
	@FXML
	protected HBox accessories;
	
	@FXML
	private ImageView logo;
	@FXML
	private StackPane titleStack;
	@FXML
	private Node maximize;
	@FXML
	private Node minimize;
	@FXML
	private Node close;

	protected HBox currentAccessories;
	protected Map<Node, Node> accMap = new HashMap<>();
	
	public TitleBar() {
		this("TitleBar.fxml");
	}
	
	protected TitleBar(String resource) {
		var loader = new FXMLLoader(getClass().getResource(resource));
		loader.setController(this);
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		var ss = getStylesheets();
		ss.add(TitleBar.class.getResource("TitleBar.css").toExternalForm());
//		ss.add(TitleBar.class.getResource("Common.css").toExternalForm());
		
		currentAccessories = accessories;
		
		minimize.managedProperty().bind(minimize.visibleProperty());
		maximize.managedProperty().bind(maximize.visibleProperty());
		close.managedProperty().bind(close.visibleProperty());
		
		maximize.setVisible(false);
	}
	
	public void addAccessories(Node... accessories) {
		var ac = currentAccessories.getChildren();
		Arrays.asList(accessories).forEach(a -> { 
			var oc = a.getOnMouseClicked();
			a.setOnMouseClicked(evt -> {
				if(oc != null) {
					oc.handle(evt);
				}
				evt.consume();
			});
			var ax = Platforms.style().accessory(a);
			ac.add(ax); 
			ax.setOnMouseClicked(oc);
			ax.visibleProperty().bind(a.visibleProperty());
            ax.managedProperty().bind(a.managedProperty());
			accMap.put(a, ax);
		});
		for(int i = 0 ; i < ac.size(); i++) {
			var a = ac.get(i);
			if(i == 0) {
				a.getStyleClass().add("first-element");
			}
			else {
				a.getStyleClass().remove("first-element");
			}
		}
		
	}
	
	public void removeAccessories(Node... accessories) {
		for(var a : accessories) {
			this.accessories.getChildren().remove(accMap.remove(a));
		}
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
		getStage().setIconified(true);
	}

	@FXML
	private void maximize() {
		var wnd = getStage();
		if(wnd.isMaximized()) {
			wnd.setMaximized(false);
			maximize.getStyleClass().remove("restorable");
		}
		else {
			maximize.getStyleClass().add("restorable");
			wnd.setMaximized(true);
		}
	}
	
	@FXML
	private void close() {
		var wnd = getStage();
		wnd.fireEvent(new WindowEvent(wnd, WindowEvent.WINDOW_CLOSE_REQUEST)); 
	}

	protected Stage getStage() {
		return (Stage)getScene().getWindow();
	}
}
