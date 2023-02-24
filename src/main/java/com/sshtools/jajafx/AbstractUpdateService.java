package com.sshtools.jajafx;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class AbstractUpdateService implements UpdateService {

	static Logger log = LoggerFactory.getLogger(AbstractUpdateService.class);

	private List<DownloadListener> downloadListeners = new ArrayList<>();
	private BooleanProperty updating = new SimpleBooleanProperty();
	private StringProperty availableVersion = new SimpleStringProperty();;
	private BooleanProperty needsUpdating = new SimpleBooleanProperty();
	private ScheduledFuture<?> checkTask;
	private long deferUntil;

	protected JajaApp<? extends JajaFXApp<?>> context;

	protected AbstractUpdateService(JajaApp<? extends JajaFXApp<?>> context) {
		this.context = context;
		needsUpdating.bind(Bindings.isNotNull(availableVersion));
	}

	@Override
	public final void rescheduleCheck() {
		cancelTask();
		deferUntil = context.getFrameworkConfiguration().getUpdatesDeferredUntil();
		if (deferUntil > 0) {
			rescheduleCheck(TimeUnit.SECONDS.toMillis(12));
		} else
			deferUntil = 0;
	}

	@Override
	public ReadOnlyStringProperty availableVersionProperty() {
		return availableVersion;
	}

	@Override
	public ReadOnlyBooleanProperty needsUpdatingProperty() {
		return needsUpdating;
	}

	@Override
	public void shutdown() {
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
	public final ReadOnlyBooleanProperty updatingProperty() {
		return updating;
	}

	@Override
	public final String getAvailableVersion() {
		return availableVersion.get();
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
		availableVersion.set(null);
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
			checkTask = context.getScheduler().schedule(() -> timedCheck(), when, TimeUnit.MILLISECONDS);
		} else {
			if (nonDeferredDelay == 0) {
				configDeferUpdate();
			} else
				checkTask = context.getScheduler().schedule(() -> timedCheck(), nonDeferredDelay,
						TimeUnit.MILLISECONDS);
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
			availableVersion.set(null);
		} else {
			long defer = getDeferUntil();
			if (!check || defer == 0 || System.currentTimeMillis() >= defer) {
				setDeferUntil(0);
				updating.set(true);
				try {
					availableVersion.set(doUpdate(check));
				} finally {
					updating.set(false);
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

	protected void fireDownload(DownloadEvent event) {
		for (int i = downloadListeners.size() - 1; i >= 0; i--) {
			downloadListeners.get(i).downloadEvent(event);
		}
	}

	protected abstract String doUpdate(boolean check) throws IOException;

	@Override
	public final Phase[] getPhases() {
		return Arrays.asList(Phase.values()).stream()
				.filter(p -> p.equals(Phase.NIGHTLY) || Boolean.getBoolean("jajafx.nightly")
						|| Boolean.getBoolean("jadaptive.development"))
				.collect(Collectors.toList()).toArray(new Phase[0]);
	}

}
