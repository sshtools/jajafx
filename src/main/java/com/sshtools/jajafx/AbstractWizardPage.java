package com.sshtools.jajafx;

import javafx.scene.Scene;

public abstract class AbstractWizardPage<C> implements WizardPage<C> {

	private Scene scene;
	private Wizard<C> wizard;
	private C context;

	@Override
	public final void configure(Scene scene, Wizard<C> wizard, C context) {
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
	
	protected Wizard<C> getWizard() {
		return wizard;
	}

	@Override
	public void shown() {
	}

	@Override
	public void hidden() {
	}

}
