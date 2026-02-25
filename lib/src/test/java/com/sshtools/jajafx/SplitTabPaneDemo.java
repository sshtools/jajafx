package com.sshtools.jajafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SplitTabPaneDemo extends Application {
	@Override
	public void start(Stage stage) {
		SplitTabPane single = new SplitTabPane();
		single.setPrefHeight(160);
		single.getTabs().add(new Tab("Single", content("Single tab", Color.LIGHTSTEELBLUE)));

		SplitTabPane flat = new SplitTabPane();
		flat.setPrefHeight(180);
		flat.getTabs().addAll(
				new Tab("One", content("Tab One", Color.BEIGE)),
				new Tab("Two", content("Tab Two", Color.PEACHPUFF)),
				new Tab("Three", content("Tab Three", Color.LIGHTGREEN)));

		SplitTabPane nested = new SplitTabPane();
		nested.setPrefHeight(220);
		Tab main = new Tab("Main", content("Main content", Color.LIGHTYELLOW));
		Tab side = new Tab("Side", content("Side content", Color.LIGHTPINK));
		Tab bottom = new Tab("Bottom", content("Bottom content", Color.LIGHTGRAY));
		nested.getTabs().add(main);
		nested.splitTab(main, side, Orientation.VERTICAL);
		nested.splitTab(side, bottom, Orientation.HORIZONTAL);

		SplitTabPane mixed = new SplitTabPane();
		mixed.setPrefHeight(260);
		Tab leftRoot = new Tab("Left", content("Left root", Color.LIGHTBLUE));
		Tab leftSplit = new Tab("Left split", content("Left split", Color.ALICEBLUE));
		Tab rightRoot = new Tab("Right", content("Right root", Color.LIGHTCORAL));
		Tab rightSplit = new Tab("Right split", content("Right split", Color.MISTYROSE));
		mixed.getTabs().addAll(leftRoot, rightRoot);
		mixed.splitTab(leftRoot, leftSplit, Orientation.VERTICAL);
		mixed.splitTab(rightRoot, rightSplit, Orientation.HORIZONTAL);


		VBox root = new VBox(10,
				sectionLabel("Single tab (headers hidden)"), single,
				sectionLabel("Multiple flat tabs"), flat,
				sectionLabel("Nested tabs via splits"), nested,
				sectionLabel("Flat tabs with mixed nested splits"), mixed);
		root.setPadding(new Insets(12));

		stage.setScene(new Scene(root, 900, 750));
		stage.setTitle("SplitTabPane Demo");
		stage.show();
	}

	private StackPane content(String text, Color color) {
		Label label = new Label(text);
		StackPane pane = new StackPane(label);
		pane.setPadding(new Insets(12));
		pane.setBackground(new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY)));
		return pane;
	}

	private Label sectionLabel(String text) {
		Label label = new Label(text);
		label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
		return label;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
