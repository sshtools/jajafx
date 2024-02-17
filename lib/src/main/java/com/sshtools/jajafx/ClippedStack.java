package com.sshtools.jajafx;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class ClippedStack extends StackPane {

	private StackPane inner;
	private Rectangle clip;

	public ClippedStack(Node... children) {
		inner = new StackPane();
		inner.getChildren().addAll(children);
		clip = new Rectangle();
		clip.setManaged(false);
		clip.widthProperty().bind(widthProperty());
		clip.heightProperty().bind(heightProperty());
		getChildren().addAll(inner);
		setClip(clip);

	}
}
