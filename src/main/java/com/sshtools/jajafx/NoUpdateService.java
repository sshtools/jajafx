package com.sshtools.jajafx;

import java.io.IOException;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

public class NoUpdateService implements UpdateService {

	private ReadOnlyBooleanProperty updating = new SimpleBooleanProperty();
	private ReadOnlyBooleanProperty needsUpdating = new SimpleBooleanProperty();
	private ReadOnlyStringProperty availableVersionProperty = new SimpleStringProperty();

	public NoUpdateService(JajaApp<? extends JajaFXApp<?>> context) {
	}

	@Override
	public void addDownloadListener(DownloadListener listener) {
	}

	@Override
	public void removeDownloadListener(DownloadListener listener) {
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
	public Phase[] getPhases() {
		return new Phase[0];
	}

	@Override
	public void deferUpdate() {
	}

	@Override
	public void checkForUpdate() throws IOException {
	}

	@Override
	public void update() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void shutdown() {
	}

	@Override
	public ReadOnlyBooleanProperty needsUpdatingProperty() {
		return needsUpdating;
	}

	@Override
	public boolean isUpdatesEnabled() {
		return false;
	}

}
