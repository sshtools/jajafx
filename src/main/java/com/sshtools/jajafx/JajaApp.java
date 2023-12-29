package com.sshtools.jajafx;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import com.install4j.api.launcher.StartupNotification;
import com.install4j.api.launcher.StartupNotification.Listener;
import com.sshtools.jaul.AppRegistry;
import com.sshtools.jaul.AppRegistry.App;
import com.sshtools.jaul.Phase;
import com.sshtools.jaul.UpdateableAppContext;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command
public abstract class JajaApp<FXA extends JajaFXApp<?>> implements Callable<Integer> {

	public static abstract class JajaAppBuilder<BA extends JajaApp<BFXA>, BB extends JajaAppBuilder<BA, BB, BFXA>, BFXA extends JajaFXApp<?>> {

		private Optional<Phase> defaultPhase = Optional.empty();
		private Optional<Class<? extends JajaFXApp<?>>> appClazz = Optional.empty();
		private Optional<Integer> inceptionYear;
		private Optional<ResourceBundle> appResources = Optional.empty();

		@SuppressWarnings("unchecked")
		public BB withAppResources(ResourceBundle appResources) {
			this.appResources = Optional.of(appResources);
			return (BB) this;
		}

		@SuppressWarnings("unchecked")
		public BB withApp(Class<? extends JajaFXApp<?>> appClazz) {
			this.appClazz = Optional.of(appClazz);
			return (BB) this;
		}

		public BB withDefaultPhase(Phase defaultPhase) {
			return withDefaultPhase(Optional.of(defaultPhase));
		}

		@SuppressWarnings("unchecked")
		public BB withDefaultPhase(Optional<Phase> defaultPhase) {
			this.defaultPhase = defaultPhase;
			return (BB) this;
		}

		public BB withInceptionYear(int inceptionYear) {
			return withInceptionYear(Optional.of(inceptionYear));
		}

		@SuppressWarnings("unchecked")
		public BB withInceptionYear(Optional<Integer> inceptionYear) {
			this.inceptionYear = inceptionYear;
			return (BB) this;
		}

		public abstract BA build();
	}
	
	static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
	}

	static Logger LOG = LoggerFactory.getLogger(JajaApp.class);

	@Option(names = { "-W", "--standard-window-decorations" }, negatable = true, description = "Either prevent or force the use standard window decorations.")
	Optional<Boolean> standardWindowDecorations;

	@Option(names = { "--jaul-register" }, hidden = true, description = "Register this application with the JADAPTIVE update system and exit. Usually only called on installation.")
	boolean jaulRegister;

	@Option(names = { "--jaul-deregister" }, hidden = true, description = "De-register this application from the JADAPTIVE update system and exit. Usually only called on uninstallation.")
	boolean jaulDeregister;

	@Spec
	CommandSpec spec;

	private final Class<? extends JajaFXApp<?>> appClazz;
	private final Optional<Phase> defaultPhase;
	private final Optional<Integer> inceptionYear;
	private final ResourceBundle appResources;
	
	private AppUpdateService updateService;
	protected ScheduledExecutorService scheduler;
	private Optional<App> app;
	private Optional<Listener> onOpenRequest = Optional.empty();
	private FXA fxApp;

	private static JajaApp<?> instance;

	protected JajaApp(JajaAppBuilder<?, ?, ?> builder) {
		instance = this;
		this.appResources = builder.appResources
				.orElseThrow(() -> new IllegalStateException("App resources must be provided."));
		this.inceptionYear = builder.inceptionYear;
		this.appClazz = builder.appClazz.orElseThrow(() -> new IllegalStateException("App class must be provided"));
		this.defaultPhase = builder.defaultPhase;
		scheduler = Executors.newScheduledThreadPool(1);
	}

	@SuppressWarnings("unchecked")
	void init(JajaFXApp<?> fxApp) {
		this.fxApp = (FXA) fxApp;
	}

	public final FXA getFXApp() {
		return fxApp;
	}

	public final void setOnOpenRequest(Listener onOpenRequest) {
		this.onOpenRequest = Optional.ofNullable(onOpenRequest);
	}

	public final ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public final Preferences getAppPreferences() {
		return AppRegistry.getBestAppPreferences(app, this);
	}

	public static JajaApp<?> getInstance() {
		return instance;
	}

	public void exit() {
		exit(0);
	}

	public void exit(int code) {
		scheduler.shutdown();
		System.exit(code);
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
		};
	}
	
	public final App getRegisteredApp() {
		return app.orElseThrow(() -> new IllegalStateException("No registered app."));
	}

	public final boolean isConsoleMode() {
		return false;
	}

	public final AppUpdateService getUpdateService() {
		if (updateService == null)
			updateService = createUpdateService();
		return updateService;
	}

	public final CommandSpec getCommandSpec() {
		return spec;
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

	protected void beforeCall() throws Exception {
	}

	protected void initCall() throws Exception {
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

	public final int getInceptionYear() {
		return inceptionYear.orElse(1900);
	}

	public final ResourceBundle getAppResources() {
		return appResources;
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
