package com.sshtools.jajafx.updateable;

import com.sshtools.jajafx.JajaFXAppWindow;

import javafx.stage.Stage;

public class UpdateableJajaFXAppWindow<A extends UpdateableJajaFXApp<?, ?>> extends JajaFXAppWindow<A> {

	public UpdateableJajaFXAppWindow(Stage stage, A app, double width, double height) {
		super(stage, app, width, height);
	}

	public UpdateableJajaFXAppWindow(Stage stage, A app) {
		super(stage, app);
	}

	
}
