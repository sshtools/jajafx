package com.sshtools.jajafx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

/**
 * A TabPane-like control that hides headers for a single tab and supports
 * splitting tabs into nested SplitPanes.
 */
public class SplitTabPane extends Region {
	private static final String SPLIT_MARKER_KEY = "splitTabPane.split";

	private final ObservableList<Tab> tabs = FXCollections.observableArrayList();
	private final ObservableList<Tab> rootTabs = FXCollections.observableArrayList();
	private final Set<Tab> nestedTabs = new HashSet<>();
	private final Map<Tab, Tab> parentTabs = new HashMap<>();
	private final Map<Tab, Node> leafNodes = new HashMap<>();
	private final Map<Tab, Node> contentRoots = new HashMap<>();
	private final Map<Tab, ChangeListener<Node>> contentListeners = new HashMap<>();
	private final Map<Tab, EventHandler<MouseEvent>> mouseHandlers = new HashMap<>();
	private final Map<Tab, ChangeListener<Boolean>> focusHandlers = new HashMap<>();

	private final TabPane tabPane = new TabPane();
	private final StackPane singleContent = new StackPane();
	private final StackPane container = new StackPane();

	private final SplitTabSelectionModel selectionModel = new SplitTabSelectionModel();
	private final ReadOnlyObjectWrapper<Tab> selectedTab = new ReadOnlyObjectWrapper<>(this, "selectedTab");

