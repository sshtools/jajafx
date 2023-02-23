package com.sshtools.jajafx;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractUpdateService implements UpdateService {

	static Logger log = LoggerFactory.getLogger(AbstractUpdateService.class);

	private List<Listener> listeners = new ArrayList<>();
	private List<DownloadListener> downloadListeners = new ArrayList<>();
	private boolean updating;
	private String availableVersion;
	private ScheduledFuture<?> checkTask;
	private long deferUntil;

	protected JajaApp<? extends JajaFXApp> context;
	protected ScheduledExecutorService scheduler;

	protected AbstractUpdateService(JajaApp<? extends JajaFXApp> context) {
		this.context = context;
		scheduler = Executors.newScheduledThreadPool(1);
		checkIfBusAvailable();
	}

	@Override
	public void checkIfBusAvailable() {
		cancelTask();
		deferUntil = context.getFrameworkConfiguration().getUpdatesDeferredUntil();
		if(deferUntil > 0) {
			rescheduleCheck(TimeUnit.SECONDS.toMillis(12));
		} else
			deferUntil = 0;
	}

	@Override
	public void shutdown() {
		scheduler.shutdown();
	}

	@Override
	public final void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public final void addDownloadListener(DownloadListener listener) {
		downloadListeners.add(listener);
	}

	@Override
	public final boolean isNeedsUpdating() {
		return availableVersion != null;
	}

	@Override
	public final boolean isUpdating() {
		return updating;
	}

	@Override
	public final String getAvailableVersion() {
		return availableVersion == null ? context.getCommandSpec().version()[0] : availableVersion;
	}

	@Override
	public final void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	@Override
	public final void removeDownloadListener(DownloadListener listener) {
		downloadListeners.remove(listener);
	}

	@Override
	public final boolean isUpdatesEnabled() {
		return "false".equals(System.getProperty("hypersocket.development.noUpdates", "false"));
	}

	@Override
	public final void update() throws IOException {
		if (!isNeedsUpdating()) {
			throw new IllegalStateException("An update is not required.");
		}
		update(false);
	}

	@Override
	public final void deferUpdate() {
		setAvailableVersion(null);
		configDeferUpdate();
		context.getFrameworkConfiguration().setUpdatesDeferredUntil(deferUntil);
	}

	@Override
	public final void checkForUpdate() throws IOException {
		setDeferUntil(0);
		log.info("Checking for updates ...");
		update(true);
	}

	protected final void configDeferUpdate() {
		long day = TimeUnit.DAYS.toMillis(1);
		long nowDay = (System.currentTimeMillis() / day) * day;
		long when = nowDay + day + TimeUnit.HOURS.toMillis(12)
				+ (long) (Math.random() * 3.0d * (double) TimeUnit.HOURS.toMillis(3));
		setDeferUntil(when);
		log.info("Deferring update for " + DateFormat.getDateTimeInstance().format(new Date(when)) + " days");
		rescheduleCheck(0);
	}

	protected void rescheduleCheck(long nonDeferredDelay) {
		cancelTask();
		long defer = getDeferUntil();
		long when = defer == 0 ? 0 : defer - System.currentTimeMillis();
		if (when > 0) {
			log.info(String.format("Scheduling next check for %s",
					DateFormat.getDateTimeInstance().format(new Date(defer))));
			checkTask = scheduler.schedule(() -> timedCheck(), when, TimeUnit.MILLISECONDS);
		} else {
			if (nonDeferredDelay == 0) {
				configDeferUpdate();
			} else
				checkTask = scheduler.schedule(() -> timedCheck(), nonDeferredDelay, TimeUnit.MILLISECONDS);
		}
	}

	protected void cancelTask() {
		if (checkTask != null) {
			checkTask.cancel(false);
		}
	}

	protected long getDeferUntil() {
		return deferUntil;
	}

	protected void setDeferUntil(long deferUntil) {
		this.deferUntil = deferUntil;
	}

	protected void timedCheck() {
		try {
			update(true);
		} catch (Exception e) {
			log.error("Failed to automatically check for updates.", e);
		} finally {
			rescheduleCheck(0);
		}
	}

	protected final void update(boolean check) throws IOException {
		if (!isUpdatesEnabled()) {
			log.info("Updates disabled.");
			setAvailableVersion(null);
		} else {
			long defer = getDeferUntil();
			if (!check || defer == 0 || System.currentTimeMillis() >= defer) {
				setDeferUntil(0);
				updating = true;
				try {
					setAvailableVersion(doUpdate(check));
				} finally {
					updating = false;
					if (check) {
						rescheduleCheck(0);
					}
				}
			} else {
				log.info(String.format("Updates deferred until %s",
						DateFormat.getDateTimeInstance().format(new Date(defer))));
			}
		}
	}

	protected void setAvailableVersion(String version) {
		if (!Objects.equals(availableVersion, version)) {
			this.availableVersion = version;
			fireStateChange();
		}
	}

	protected void fireStateChange() {
		for (int i = listeners.size() - 1; i >= 0; i--) {
			listeners.get(i).stateChanged();
		}
	}

	protected void fireDownload(DownloadEvent event) {
		for (int i = downloadListeners.size() - 1; i >= 0; i--) {
			downloadListeners.get(i).downloadEvent(event);
		}
	}

	protected abstract String doUpdate(boolean check) throws IOException;

	private boolean isNightly(String phase) {
		return phase.startsWith("nightly");
	}

	@Override
	public final String[] getPhases() {
		List<String> l = new ArrayList<>();
		for (String p : new String[] { "nightly", "ea", "stable" }) {
			if (!isNightly(p) || (Boolean.getBoolean("logonbox.vpn.updates.nightly")
					|| Boolean.getBoolean("hypersocket.development"))) {
				l.add(p);
			}
		}
		return l.toArray(new String[0]);
	}

}
