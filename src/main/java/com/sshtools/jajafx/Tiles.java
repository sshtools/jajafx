package com.sshtools.jajafx;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

public class Tiles<C> extends BorderPane {

	class PageWrapper<P extends Tile<C>> {
		final Class<P> clazz;
		final PageTransition transition;
		Tile<C> instance;

		PageWrapper(Class<P> clazz, PageTransition transition) {
			this.clazz = clazz;
			this.transition = transition;
		}

		void hidden() {
			if (instance != null)
				instance.hidden();
		}

		Tile<C> show() {
			if (instance == null) {
				try {
					instance = openScene(clazz);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
			instance.shown();
			return instance;
		}
	}

	private SlideyStack stack = new SlideyStack();
	private List<PageWrapper<? extends Tile<C>>> controllers = new ArrayList<>();
	private IntegerProperty index = new SimpleIntegerProperty(-1);
	private final C context;

	public Tiles() {
		// For scenebuilder
		this(null);
	}

	public Tiles(C context) {
		this.context = context;
		
		ClippedStack clippedStack = new ClippedStack(stack);

		var anchor = new AnchorPane(clippedStack);
		anchor.setPrefSize(600, 600);
		AnchorPane.setBottomAnchor(clippedStack, 0d);
		AnchorPane.setTopAnchor(clippedStack, 0d);
		AnchorPane.setLeftAnchor(clippedStack, 0d);
		AnchorPane.setRightAnchor(clippedStack, 0d);
		
//		setClip(anchor);
		
		setCenter(anchor);
		FXUtil.addIfNotAdded(getStylesheets(), Tiles.class.getResource("Common.css").toExternalForm(),
				Tiles.class.getResource("Tiles.css").toExternalForm());
	}

	public final int getIndex() {
		return index.get();
	}

	public final IntegerProperty indexProperty() {
		return index;
	}

	@SuppressWarnings("unchecked")
	public <P extends Tile<C>> P getPage(Class<P> clazz) {
		return (P) controllers.stream().filter(p -> p.instance != null && p.instance.getClass().equals(clazz))
				.collect(Collectors.reducing((a, b) -> null)).orElseThrow().instance;
	}

	public <P extends Tile<C>> void add(Class<P> clazz) {
		add(clazz, PageTransition.FROM_RIGHT);
	}

	public <P extends Tile<C>> void add(Class<P> clazz, PageTransition transition) {
		var w = new PageWrapper<>(clazz, transition);
		controllers.add(w);
		if (index.get() == -1) {
			next(null);
		}
	}

	public <P extends Tile<C>> P popup(Class<P> clazz) {
		return popup(clazz, PageTransition.FROM_BOTTOM);
	}

	@SuppressWarnings("unchecked")
	public <P extends Tile<C>> P popup(Class<P> clazz, PageTransition transition) {
		var w = new PageWrapper<P>(clazz, transition);

		var idx = index.get();
		if (idx == -1) {
			controllers.add(w);
			next(null);
		} else {
			controllers.add(idx + 1, w);
			showIndex(idx + 1);
		}
		return (P) getCurrentPage();
	}

	public void next() {
		next(null);
	}

	public void prev() {
		prev(null);
	}

	public void remove(Tile<C> page) {
		if (getCurrentPage() == page) {
			prev();
		}
		PageWrapper<? extends Tile<C>> wrapper = null;
		for (var c : controllers) {
			if (page.equals(c.instance)) {
				wrapper = (Tiles<C>.PageWrapper<? extends Tile<C>>) c;
			}
		}
		controllers.remove(wrapper);
	}

	public void startAgain() {
		if (index.get() > 0) {
			if (index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			while (stack.size() > 1)
				stack.pop();
			index.set(0);
			controllers.get(index.get()).show();
		}

	}

	public Tile<C> getCurrentPage() {
		var idx = index.get();
		return idx == -1 ? null : controllers.get(idx).instance;
	}

	void next(ActionEvent evt) {
		var cp = getCurrentPage();
		if (index.get() < controllers.size() - 1 && (cp == null || validate(cp))) {
			if (index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			index.set(index.get() + 1);
			var w = controllers.get(index.get());
			stack.push(controllers.size() < 2 ? PageTransition.NONE : w.transition, w.show().getScene().getRoot());
		}
	}

	private void showIndex(int idx) {

		var cp = getCurrentPage();
		if (idx < controllers.size() && (cp == null || validate(cp))) {
			if (index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			index.set(idx);
			var w = controllers.get(idx);
			stack.push(controllers.size() < 2 ? PageTransition.NONE : w.transition, w.show().getScene().getRoot());
		}
	}

	private boolean validate(Tile<C> cp) {
		if (cp == null)
			return false;
		else {
			return cp.validate();
		}
	}

	void prev(ActionEvent evt) {
		if (index.get() > 0) {
			if (index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			stack.pop();
			index.set(index.get() - 1);
			controllers.get(index.get()).show();
		}
	}

	private <P extends Tile<C>> P openScene(Class<P> controller) throws IOException {
		return openScene(controller, null);
	}

	@SuppressWarnings("unchecked")
	private <P extends Tile<C>> P openScene(Class<P> controller, String fxmlSuffix) throws IOException {
		var resourceName = controller.getSimpleName() + (fxmlSuffix == null ? "" : fxmlSuffix) + ".fxml";
		var resource = controller.getResource(resourceName);
		if (resource == null)
			throw new IOException(MessageFormat.format("No FXML {0} against {1}", resourceName,
					controller.getClass().getSimpleName()));
		var loader = new FXMLLoader();
		try {
			loader.setResources(ResourceBundle.getBundle(controller.getName()));
		} catch (MissingResourceException mre) {
			// Don't care
		}
		loader.setLocation(Tiles.class.getResource("Wizard.css"));
		Parent root = loader.load(resource.openStream());
		var controllerInst = (Tile<C>) loader.getController();
		if (controllerInst == null) {
			throw new IOException("Controller not found. Check controller in FXML");
		}
		addStylesheets(controller, root);

		var scene = new Scene(root);
		controllerInst.configure(scene, this, context);
		scene.getRoot().getStyleClass().add("rootPane");
		return (P) controllerInst;
	}

	private void addStylesheets(Class<?> controller, Parent root) {
		var ss = root.getStylesheets();
		FXUtil.addIfNotAdded(ss, JajaApp.class.getResource("Common.css").toExternalForm());
		if (controller != null) {
			var controllerCssUrl = controller.getResource(controller.getSimpleName() + ".css");
			if (controllerCssUrl != null)
				FXUtil.addIfNotAdded(ss, controllerCssUrl.toExternalForm());
		}
	}
}
