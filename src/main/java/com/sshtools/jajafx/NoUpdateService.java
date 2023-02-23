package com.sshtools.jajafx;

import java.io.IOException;

public class NoUpdateService implements UpdateService {

	private JajaApp<? extends JajaFXApp> context;

	public NoUpdateService(JajaApp<? extends JajaFXApp> context) {
		this.context = context;
	}

	@Override
	public void addDownloadListener(DownloadListener listener) {
	}

	@Override
	public void removeDownloadListener(DownloadListener listener) {
	}

	@Override
	public void addListener(Listener listener) {
	}

	@Override
	public void removeListener(Listener listener) {
	}

	@Override
	public boolean isNeedsUpdating() {
		return false;
	}

	@Override
	public boolean isUpdating() {
		return false;
	}

	@Override
	public String[] getPhases() {
		return new String[0];
	}

	@Override
	public String getAvailableVersion() {
		return context.getCommandSpec().version()[0];
	}

	@Override
	public void deferUpdate() {
	}

	@Override
	public boolean isUpdatesEnabled() {
		return false;
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
	public void checkIfBusAvailable() {
	}

}
