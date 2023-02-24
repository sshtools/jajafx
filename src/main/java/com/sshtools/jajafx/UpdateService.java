package com.sshtools.jajafx;

import java.io.IOException;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface UpdateService {

	public static class DownloadEvent {
		public enum Type {
			START, PROGRESS, END;
		}

		private final Type type;
		private final long value;

		public DownloadEvent(Type type, long value) {
			super();
			this.type = type;
			this.value = value;
		}

		public Type getType() {
			return type;
		}

		public long getValue() {
			return value;
		}

	}

	public interface DownloadListener {
		void downloadEvent(DownloadEvent event);
	}
	
	ReadOnlyBooleanProperty updatingProperty();

	void addDownloadListener(DownloadListener listener);

	void removeDownloadListener(DownloadListener listener);

	ReadOnlyBooleanProperty needsUpdatingProperty();

	default boolean isNeedsUpdating() {
		return needsUpdatingProperty().get();
	}

	default boolean isUpdating() {
		return updatingProperty().get();
	}

	Phase[] getPhases();

	default String getAvailableVersion() {
		return availableVersionProperty().get();
	}

	ReadOnlyStringProperty availableVersionProperty();

	void deferUpdate();

	boolean isUpdatesEnabled();

	void checkForUpdate() throws IOException;

	void update() throws IOException;

	void shutdown();
	
	default void rescheduleCheck() {
	}
}
