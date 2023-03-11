package com.sshtools.jajafx;

import com.sshtools.jaul.UpdateService;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface AppUpdateService extends UpdateService {

	ReadOnlyBooleanProperty updatingProperty();

	ReadOnlyBooleanProperty needsUpdatingProperty();

	default boolean isNeedsUpdating() {
		return needsUpdatingProperty().get();
	}

	default boolean isUpdating() {
		return updatingProperty().get();
	}

	default String getAvailableVersion() {
		return availableVersionProperty().get();
	}

	ReadOnlyStringProperty availableVersionProperty();

	ReadOnlyBooleanProperty checkOnlyProperty();

}
