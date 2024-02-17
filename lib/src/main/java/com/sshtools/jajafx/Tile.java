package com.sshtools.jajafx;

import java.io.Closeable;

import javafx.scene.Scene;

public interface Tile<C> extends Closeable {
	void configure(Scene scene, Tiles<C> wizard, C context);

	void shown();
	
	void hidden();

	Scene getScene();
	
	void close();

	default boolean validate() {
		return true;
	}

}
