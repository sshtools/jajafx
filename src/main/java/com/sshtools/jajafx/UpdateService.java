package com.sshtools.jajafx;

import java.io.IOException;

public interface UpdateService {

	public interface Listener {
		void stateChanged();
	}

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

	void addDownloadListener(DownloadListener listener);

	void removeDownloadListener(DownloadListener listener);

	void addListener(Listener listener);

	void removeListener(Listener listener);

	boolean isNeedsUpdating();

	boolean isUpdating();

	String[] getPhases();

	String getAvailableVersion();

	void deferUpdate();

	boolean isUpdatesEnabled();

	void checkForUpdate() throws IOException;

	void update() throws IOException;

	void shutdown();

	void checkIfBusAvailable();
}