	public SplitTabPane() {
		getStyleClass().add("split-tab-pane");

		container.getChildren().addAll(tabPane, singleContent);
		getChildren().add(container);

		singleContent.setVisible(false);
		singleContent.setManaged(false);

		tabs.addListener(this::onTabsChanged);

		tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
			if (newTab != null && rootTabs.contains(newTab)) {
				selectionModel.select(newTab);
			}
		});

		selectedTab.addListener((obs, oldTab, newTab) -> syncRootSelection(newTab));
	}
	
	public void setTabClosingPolicy(TabPane.TabClosingPolicy policy) {
		tabPane.setTabClosingPolicy(policy);
	}
	
	public TabPane.TabClosingPolicy getTabClosingPolicy() {
		return tabPane.getTabClosingPolicy();
	}
	
	public ObjectProperty<TabPane.TabClosingPolicy> tabClosingPolicyProperty() {
		return tabPane.tabClosingPolicyProperty();
	}

	public void setSide(Side side) {
		tabPane.setSide(side);
	}

	public Side getSide() {
		return tabPane.getSide();
	}

	public ObjectProperty<Side> sideProperty() {
		return tabPane.sideProperty();
	}

	public void setTabDragPolicy(TabDragPolicy reorder) {
		tabPane.setTabDragPolicy(reorder);
	}

	public TabDragPolicy getTabDragPolicy() {
		return tabPane.getTabDragPolicy();
	}

	public ObjectProperty<TabDragPolicy> tabDragPolicyProperty() {
		return tabPane.tabDragPolicyProperty();
	}

	public ObservableList<Tab> getTabs() {
		return tabs;
	}

	public SingleSelectionModel<Tab> getSelectionModel() {
		return selectionModel;
	}

	public ReadOnlyObjectProperty<Tab> selectedTabProperty() {
		return selectedTab.getReadOnlyProperty();
	}

	public Tab getSelectedTab() {
		return selectedTab.get();
	}

	public void splitTab(Tab target, Tab newTab) {
		splitTab(target, newTab, Orientation.HORIZONTAL);
	}

	public void splitTab(Tab target, Tab newTab, Orientation orientation) {
		if (target == null || newTab == null) {
			return;
		}
		if (!tabs.contains(target)) {
			tabs.add(target);
		}

		nestedTabs.add(newTab);
		parentTabs.put(newTab, target);
		if (!tabs.contains(newTab)) {
			tabs.add(newTab);
		} else {
			rootTabs.remove(newTab);
			tabPane.getTabs().remove(newTab);
		}

		Node targetLeaf = ensureLeafNode(target);
		Node newLeaf = ensureLeafNode(newTab);

		SplitPane split = new SplitPane();
		split.setOrientation(orientation);
		split.getItems().addAll(targetLeaf, newLeaf);
		markSplit(split);

		replaceLeafWithSplit(target, targetLeaf, split);
		selectionModel.select(newTab);
	}

	@Override
	protected void layoutChildren() {
		container.resizeRelocate(0, 0, getWidth(), getHeight());
	}

	private void onTabsChanged(ListChangeListener.Change<? extends Tab> change) {
		while (change.next()) {
			if (change.wasAdded()) {
				for (Tab tab : change.getAddedSubList()) {
					addTabInternal(tab);
				}
			}
			if (change.wasRemoved()) {
				for (Tab tab : change.getRemoved()) {
					removeTabInternal(tab);
				}
			}
		}
		rebuildRootTabs();
		updateRootContainer();
	}

	private void rebuildRootTabs() {
		List<Tab> newRoots = new ArrayList<>();
		for (Tab tab : tabs) {
			if (!nestedTabs.contains(tab)) {
				newRoots.add(tab);
			}
		}
		if (!rootTabs.equals(newRoots)) {
			rootTabs.setAll(newRoots);
		}
	}

	private void addTabInternal(Tab tab) {
		if (tab == null || contentListeners.containsKey(tab)) {
			return;
		}

		ensureLeafNode(tab);
		if (!nestedTabs.contains(tab)) {
			rootTabs.add(tab);
			Node rootNode = contentRoots.get(tab);
			if (rootNode != null) {
				tab.setContent(rootNode);
			}
		}

		ChangeListener<Node> listener = (obs, oldVal, newVal) -> {
			if (newVal == null) {
				Node placeholder = createPlaceholder(tab);
				tab.setContent(placeholder);
				return;
			}
			if (isMarkedSplit(newVal)) {
				contentRoots.put(tab, newVal);
				return;
			}
			updateLeafContent(tab, oldVal, newVal);
		};
		contentListeners.put(tab, listener);
		tab.contentProperty().addListener(listener);

		if (selectionModel.getSelectedItem() == null) {
			selectionModel.select(tab);
		}
	}

	private void removeTabInternal(Tab tab) {
		boolean wasRoot = !nestedTabs.contains(tab);
		rootTabs.remove(tab);
		nestedTabs.remove(tab);
		parentTabs.remove(tab);
		tabPane.getTabs().remove(tab);

		ChangeListener<Node> listener = contentListeners.remove(tab);
		if (listener != null) {
			tab.contentProperty().removeListener(listener);
		}

		Node leaf = leafNodes.remove(tab);
		Node root = contentRoots.remove(tab);
		removeSelectionHandlers(tab, leaf);

		if (root != null) {
			collapseIfNeeded(root, leaf, wasRoot);
		}

		if (selectionModel.getSelectedItem() == tab) {
			selectionModel.clearSelection();
		}
	}

	private Node ensureLeafNode(Tab tab) {
		Node leaf = leafNodes.get(tab);
		if (leaf != null) {
			return leaf;
		}
		leaf = tab.getContent();
		if (leaf == null) {
			leaf = createPlaceholder(tab);
			tab.setContent(leaf);
		}
		leafNodes.put(tab, leaf);
		contentRoots.put(tab, leaf);
		installSelectionHandlers(tab, leaf);
		return leaf;
	}

	private void updateLeafContent(Tab tab, Node oldVal, Node newVal) {
		Node oldLeaf = leafNodes.get(tab);
		if (oldLeaf != null && oldLeaf != newVal) {
			removeSelectionHandlers(tab, oldLeaf);
		}
		leafNodes.put(tab, newVal);
		installSelectionHandlers(tab, newVal);

		Node root = contentRoots.get(tab);
		if (root == oldVal || root == oldLeaf) {
			contentRoots.put(tab, newVal);
			replaceNodeInLayout(tab, oldVal, newVal);
		} else {
			replaceNodeInParent(oldVal, newVal);
		}
	}

	private void replaceLeafWithSplit(Tab tab, Node leaf, SplitPane split) {
		Node root = contentRoots.get(tab);
		SplitPane parentSplit = findSplitPaneParent(leaf);

		if (parentSplit != null && parentSplit.getItems().contains(leaf)) {
			int index = parentSplit.getItems().indexOf(leaf);
			if (index >= 0) {
				parentSplit.getItems().set(index, split);
			}
		} else {
			contentRoots.put(tab, split);
			replaceNodeInLayout(tab, root, split);
		}

		tab.setContent(contentRoots.get(tab));
		updateRootContainer();
	}

	private void replaceNodeInLayout(Tab tab, Node oldNode, Node newNode) {
		if (rootTabs.contains(tab)) {
			tab.setContent(newNode);
			if (rootTabs.size() == 1) {
				singleContent.getChildren().setAll(newNode);
			}
			return;
		}

		replaceNodeInParent(oldNode, newNode);
	}

	private void replaceNodeInParent(Node oldNode, Node newNode) {
		if (oldNode == null) {
			return;
		}
		Parent parent = oldNode.getParent();
		if (parent instanceof SplitPane) {
			SplitPane split = (SplitPane) parent;
			int index = split.getItems().indexOf(oldNode);
			if (index >= 0) {
				split.getItems().set(index, newNode);
			}
		} else if (parent == singleContent) {
			singleContent.getChildren().setAll(newNode);
		}
	}

	private void updateRootContainer() {
		if (rootTabs.size() <= 1) {
			tabPane.setVisible(false);
			tabPane.setManaged(false);
			tabPane.getTabs().clear();
			tabPane.getSelectionModel().clearSelection();
			singleContent.setVisible(true);
			singleContent.setManaged(true);

			if (rootTabs.isEmpty()) {
				singleContent.getChildren().clear();
			} else {
				Tab root = rootTabs.get(0);
				Node node = contentRoots.get(root);
				if (node == null) {
					node = root.getContent();
				}
				if (node == null) {
					node = createPlaceholder(root);
					root.setContent(node);
				}
				contentRoots.put(root, node);
				singleContent.getChildren().setAll(node);
			}
		} else {
			singleContent.setVisible(false);
			singleContent.setManaged(false);
			tabPane.setVisible(true);
			tabPane.setManaged(true);
			tabPane.getTabs().setAll(rootTabs);
		}
	}

	private void syncRootSelection(Tab selected) {
		if (selected == null) {
			return;
		}
		Tab root = findRootTab(selected);
		if (root != null && rootTabs.contains(root)) {
			tabPane.getSelectionModel().select(root);
		}
	}

	private Tab findRootTab(Tab tab) {
		Tab current = tab;
		while (parentTabs.containsKey(current)) {
			current = parentTabs.get(current);
		}
		return current;
	}

	private void installSelectionHandlers(Tab tab, Node node) {
		if (node == null) {
			return;
		}

		EventHandler<MouseEvent> mouseHandler = evt -> {
			selectionModel.select(tab);
			node.requestFocus();
		};
		ChangeListener<Boolean> focusHandler = (obs, oldVal, newVal) -> {
			if (Boolean.TRUE.equals(newVal)) {
				selectionModel.select(tab);
			}
		};

		node.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
		node.focusedProperty().addListener(focusHandler);

		mouseHandlers.put(tab, mouseHandler);
		focusHandlers.put(tab, focusHandler);
	}

	private void removeSelectionHandlers(Tab tab, Node node) {
		if (node == null) {
			return;
		}
		EventHandler<MouseEvent> mouseHandler = mouseHandlers.remove(tab);
		ChangeListener<Boolean> focusHandler = focusHandlers.remove(tab);
		if (mouseHandler != null) {
			node.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
		}
		if (focusHandler != null) {
			node.focusedProperty().removeListener(focusHandler);
		}
	}

	private void collapseIfNeeded(Node root, Node leaf, boolean wasRoot) {
		if (leaf == null) {
			return;
		}
		SplitPane split = findSplitPaneParent(leaf);
		if (split == null || split.getItems().size() != 2) {
			return;
		}
		Node other = null;
		for (Node item : split.getItems()) {
			if (item != leaf) {
				other = item;
				break;
			}
		}
		if (other == null) {
			return;
		}
		if (wasRoot) {
			Tab remainingTab = findTabByNode(other);
			if (remainingTab != null) {
				nestedTabs.remove(remainingTab);
				parentTabs.remove(remainingTab);
				contentRoots.put(remainingTab, other);
			}
		}
		replaceSplitWithNode(split, other);
	}

	private Tab findTabByNode(Node node) {
		for (Map.Entry<Tab, Node> entry : contentRoots.entrySet()) {
			if (entry.getValue() == node) {
				return entry.getKey();
			}
		}
		for (Map.Entry<Tab, Node> entry : leafNodes.entrySet()) {
			if (entry.getValue() == node) {
				return entry.getKey();
			}
		}
		return null;
	}

	private Tab findTabByContentRoot(Node node) {
		for (Map.Entry<Tab, Node> entry : contentRoots.entrySet()) {
			if (entry.getValue() == node) {
				return entry.getKey();
			}
		}
		return null;
	}

	private SplitPane findSplitPaneParent(Node node) {
		Parent parent = node.getParent();
		while (parent != null) {
			if (parent instanceof SplitPane) {
				return (SplitPane) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	private Node createPlaceholder(Tab tab) {
		StackPane placeholder = new StackPane();
		placeholder.setMinSize(0, 0);
		placeholder.setPrefSize(0, 0);
		placeholder.getStyleClass().add("split-tab-pane-placeholder");
		return placeholder;
	}

	private boolean isMarkedSplit(Node node) {
		return node instanceof SplitPane && Boolean.TRUE.equals(node.getProperties().get(SPLIT_MARKER_KEY));
	}

	private void markSplit(SplitPane split) {
		split.getProperties().put(SPLIT_MARKER_KEY, Boolean.TRUE);
	}

	private void replaceSplitWithNode(SplitPane split, Node replacement) {
		Tab owner = findTabByContentRoot(split);
		if (owner != null) {
			contentRoots.put(owner, replacement);
			owner.setContent(replacement);
			if (rootTabs.size() == 1 && rootTabs.contains(owner)) {
				singleContent.getChildren().setAll(replacement);
			}
			return;
		}

		SplitPane parentSplit = findSplitPaneParent(split);
		if (parentSplit != null) {
			int index = parentSplit.getItems().indexOf(split);
			if (index >= 0) {
				parentSplit.getItems().set(index, replacement);
			}
			return;
		}

		if (split.getParent() == singleContent) {
			singleContent.getChildren().setAll(replacement);
		}
	}

	private final class SplitTabSelectionModel extends SingleSelectionModel<Tab> {
		@Override
		protected Tab getModelItem(int index) {
			return index >= 0 && index < tabs.size() ? tabs.get(index) : null;
		}

		@Override
		protected int getItemCount() {
			return tabs.size();
		}

		@Override
		public void select(Tab obj) {
			if (obj == null || !tabs.contains(obj)) {
				clearSelection();
				return;
			}
			setSelectedItem(obj);
			setSelectedIndex(tabs.indexOf(obj));
			selectedTab.set(obj);
		}
		
		@Override
		public void select(int index) {
			if (index < 0 || index >= tabs.size()) {
				clearSelection();
				return;
			}
			select(tabs.get(index));
		}
		
		@Override
		public void clearSelection() {
			super.clearSelection();
			selectedTab.set(null);
		}
	}
}
