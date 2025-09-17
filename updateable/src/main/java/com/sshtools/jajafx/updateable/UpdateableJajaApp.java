package com.sshtools.jajafx.updateable;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.install4j.api.launcher.StartupNotification;
import com.install4j.api.launcher.StartupNotification.Listener;
import com.sshtools.jajafx.JajaApp;
import com.sshtools.jajafx.JajaFXApp;
import com.sshtools.jaul.AppRegistry;
import com.sshtools.jaul.AppRegistry.App;
import com.sshtools.jaul.Phase;
import com.sshtools.jaul.UpdateableAppContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public abstract class UpdateableJajaApp<FXA extends UpdateableJajaFXApp<?, WND>, WND extends UpdateableJajaFXAppWindow<FXA>> extends JajaApp<FXA, WND> {

	public static abstract class UpdateableJajaAppBuilder<BA extends UpdateableJajaApp<BFXA, ?>, BB extends UpdateableJajaAppBuilder<BA, BB, BFXA>, BFXA extends UpdateableJajaFXApp<?, ?>> extends
		JajaApp.JajaAppBuilder<BA, BB,BFXA> {

		private Optional<Phase> defaultPhase = Optional.empty();

		public BB withDefaultPhase(Phase defaultPhase) {
			return withDefaultPhase(Optional.of(defaultPhase));
		}

		@SuppressWarnings("unchecked")
		public BB withDefaultPhase(Optional<Phase> defaultPhase) {
			this.defaultPhase = defaultPhase;
			return (BB) this;
		}

		public abstract BA build();
	}
	
	@Deprecated
	@Option(names = { "--jaul-register" }, hidden = true, description = "Register this application with the JADAPTIVE update system and exit. Usually only called on installation.")
	boolean jaulRegister;

	@Deprecated
	@Option(names = { "--jaul-deregister" }, hidden = true, description = "De-register this application from the JADAPTIVE update system and exit. Usually only called on uninstallation.")
	boolean jaulDeregister;

	private final Optional<Phase> defaultPhase;
	
	private AppUpdateService updateService;
	private Optional<App> app;
	private Optional<Listener> onOpenRequest = Optional.empty();
	
	static Logger LOG = LoggerFactory.getLogger(UpdateableJajaApp.class);

	protected UpdateableJajaApp(UpdateableJajaAppBuilder<?, ?, ?> builder) {
		super(builder);
		this.defaultPhase = builder.defaultPhase;
	}

	public final void setOnOpenRequest(Listener onOpenRequest) {
		this.onOpenRequest = Optional.ofNullable(onOpenRequest);
	}

	public final Preferences getAppPreferences() {
		return AppRegistry.getBestAppPreferences(app, this);
	}

	public final Preferences getUserPreferences() {
		return AppRegistry.getBestUserPreferences(app, this);
	}

	public final Phase getDefaultPhaseForVersion() {
		return Phase.getDefaultPhaseForVersion(spec.version());
	}

	public UpdateableAppContext getUpdateContext() {
		return new UpdateableAppContext() {

			@Override
			public void setUpdatesDeferredUntil(long timeMs) {
				getAppPreferences().putLong(AppRegistry.KEY_DEFER, timeMs);
			}

			@Override
			public long getUpdatesDeferredUntil() {
				return getAppPreferences().getLong(AppRegistry.KEY_DEFER, 0);
			}

			@Override
			public Phase getPhase() {
				return Phase.valueOf(
						getAppPreferences().get(AppRegistry.KEY_PHASE, defaultPhase.orElse(getDefaultPhaseForVersion()).name()));
			}

			@Override
			public void setPhase(Phase phase) {
				getAppPreferences().put(AppRegistry.KEY_PHASE, phase.name());

			}

			@Override
			public boolean isAutomaticUpdates() {
				return getAppPreferences().getBoolean(AppRegistry.KEY_AUTOMATIC_UPDATES, true);
			}

			@Override
			public void setAutomaticUpdates(boolean automaticUpdates) {
				getAppPreferences().putBoolean(AppRegistry.KEY_AUTOMATIC_UPDATES, automaticUpdates);
			}

			@Override
			public ScheduledExecutorService getScheduler() {
				return scheduler;
			}

			@Override
			public String getVersion() {
				return spec.version()[0];
			}
		};
	}
	
	public final App getRegisteredApp() {
		return app.orElseThrow(() -> new IllegalStateException("No registered app."));
	}

	public final AppUpdateService getUpdateService() {
		if (updateService == null) {
			updateService = createUpdateService();
	        updateService.needsUpdatingProperty().addListener((c, o, n) -> getFXApp().needUpdate());
	        updateService.rescheduleCheck();
		}
		return updateService;
	}

	public final Integer call() throws Exception {
		initCall();
		if(jaulDeregister) {
			AppRegistry.get().deregister(getClass());
			return 0;
		}
		else if(jaulRegister) {
			AppRegistry.get().register(getClass());
			return 0;
		}
		else {
			app = locateApp();
			
			app.ifPresent(a -> onOpenRequest.ifPresent(o -> StartupNotification.registerStartupListener(o)));
			
			beforeCall();
			JajaFXApp.launch(appClazz, new String[0]);
			return 0;
		}
	}
	
	protected void setHandleQuit(boolean handleQuit) {
		StartupNotification.setHandleQuit(handleQuit);
	}

	protected final AppUpdateService createUpdateService() {
		try {
			if ("true".equals(System.getProperty("jajafx.dummyUpdates"))) {
				return new AppDummyUpdateService(this);
			}
			if(app.isPresent()) {
				return new AppInstall4JUpdateService(this, app.get());
			}
			else
				return createDefaultUpdateService();
						
		} catch (Throwable t) {
			if (LOG.isDebugEnabled())
				LOG.info("Failed to create Install4J update service, using dummy service.", t);
			else
				LOG.info("Failed to create Install4J update service, using dummy service. {}", t.getMessage());
			return createDefaultUpdateService();
		}
	}

	protected AppUpdateService createDefaultUpdateService() {
		return new AppNoUpdateService(this);
	}

	public final void update(Consumer<IOException> onError) {
		getScheduler().execute(() -> {
			try {
				updateService.update();
			} catch (IOException ioe) {
				LOG.error("Failed to update.", ioe);
				onError.accept(ioe);
			}
		});
	}

	public final void updateCheck(Consumer<Boolean> onResult, Consumer<IOException> onError) {
		getScheduler().execute(() -> {
			try {
				getUpdateService().checkForUpdate();
				onResult.accept(updateService.isNeedsUpdating());
			} catch (IOException ioe) {
				LOG.error("Failed to check for updates.", ioe);
				onError.accept(ioe);
			}
		});
	}

	private Optional<App> locateApp() {
		try {
			return Optional.of(AppRegistry.get().launch(this.getClass()));
		}
		catch(Exception e) {
			LOG.warn(MessageFormat.format("Failed to determine app installation. No update features will be available, and application preferences root is now determined by the class name {0}. {1}", getClass().getName(), e.getMessage() == null ? "No message supplied." : e.getMessage()));
			return Optional.empty();
		}
	}
}
