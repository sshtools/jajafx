package com.sshtools.jajafx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ScrollStack extends AnimPane {

	private final ObservableList<Node> nodes = FXCollections.observableArrayList();
	private final BooleanProperty showingFirst = new SimpleBooleanProperty();
	private final BooleanProperty showingLast = new SimpleBooleanProperty();
	private final IntegerProperty index = new SimpleIntegerProperty(-1);
	
	public ScrollStack() {
		index.addListener((c,o,n) -> {
			if(n.intValue() == -1) {
				clear();
			} else { 
				var node = nodes.get(n.intValue());
				if(getChildren().isEmpty()) {
					getChildren().add(node);
				}
				else {
					if(o.intValue() > n.intValue())
						doAnim(PageTransition.FROM_LEFT, node);
					else
						doAnim(PageTransition.FROM_RIGHT, node);
				}
			}
			
			/*
			 * NOTE: Without this new content is not fully properly initialize. E.g.
			 * CheckListView won't turn items into real children. Took days to find this!
			 */
			layout();
			applyCss();
		});
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public void remove(Node node) {
		var idx = nodes.indexOf(node);
		if (idx != -1) {
			nodes.remove(node);
			if(nodes.isEmpty())
				index.set(-1);
			else if(idx == index.get()) {
				if(idx > 0)
					index.set(idx - 1);
				else
					index.set(idx + 1);
			}
		}
	}

	public void set(int index, Node node) {
		nodes.set(index, node);
		if(index == indexProperty().get()) {
			getChildren().clear();
			getChildren().add(node);
		}
			
	}
	
	public ObservableList<Node> getNodes() {
		return nodes;
	}

	public IntegerProperty indexProperty() {
		return index;
	}

	public BooleanProperty showingFirstProperty() {
		return showingFirst;
	}

	public BooleanProperty showingLastProperty() {
		return showingLast;
	}

	public int size() {
		return nodes.size();
	}

	public void clear() {
		nodes.clear();
		getChildren().clear();
		index.set(-1);
		stateChange();
	}
	
	public void next() {
		if(index.get() < nodes.size() - 1)
			index.set(index.get() + 1);
	}
	
	public void previous() {
		if(index.get() > 0)
			index.set(index.get() - 1);
	}

	public void add(Node node) {
		var wasEmpty = getChildren().isEmpty();
		StackPane.setAlignment(node, Pos.TOP_LEFT);
		nodes.add(node);
		if (wasEmpty) {
			index.set(0);
		} else
			onChange(node);
	}

	public int indexOf(Node node) {
		return nodes.indexOf(node);
	}

	@Override
	protected void onChange(Node node) {
		stateChange();
	}

	private void stateChange() {
		Node content = getContent();
		if (content == null) {
			showingFirst.set(false);
			showingLast.set(false);
		} else {
			var idx = nodes.indexOf(content);
			showingFirst.set(nodes.size() > 0 && idx == 0);
			showingLast.set(nodes.size() > 0 && idx == nodes.size() - 1);
		}
	}

}
