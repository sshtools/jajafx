package com.sshtools.jajafx;

import java.io.IOException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command
public abstract class JajaApp<FXA extends JajaFXApp<?>> implements Callable<Integer> {

	public static abstract class JajaAppBuilder<BA extends JajaApp<BFXA>, BB extends JajaAppBuilder<BA, BB, BFXA>, BFXA extends JajaFXApp<?>> {

		private Optional<String> updatesUrl = Optional.empty();
		private Optional<Phase> defaultPhase = Optional.empty();
		private Optional<Class<? extends JajaFXApp<?>>> appClazz = Optional.empty();
		private Optional<String> launcherId = Optional.empty();
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

		public BB withUpdatesUrl(String updatesUrl) {
			return withUpdatesUrl(Optional.of(updatesUrl));
		}

		@SuppressWarnings("unchecked")
		public BB withUpdatesUrl(Optional<String> updatesUrl) {
			this.updatesUrl = updatesUrl;
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

		public BB withLauncherId(String launcherId) {
			return withLauncherId(Optional.of(launcherId));
		}

		public BB withInceptionYear(int inceptionYear) {
			return withInceptionYear(Optional.of(inceptionYear));
		}

		@SuppressWarnings("unchecked")
		public BB withInceptionYear(Optional<Integer> inceptionYear) {
			this.inceptionYear = inceptionYear;
			return (BB)this;
		}

		@SuppressWarnings("unchecked")
		public BB withLauncherId(Optional<String> launcherId) {
			this.launcherId = launcherId;
			return (BB) this;
		}

		public abstract BA build();
	}

	static Logger log = LoggerFactory.getLogger(Install4JUpdateServiceImpl.class);

	@Option(names = { "-W", "--standard-window-decorations" }, description = "Use standard window decorations.")
	boolean standardWindowDecorations;

	@Option(names = { "-d", "--dark" }, paramLabel = "PATH", description = "Use a dark theme.")
	boolean darkMode;

	@Spec
	CommandSpec spec;

	private final Class<? extends JajaFXApp<?>> appClazz;
	private final Optional<String> updatesUrl;
	private final Optional<Phase> defaultPhase;
	private final Optional<String> launcherId;
	private final Optional<Integer> inceptionYear;
	private final ResourceBundle appResources;

	private UpdateService updateService;

	private boolean checking;

	protected ScheduledExecutorService scheduler;
	private static JajaApp<?> instance;

	protected JajaApp(JajaAppBuilder<?, ?, ?> builder) {
		instance = this;
		this.appResources = builder.appResources.orElseThrow(() -> new IllegalStateException("App resources must be provided."));
		this.inceptionYear = builder.inceptionYear;
		this.appClazz = builder.appClazz.orElseThrow(() -> new IllegalStateException("App class must be provided"));
		this.updatesUrl = builder.updatesUrl;
		this.defaultPhase = builder.defaultPhase;
		this.launcherId = builder.launcherId;

		scheduler = Executors.newScheduledThreadPool(1);
	}

	public final ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public static JajaApp<?> getInstance() {
		return instance;
	}

	public void exit() {
		exit(0);
	}

	public void exit(int code) {
		System.exit(code);
	}

	public FrameworkConfig getFrameworkConfiguration() {
		return new FrameworkConfig() {

			@Override
			public void setUpdatesDeferredUntil(long timeMs) {
				Preferences.userNodeForPackage(JajaApp.this.getClass()).putLong("updatesDeferredUntil", timeMs);
			}

			@Override
			public long getUpdatesDeferredUntil() {
				return Preferences.userNodeForPackage(JajaApp.this.getClass()).getLong("updatesDeferredUntil", 0);
			}

			@Override
			public Phase getPhase() {
				return Phase.valueOf(Preferences.userNodeForPackage(JajaApp.this.getClass()).get("phase",
						defaultPhase.orElse(Phase.STABLE).name()));
			}

			@Override
			public void setPhase(Phase phase) {
				Preferences.userNodeForPackage(JajaApp.this.getClass()).put("phase", phase.name());

			}
		};
	}

	public String getUpdatesUrl() {
		if (updatesUrl.isPresent()) {
			return updatesUrl.get().replace("${phase}", getFrameworkConfiguration().getPhase().name().toLowerCase());
		} else
			throw new IllegalArgumentException("App has been configured without ");
	}

	public boolean isConsoleMode() {
		return false;
	}

	public String getLauncherId() {
		return launcherId.orElseThrow(() -> new IllegalStateException("No launcher ID provided."));
	}

	public UpdateService getUpdateService() {
		if (updateService == null)
			updateService = createUpdateService();
		return updateService;
	}

	public CommandSpec getCommandSpec() {
		return spec;
	}

	public Integer call() throws Exception {
		JajaFXApp.launch(appClazz, new String[0]);
		return 0;
	}

	protected UpdateService createUpdateService() {
		try {
			if ("true".equals(System.getProperty("jajafx.dummyUpdates"))) {
				return new DummyUpdateService(this);
			}
			return new Install4JUpdateServiceImpl(this);
		} catch (Throwable t) {
			if (log.isDebugEnabled())
				log.info("Failed to create Install4J update service, using dummy service.", t);
			else
				log.info("Failed to create Install4J update service, using dummy service. {}", t.getMessage());
			return new NoUpdateService(this);
		}
	}

	public int getInceptionYear() {
		return inceptionYear.orElse(1900);
	}

	public ResourceBundle getAppResources() {
		return appResources;
	}

	public void update(Consumer<IOException> onError) {
		new Thread() {
			public void run() {
				try {
					updateService.update();
				} catch (IOException ioe) {
					log.error("Failed to update.", ioe);
					onError.accept(ioe);
				}
			}
		}.start();
	}
	
	public void updateCheck(Consumer<Boolean> onResult, Consumer<IOException> onError) {
		if(checking)
			throw new IllegalArgumentException("Already updating.");
		checking = true;
		new Thread() {
			public void run() {
				checkForUpdate(onResult, onError);
			}
		}.start();
	}
	
	private void checkForUpdate(Consumer<Boolean> onResult, Consumer<IOException> onError) {
		try {
			getUpdateService().checkForUpdate();
			onResult.accept(updateService.isNeedsUpdating());
		} catch (IOException ioe) {
			log.error("Failed to check for updates.", ioe);
			onError.accept(ioe);
		} finally {
			checking = false;
		}
	}
}
