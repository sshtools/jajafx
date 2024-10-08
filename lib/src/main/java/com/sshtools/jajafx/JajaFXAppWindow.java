package com.sshtools.jajafx;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import com.goxr3plus.fxborderlessscene.borderless.BorderlessScene;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

public class JajaFXAppWindow<A extends JajaFXApp<?, ?>> implements ListChangeListener<Screen> {

	private TitleBar titleBar;
	private final Stage stage;
	private Label titleLabel;
	private Node titleNode;
	private ImageView titleImage;
	private AppStyle appStyle;

	protected A app;
	protected final Scene scene;
    private boolean keepInBounds;
	private Set<String> defaultStylesheets;
	private final BorderPane ui;

    public JajaFXAppWindow(Stage stage, A app) {
        this(stage, app, 0, 0);
    }

	public JajaFXAppWindow(Stage stage, A app, double width, double height) {
		this.stage = stage;
		this.app = app;
		
		stage.initStyle(app.isDecorated() ? StageStyle.DECORATED :  borderlessStageStyle());

		ui = new BorderPane();
		if (!app.isDecorated()) {
			ui.setTop(titleBar = createTitleBar());
			checkFrameTitle();
		}
		
		// TODO check .. this doesnt seem necessary, its done slightly later on too
//		app.addCommonStylesheets(
//				content instanceof Parent ? ((Parent) content).getStylesheets() : content.getParent().getStylesheets());
		

		if (app.isDecorated()) {
			scene = new Scene(ui);
		} else {
			var primaryScene = new BorderlessScene(stage, borderlessStageStyle(), ui, 1, 1);

			primaryScene.setMoveControl(ui.getTop());
			primaryScene.setDoubleClickMaximizeEnabled(false);
			primaryScene.setSnapEnabled(false);
			primaryScene.removeDefaultCSS();
			primaryScene.setResizable(true);

			primaryScene.getRoot().getStyleClass().add("borderless-root");

			scene = primaryScene;
			scene.setFill(Color.TRANSPARENT);
		}
		stage.focusedProperty().addListener((c, o, n) -> {
			setStageFocusStyles(scene.getRoot(), n);
		});
		setStageFocusStyles(scene.getRoot(), stage.isFocused());
		setStagePlatformStyles(scene.getRoot());
		
		appStyle = new AppStyle(scene, app.isDarkMode());
		var stylesheets = scene.getStylesheets();
		defaultStylesheets = new LinkedHashSet<>(stylesheets);
		app.addCommonStylesheets(stylesheets);
		if(width == 0 || height == 0) {
    		stage.setWidth(760);
    		stage.setHeight(680);
		}
		else {
	        stage.setWidth(width);
	        stage.setHeight(height);
		}
		stage.setScene(scene);

		app.getWindows().add(this); 
		
		stage.setOnHidden(this::onClose);
	}
	
	public void setContent(Node content) {
		ui.setCenter(content);
		updateDarkMode();
	}

	public void reloadCss() {
		scene.getStylesheets().clear();
		scene.getStylesheets().addAll(defaultStylesheets);
		app.addCommonStylesheets(scene.getStylesheets());
	}
	
	public void configurePersistentGeometry(Rectangle2D limits, Rectangle2D configuredBounds, Consumer<Rectangle2D> onUpdate) {

	    if(configuredBounds == null) {
	        stage.centerOnScreen();
	    }
	    else {
	        stage.setX(configuredBounds.getMinX());
            stage.setY(configuredBounds.getMinY());
            stage.setWidth(configuredBounds.getWidth());
            stage.setHeight(configuredBounds.getHeight());
	    }

        if(limits != null) {
            stage.setMaxWidth(limits.getMaxX());
            stage.setMinWidth(limits.getMinX());
            stage.setMaxHeight(limits.getMaxY());
            stage.setMinHeight(limits.getMinY());
        }
	    
	    if(onUpdate != null) {
            ChangeListener<? super Number> l = (c,o,n) -> onUpdate.accept(new Rectangle2D(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));;
            stage.xProperty().addListener(l );
            stage.yProperty().addListener(l);
            stage.widthProperty().addListener(l);
            stage.heightProperty().addListener(l);
	    }
	}

	public ImageView getTitleImage() {
        return titleImage;
    }

    public void setTitleImage(ImageView titleImage) {
        this.titleImage = titleImage;
        checkFrameTitle();
    }

    public void updateDarkMode() {
    	appStyle.updateDarkMode(app.isDarkMode());
		app.updateRootStyles(scene.getRoot());
	}

	public void onClose(WindowEvent evt) {
		app.getWindows().remove(this);
	}
	
	public Node content() {
		return ui.getCenter();
	}

	public Scene scene() {
		return scene;
	}

	public final Stage stage() {
		return stage;
	}

	public final TitleBar titleBar() {
		return titleBar;
	}

	public StageStyle borderlessStageStyle() {
		return StageStyle.TRANSPARENT;
	}
	
	public void setKeepInBounds(boolean keepInBounds) {
	    if(this.keepInBounds != keepInBounds) {
	        this.keepInBounds = keepInBounds;
	        if(keepInBounds) {
	            keepInBounds();
	            Screen.getScreens().addListener(this);
	        }
	        else {
                Screen.getScreens().removeListener(this);
	        }
	    }
	}

    protected void keepInBounds() {

        var screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(),
                stage.getHeight());
        var screen = screens.isEmpty() ? Screen.getPrimary() : screens.get(0);
        var bounds = screen.getVisualBounds();

        if (stage.getX() < bounds.getMinX()) {
            stage.setX(bounds.getMinX());
        } else if (stage.getX() + stage.getWidth() > bounds.getMaxX()) {
            stage.setX(bounds.getMaxX() - stage.getWidth());
        }
        if (stage.getY() < bounds.getMinY()) {
            stage.setY(bounds.getMinY());
        } else if (stage.getY() + stage.getHeight() > bounds.getMaxY()) {
            stage.setY(bounds.getMaxY() - stage.getHeight());
        }
    }

	protected TitleBar createTitleBar() {
		return Platforms.style().titleBar();
	}

	protected void setStagePlatformStyles(Node ui) {
		Platforms.style().configureStageRootStyles(ui);
	}
	
	protected void setStageFocusStyles(Node ui, Boolean n) {
		var sc = ui.getStyleClass();
		if (n) {
			sc.add("stage-focused");
			sc.remove("stage-unfocused");
		} else {
			sc.remove("stage-focused");
			sc.add("stage-unfocused");
		}
	}

	final void checkFrameTitle() {
		if (titleBar != null) {
		    if(titleNode != null) {
                titleBar.getTitleStack().getChildren().remove(titleNode);
		    }
		    if(titleImage == null) {
		        if(titleLabel == null) {
	                titleLabel = new Label();
	                titleLabel.getStyleClass().add("title-label");
	                titleLabel.textProperty().bind(stage.titleProperty());
		        }
                titleBar.getTitleStack().getChildren().add(titleNode = titleLabel);
		    }
		    else {
                titleBar.getTitleStack().getChildren().add(titleNode = titleImage);
		    }
		}
	}

    @Override
    public void onChanged(Change<? extends Screen> c) {
        keepInBounds();
    }
}
