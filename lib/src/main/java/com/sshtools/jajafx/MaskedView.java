package com.sshtools.jajafx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class MaskedView extends Control {

    public MaskedView(Node content) {
        setContent(content);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MaskedViewSkin(this);
    }

    private final SimpleObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public final Node getContent() {
        return content.get();
    }

    public final SimpleObjectProperty<Node> contentProperty() {
        return content;
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    private final DoubleProperty fadingSize = new SimpleDoubleProperty(this, "fadingSize", 120);

    public final double getFadingSize() {
        return fadingSize.get();
    }

    public final DoubleProperty fadingSizeProperty() {
        return fadingSize;
    }

    public final void setFadingSize(double fadingSize) {
        this.fadingSize.set(fadingSize);
    }
}
