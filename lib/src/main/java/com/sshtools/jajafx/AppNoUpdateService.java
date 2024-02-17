package com.sshtools.jajafx;

import com.sshtools.jaul.NoUpdateService;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class AppNoUpdateService extends NoUpdateService implements AppUpdateService {

	private ReadOnlyBooleanProperty updating = new SimpleBooleanProperty();
	private ReadOnlyBooleanProperty needsUpdating = new SimpleBooleanProperty();
	private ReadOnlyBooleanProperty checkOnly = new SimpleBooleanProperty();
	private ReadOnlyStringProperty availableVersionProperty = new SimpleStringProperty();

	public AppNoUpdateService(JajaApp<? extends JajaFXApp<?>> context) {
		super(context.getUpdateContext());
	}

	@Override
	public ReadOnlyBooleanProperty updatingProperty() {
		return updating;
	}

	@Override
	public ReadOnlyStringProperty availableVersionProperty() {
		return availableVersionProperty;
	}

	@Override
	public ReadOnlyBooleanProperty needsUpdatingProperty() {
		return needsUpdating;
	}

	@Override
	public ReadOnlyBooleanProperty checkOnlyProperty() {
		return checkOnly;
	}

}
