package com.sshtools.jajafx.updateable;

import java.io.IOException;
import java.util.function.Consumer;

import com.sshtools.jaul.Phase;
import com.sshtools.jaul.UpdateableAppContext;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public class DelegatingAppUpdateService implements AppUpdateService {
	private final AppUpdateService delegate;

	public DelegatingAppUpdateService(AppUpdateService delegate) {
		this.delegate = delegate;
	}

	@Override
	public void update() throws IOException {
		delegate.update();
	}

	@Override
	public void shutdown() {
		delegate.shutdown();
	}

	@Override
	public void removeDownloadListener(DownloadListener listener) {
		delegate.removeDownloadListener(listener);
	}

	@Override
	public boolean isUpdatesEnabled() {
		return delegate.isUpdatesEnabled();
	}

	@Override
	public Phase[] getPhases() {
		return delegate.getPhases();
	}

	@Override
	public UpdateableAppContext getContext() {
		return delegate.getContext();
	}

	@Override
	public void deferUpdate() {
		delegate.deferUpdate();
	}

	@Override
	public void checkForUpdate() throws IOException {
		delegate.checkForUpdate();
	}

	@Override
	public void addDownloadListener(DownloadListener listener) {
		delegate.addDownloadListener(listener);
	}

	@Override
	public ReadOnlyBooleanProperty updatingProperty() {
		return delegate.updatingProperty();
	}

	@Override
	public ReadOnlyBooleanProperty needsUpdatingProperty() {
		return delegate.needsUpdatingProperty();
	}

	@Override
	public ReadOnlyStringProperty availableVersionProperty() {
		return delegate.availableVersionProperty();
	}

	@Override
	public void setOnAvailableVersion(Consumer<String> onAvailableVersion) {
		delegate.setOnAvailableVersion(onAvailableVersion);
	}

	@Override
	public boolean isCheckOnly() {
		return delegate.isCheckOnly();
	}

	@Override
	public void setOnBusy(Consumer<Boolean> busy) {
		delegate.setOnBusy(busy);
	}

	@Override
	public ReadOnlyBooleanProperty checkOnlyProperty() {
		return delegate.checkOnlyProperty();
	}
}