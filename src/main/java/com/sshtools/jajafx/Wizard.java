package com.sshtools.jajafx;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class Wizard<C> extends BorderPane {
	
	class PageWrapper<P extends WizardPage<C>> {
		final Class<P> clazz;
		final PageTransition transition;
		P instance;
		
		PageWrapper(Class<P> clazz, PageTransition transition) {
			this.clazz = clazz;
			this.transition = transition;
		}
		
		void hidden() {
			if(instance != null)
				instance.hidden();
		}
		
		P show() {
			if(instance == null) {
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
	
	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(Wizard.class.getName());

	private final SlideyStack stack;
	private final Button nextButton;
	private final Hyperlink prevButton;
	private List<PageWrapper<?>> controllers = new ArrayList<>();
	private IntegerProperty index = new SimpleIntegerProperty(-1);
	private final BooleanProperty nextAvailable = new SimpleBooleanProperty();
	private final BooleanProperty prevVisible = new SimpleBooleanProperty(true);
	private final C context;
	private final HBox accessories;

	private BorderPane tools;

	public Wizard(C context) {
		this.context = context;
		
		nextButton = new Button(RESOURCES.getString("next"));
		nextButton.setId("next");
		nextButton.setOnAction(this::next);
		nextButton.setDefaultButton(true);
		nextButton.setGraphic(new FontIcon(FontAwesomeSolid.ARROW_RIGHT));
		nextButton.disableProperty().bind(Bindings.not(nextAvailable));
//		nextButton.managedProperty().bind(nextButton.visibleProperty());

		prevButton = new Hyperlink(RESOURCES.getString("previous"));
		prevButton.setId("prev");
		prevButton.setOnAction(this::prev);
		prevButton.setGraphic(new FontIcon(FontAwesomeSolid.ARROW_LEFT));
		prevButton.visibleProperty().bind(Bindings.and(
				prevVisible,
				Bindings.greaterThan(index, 0)));

		var navButtons = new HBox(8);
		navButtons.getStyleClass().add("spaced");
		navButtons.setAlignment(Pos.CENTER_RIGHT);
		navButtons.getChildren().addAll(/* dbgButton, */ prevButton, nextButton);

		stack = new SlideyStack();
		
		var anchor = new AnchorPane(stack);
		anchor.setPrefSize(600, 600);
		AnchorPane.setBottomAnchor(stack, 0d);
		AnchorPane.setTopAnchor(stack, 0d);
		AnchorPane.setLeftAnchor(stack, 0d);
		AnchorPane.setRightAnchor(stack, 0d);
		
		accessories = new HBox();
		accessories.getStyleClass().add("spaced");
		
		tools = new BorderPane();
		tools.setLeft(accessories);
		tools.setRight(navButtons);
		
		getStyleClass().add("padded");
		setCenter(anchor);
		setBottom(tools);

		FXUtil.addIfNotAdded(getStylesheets(), Wizard.class.getResource("Common.css").toExternalForm(), Wizard.class.getResource("Wizard.css").toExternalForm());
	}

	@SuppressWarnings("unchecked")
	public <P extends WizardPage<C>> P getPage(Class<P> clazz) {
		return (P) controllers.stream().filter(p -> p.instance != null && p.instance.getClass().equals(clazz))
				.collect(Collectors.reducing((a, b) -> null)).orElseThrow().instance;
	}

	public BooleanProperty toolsVisibleProperty() {
		return tools.visibleProperty();
	}

	public BooleanProperty nextVisibleProperty() {
		return nextButton.visibleProperty();
	}

	public BooleanProperty previousVisibleProperty() {
		return prevVisible;
	}
	
	public BooleanProperty nextAvailableProperty() {
		return nextAvailable;
	}

	public <P extends WizardPage<C>> void add(Class<P> clazz) {
		add(clazz, PageTransition.FROM_RIGHT);
	}

	public <P extends WizardPage<C>> void add(Class<P> clazz, PageTransition transition) {
		var w = new PageWrapper<>(clazz, transition);
		controllers.add(w);
		if(index.get() == -1) {
			next(null);
		}
	}

	@SuppressWarnings("unchecked")
	public <P extends WizardPage<C>> P popup(Class<P> clazz) {
		var w = new PageWrapper<>(clazz, PageTransition.FROM_BOTTOM);
		
		var idx = index.get();
		if(idx == -1) {
			controllers.add(w);
			next(null);
		}
		else {
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

	@SuppressWarnings("unchecked")
	public void remove(WizardPage<C> page) {
		if(getCurrentPage() == page) {
			prev();
		}
		PageWrapper<WizardPage<C>> wrapper = null;
		for(var c : controllers) {
			if(page.equals(c.instance)) {
				wrapper = (Wizard<C>.PageWrapper<WizardPage<C>>) c;
			}
		}
		controllers.remove(wrapper);
	}

	public void startAgain() {
		if(index.get() > 0) {
			if(index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			while(stack.size() > 1)
				stack.pop();
			index.set(0);
			controllers.get(index.get()).show();
		}
		
	}

	public HBox getAccessories() {
		return accessories;
	}
	
	public WizardPage<C> getCurrentPage() {
		var idx = index.get();
		return idx == -1 ? null : controllers.get(idx).instance;
	}

	
	private void next(ActionEvent evt) {
		var cp = getCurrentPage();
		if(index.get() < controllers.size() - 1 && (cp == null || ( cp != null && cp.validate()))) {
			if(index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			index.set(index.get() + 1);
			var w = controllers.get(index.get());
			stack.push(controllers.size() < 2 ? PageTransition.NONE : w.transition , w.show().getScene().getRoot());
		}
	}
	
	private void showIndex(int idx) {

		var cp = getCurrentPage();
		if(idx < controllers.size() && (cp == null || ( cp != null && cp.validate()))) {
			if(index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			index.set(idx);
			var w = controllers.get(idx);
			stack.push(controllers.size() < 2 ? PageTransition.NONE : w.transition , w.show().getScene().getRoot());
		}
	}
	
	private void prev(ActionEvent evt) {
		if(index.get() > 0) {
			if(index.get() > -1) {
				controllers.get(index.get()).hidden();
			}
			stack.pop();
			index.set(index.get() - 1);
			controllers.get(index.get()).show();
		}
	}

	private <P extends WizardPage<C>> P openScene(Class<P> controller) throws IOException {
		return openScene(controller, null);
	}

	@SuppressWarnings("unchecked")
	private <P extends WizardPage<C>> P openScene(Class<P> controller, String fxmlSuffix) throws IOException {
		var resourceName = controller.getSimpleName() + (fxmlSuffix == null ? "" : fxmlSuffix) + ".fxml";
		var resource = controller
				.getResource(resourceName);
		if(resource == null)
			throw new IOException(MessageFormat.format("No FXML {0} against {1}", resourceName, controller.getClass().getSimpleName()));
		var loader = new FXMLLoader();
		try {
			loader.setResources(ResourceBundle.getBundle(controller.getName()));
		} catch (MissingResourceException mre) {
			// Don't care
		}
		loader.setLocation(Wizard.class.getResource("Wizard.css"));
		Parent root = loader.load(resource.openStream());
		var controllerInst = (WizardPage<C>) loader.getController();
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
		if(controller != null) {
			var controllerCssUrl = controller.getResource(controller.getSimpleName() + ".css");
			if (controllerCssUrl != null)
				FXUtil.addIfNotAdded(ss, controllerCssUrl.toExternalForm());
		}
	}
}
