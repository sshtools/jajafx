package com.sshtools.jajafx;

import org.kordamp.ikonli.fontawesome5.FontAwesomeRegular;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.animation.FadeTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class Screenshots extends BorderPane {

	private StackPane stack = new StackPane();
	private HBox dots = new HBox();
	private IntegerProperty visible = new SimpleIntegerProperty();

	public Screenshots() {
		setCenter(stack);
		setBottom(dots);

		dots.setAlignment(Pos.TOP_CENTER);
		dots.getStyleClass().add("spaced");
		
		dots.managedProperty().bind(dots.visibleProperty());
		dots.visibleProperty().bind(Bindings.createBooleanBinding(() -> dots.getChildren().size() > 1, dots.getChildren()));

		getStack().addListener((ListChangeListener.Change<? extends Node> c) -> {
			var resetSelected = false;
			while (c.next()) {
				for (var addedNode : c.getAddedSubList()) {
					var idx = getStack().indexOf(addedNode);
					var sel = idx == visible.get();

					var lnk = new Hyperlink();
					lnk.setGraphic(createGraphic(sel));
					lnk.setOnAction(e -> {
						visible.set(dots.getChildren().indexOf(lnk));
					});
					
					dots.getChildren().add(lnk);
					if (!sel) {
						addedNode.setOpacity(0);
						addedNode.setVisible(false);
					}
				}

				for (var removedNode : c.getRemoved()) {
					var idx = getStack().indexOf(removedNode);
					if (idx == visible.intValue()) {
						resetSelected = true;
					}
					dots.getChildren().remove(0);
					removedNode.setOpacity(1);
					removedNode.setVisible(true);
				}
			}
			if (resetSelected || visible.get() > getStack().size()) {
				visible.set(0);
			}
		});

		visible.addListener((c, o, n) -> {
			
			var onode = getStack().get(o.intValue());
			var nnode = getStack().get(n.intValue());

			var odot = dots.getChildren().get(o.intValue());
			((Hyperlink) odot).setGraphic(createGraphic(false));
			var ndot = dots.getChildren().get(n.intValue());
			((Hyperlink) ndot).setGraphic(createGraphic(true));

			var fade = new FadeTransition(Duration.millis(750), onode);
			fade.setFromValue(1);
			fade.setToValue(0);
			fade.setOnFinished((e) -> onode.setVisible(false));
			fade.play();

			nnode.setVisible(true);
			fade = new FadeTransition(Duration.millis(750), nnode);
			fade.setFromValue(0);
			fade.setToValue(1);
			fade.play();

		});
	}

	public ObservableList<Node> getStack() {
		return stack.getChildren();
	}

	private Node createGraphic(boolean selected) {
		var g = selected ? FontIcon.of(FontAwesomeSolid.CIRCLE) : FontIcon.of(FontAwesomeRegular.CIRCLE);
		g.getStyleClass().add("icon-accent");
		return g;
	}
}
