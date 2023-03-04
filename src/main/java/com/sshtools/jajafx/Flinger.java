package com.sshtools.jajafx;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class Flinger extends StackPane {

	public enum Direction {
		VERTICAL, HORIZONTAL
	}

	private Pane container;
	private TranslateTransition slideTransition;
	private Rectangle slideClip;

	private Property<Direction> directionProperty = new SimpleObjectProperty<Direction>(Direction.HORIZONTAL);
	private BooleanProperty leftOrUpDisabledProperty = new SimpleBooleanProperty();
	private BooleanProperty rightOrDownDisabledProperty = new SimpleBooleanProperty();
	private DoubleProperty gapProperty = new SimpleDoubleProperty();
	private boolean dragged;
	private boolean wasDragged;

	public Flinger() {

		setup();

		directionProperty.addListener(new ChangeListener<Direction>() {

			@Override
			public void changed(ObservableValue<? extends Direction> observable, Direction oldValue,
					Direction newValue) {
				setup();
			}
		});

		// Handle events
		final AtomicReference<MouseEvent> deltaEvent = new AtomicReference<MouseEvent>();
		setOnScroll(scr -> {
			if (scr.getDeltaY() < 0) {
				slideRightOrDown();
			} else {
				slideLeftOrUp();
			}
		});
		setOnMousePressed(eh -> {
			wasDragged = false;
			dragged = false;
			deltaEvent.set(eh);
		});
		setOnMouseReleased(eh -> {
			if (!dragged) {
				/* Clicked. For the the root child item */
				EventTarget n = eh.getTarget();
				int depth = 0;
				do {
					if(n instanceof Node && container.equals(((Node)n).getParent())) {
						if(depth > 0) {
							if(!onAction.isNull().get()) {
								eh.consume();
								onAction.get().handle(new ActionEvent(this, n));
							}
						}
						break;
					}
					else if(n instanceof Node) {
						n = ((Node)n).getParent();
						depth++;
					}
					else
						break;
				}
				while(n != null);
			}
			else {
				eh.consume();
			}
			dragged = false;
		});

		// Handle drag events, will only allow drag until the first or last item
		// is revealed
		setOnMouseDragged(event -> {
			wasDragged = true;
			var mouseEvent = deltaEvent.get();
			if (mouseEvent != null && directionProperty.getValue().equals(Direction.HORIZONTAL)) {
				var delta = event.getX() - mouseEvent.getX();
				var newX = container.getTranslateX() + delta;
				var containerWidth = container.prefWidth(getHeight());
				if (newX + containerWidth < getWidth() - 1) {
					newX = container.getTranslateX();
				} else if (newX > 0) {
					newX = 0;
				}
				if (newX != container.getTranslateX()) {
					dragged = true;
					container.setTranslateX(newX);
				}
			}

			if (mouseEvent != null && directionProperty.getValue().equals(Direction.VERTICAL)) {
				var delta = event.getY() - mouseEvent.getY();
				var newX = container.getTranslateY() + delta;
				var containerHeight = container.prefHeight(getWidth());
				if (newX + containerHeight < getHeight()) {
					newX = container.getTranslateY();
				} else if (newX > 0) {
					newX = 0;
				}

				if (newX != container.getTranslateY()) {
					dragged = true;
					container.setTranslateY(newX);
				}
			}

			setAvailable();
			deltaEvent.set(event);
		});

	}

	public final boolean isWasDragged() {
		return wasDragged;
	}

	public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
		return onAction;
	}

	public final void setOnAction(EventHandler<ActionEvent> value) {
		onActionProperty().set(value);
	}

	public final EventHandler<ActionEvent> getOnAction() {
		return onActionProperty().get();
	}

	private ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>() {
		@Override
		protected void invalidated() {
			setEventHandler(ActionEvent.ACTION, get());
		}

		@Override
		public Object getBean() {
			return Flinger.this;
		}

		@Override
		public String getName() {
			return "onAction";
		}
	};

	public ReadOnlyBooleanProperty leftOrUpDisableProperty() {
		return leftOrUpDisabledProperty;
	}

	public ReadOnlyBooleanProperty rightOrDownDisableProperty() {
		return rightOrDownDisabledProperty;
	}

	public Property<Direction> directionProperty() {
		return directionProperty;
	}

	public DoubleProperty gapProperty() {
		return gapProperty;
	}

	public Pane getContent() {
		return container;
	}

	public void slideLeftOrUp() {
		/*
		 * We should only get this action if the button is enabled, which means at least
		 * one button is partially obscured on the left
		 */

		double scroll = 0;

		/*
		 * The position of the child within the container. When we find a node that
		 * crosses '0', that is how much this single scroll will adjust by, so
		 * completely revealing the hidden side
		 */

		if (directionProperty.getValue().equals(Direction.VERTICAL)) {

			for (var n : container.getChildren()) {
				double p = n.getLayoutY() + container.getTranslateY();

				double amt = p + n.getLayoutBounds().getHeight();
				if (amt >= 0) {
					scroll = Math.abs(n.getLayoutBounds().getHeight() - amt + gapProperty.get());
					if (container.getTranslateY() + scroll > 0) {
						scroll = 0;
						break;
					} else if (scroll > 0)
						break;
				}
			}
		} else {
			for (var n : container.getChildren()) {
				double p = n.getLayoutX() + container.getTranslateX();
				double amt = p + n.getLayoutBounds().getWidth();
				if (amt >= 0) {
					scroll = Math.abs(n.getLayoutBounds().getWidth() - amt);
					if (container.getTranslateX() + scroll > 0) {
						scroll = 0;
						break;
					} else if (scroll > 0)
						break;
				}
			}
		}

		setAvailable();
		if (scroll > 0) {
			if (directionProperty.getValue().equals(Direction.VERTICAL)) {
				slideTransition.setFromY(container.getTranslateY());
				slideTransition.setToY(container.getTranslateY() + scroll);
			} else {
				slideTransition.setFromX(container.getTranslateX());
				slideTransition.setToX(container.getTranslateX() + scroll);
			}
			slideTransition.setOnFinished(ae -> {
				setAvailable();
			});
			slideTransition.play();
		}
	}

	public void slideRightOrDown() {
		/*
		 * We should only get this action if the button is enabled, which means at least
		 * one button is partially obscured on the left
		 */

		var scroll = 0d;
		var c = container.getChildren();

		/*
		 * Search backwards through the nodes until on whose X or Y position is is in
		 * the visible portion
		 */
		if (directionProperty.getValue().equals(Direction.VERTICAL)) {

			for (int i = c.size() - 1; i >= 0; i--) {
				Node n = c.get(i);
				double p = n.getLayoutY() + container.getTranslateY();
				if (p <= getHeight()) {
					scroll = n.getLayoutBounds().getHeight() + gapProperty.get() - (getHeight() - p);
					break;
				}
			}
		} else {

			for (int i = c.size() - 1; i >= 0; i--) {
				Node n = c.get(i);
				double p = n.getLayoutX() + container.getTranslateX();
				if (p < getWidth()) {
					scroll = (getWidth() - (p + n.getLayoutBounds().getWidth() + 1 + gapProperty.get())) * -1;
					break;
				}
			}

		}
		setAvailable();
		if (scroll > 0) {
			leftOrUpDisabledProperty.set(false);
			if (directionProperty.getValue().equals(Direction.VERTICAL)) {
				slideTransition.setFromY(container.getTranslateY());
				slideTransition.setToY(container.getTranslateY() - scroll);
			} else {
				slideTransition.setFromX(container.getTranslateX());
				slideTransition.setToX(container.getTranslateX() - scroll);
			}
			slideTransition.setOnFinished(ae -> {
				setAvailable();
			});
			slideTransition.play();
		}
	}

	public void recentre() {
		setAvailable();

		var centre = getLaunchBarOffset();

		if (directionProperty.getValue().equals(Direction.VERTICAL)) {
			slideTransition.setFromY(container.getTranslateY());
			slideTransition.setToY(centre);
		} else {
			slideTransition.setFromX(container.getTranslateX());
			slideTransition.setToX(centre);
		}
		slideTransition.setOnFinished(eh -> setAvailable());
		slideTransition.stop();
		slideTransition.play();
	}

	@Override
	protected void layoutChildren() {
		for (var node : getChildren()) {
			layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.LEFT, VPos.TOP);
		}
	}

	private double getLaunchBarOffset() {
		return directionProperty.getValue().equals(Direction.VERTICAL)
				? (getHeight() - container.prefHeight(getWidth())) / 2d
				: (getWidth() - container.prefWidth(getHeight())) / 2d;
	}

	private void requiredSpaceChanged() {
		layoutContainer();
	}

	private void availableSpaceChanged() {
		layoutContainer();
	}

	private void layoutContainer() {
		if (directionProperty.getValue().equals(Direction.VERTICAL)) {
			container.setTranslateX((getWidth() - container.prefWidth(getHeight())) / 2d);

		} else {
			container.setTranslateY((getHeight() - container.prefHeight(getWidth())) / 2d);
		}
		layout();
		recentre();
	}

	private void setup() {
		List<Node> children = null;
		if (container != null) {
			children = new ArrayList<Node>(container.getChildrenUnmodifiable());
			container.getChildren().clear();
		}
		getChildren().clear();

		container = directionProperty.getValue().equals(Direction.VERTICAL) ? new VBox() : new HBox();
		container.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);

		if (directionProperty.getValue().equals(Direction.VERTICAL)) {
			((VBox) container).spacingProperty().bind(gapProperty);
		} else {
			((HBox) container).spacingProperty().bind(gapProperty);
		}

		widthProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				availableSpaceChanged();
			}
		});
		heightProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				availableSpaceChanged();
			}
		});

		/*
		 * Watch for new children being added, and so changing the amount of space
		 * required
		 */
		container.getChildren().addListener(new ListChangeListener<Node>() {

			@Override
			public void onChanged(ListChangeListener.Change<? extends Node> c) {
				requiredSpaceChanged();
			}
		});

		// Do not want the container managed, we lay it out ourselves
		container.setManaged(false);

		// Clip the content container to the width of this container
		slideClip = new Rectangle();
		slideClip.widthProperty().bind(widthProperty());
		slideClip.heightProperty().bind(heightProperty());
		setClip(slideClip);

		// Transition for sliding
		slideTransition = new TranslateTransition(Duration.seconds(0.125), container);
		slideTransition.setAutoReverse(false);
		slideTransition.setCycleCount(1);

		// Add back any children
		if (children != null) {
			container.getChildren().addAll(children);
		}

		// Add the new container to the scene
		getChildren().add(container);
	}

	private void setAvailable() {
		var scroll = 0d;
		var c = container.getChildren();
		if (directionProperty.getValue().equals(Direction.HORIZONTAL)) {
			for (var i = c.size() - 1; i >= 0; i--) {
				var  n = c.get(i);
				var p = n.getLayoutX() + container.getTranslateX();
				if (p < getWidth()) {
					scroll = (getWidth() - (p + n.getLayoutBounds().getWidth() + 1 + gapProperty.get())) * -1;
					break;
				}
			}

			leftOrUpDisabledProperty.set(container.getTranslateX() >= 0);
			rightOrDownDisabledProperty.set(scroll == 0);
		} else {
			for (var i = c.size() - 1; i >= 0; i--) {
				var n = c.get(i);
				var p = n.getLayoutY() + container.getTranslateY();
				if (p <= getHeight()) {
					scroll = n.getLayoutBounds().getHeight() + gapProperty.get() - (getHeight() - p);
					break;
				}
			}
			leftOrUpDisabledProperty.set(container.getTranslateY() >= 0);
			rightOrDownDisabledProperty.set(scroll == 0);
		}
	}
}
