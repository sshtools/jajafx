package com.sshtools.jajafx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

public class ScrollStack extends AnimPane {

	private final ObservableList<Node> nodes = FXCollections.observableArrayList();
	private final BooleanProperty showingFirst = new SimpleBooleanProperty();
	private final BooleanProperty showingLast = new SimpleBooleanProperty();

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public void remove(Node node) {
		var idx = nodes.indexOf(node);
		if (idx != -1) {
			nodes.remove(node);
			if (node.equals(getContent())) {
				if (nodes.isEmpty()) {
					clear();
				} else
					doAnim(PageTransition.FADE, nodes.get(Math.min(nodes.size() - 1, idx)));
			}
		}
	}
	
	public ObservableList<Node> getNodes() {
		return nodes;
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
		stateChange();
	}
	
	public void next() {
		var content = getContent();
		if(content != null) {
			var idx = nodes.indexOf(content);
			if(idx < nodes.size() - 1) {
				idx++;
				doAnim(PageTransition.FROM_RIGHT, nodes.get(idx));
			}
		}
	}
	
	public void previous() {
		var content = getContent();
		if(content != null) {
			var idx = nodes.indexOf(content);
			if(idx > 0) {
				idx--;
				doAnim(PageTransition.FROM_LEFT, nodes.get(idx));
			}
		}
	}

	public void add(Node node) {
		var wasEmpty = getChildren().isEmpty();
		StackPane.setAlignment(node, Pos.TOP_LEFT);
		nodes.add(node);
		if (wasEmpty) {
			doAnim(PageTransition.NONE, node);

			/*
			 * NOTE: Without this new content is not fully properly initialize. E.g.
			 * CheckListView won't turn items into real children. Took days to find this!
			 */
			layout();
			applyCss();
		} else
			onChange(node);
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
