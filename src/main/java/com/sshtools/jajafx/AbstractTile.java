package com.sshtools.jajafx;

import javafx.scene.Scene;

public abstract class AbstractTile<C> implements Tile<C> {

	private Scene scene;
	private Tiles<C> wizard;
	private C context;

	@Override
	public final void configure(Scene scene, Tiles<C> wizard, C context) {
		
		scene.getRoot().setStyle("-fx-background-color: background_color;");
		this.scene = scene;
		this.wizard = wizard;
		this.context = context;
		onConfigure();
	}
	
	protected void onConfigure() {
	}

	@Override
	public void close() {
	}
	
	@Override
	public final  Scene getScene() {
		return scene;
	}
	
	public C getContext() {
		return context;
	}
	
	protected Tiles<C> getTiles() { 
		return wizard;
	}

	@Override
	public void shown() {
	}

	@Override
	public void hidden() {
	}

}
