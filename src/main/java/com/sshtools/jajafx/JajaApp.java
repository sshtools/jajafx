package com.sshtools.jajafx;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command
public abstract class JajaApp<FXA extends JajaFXApp> implements Callable<Integer> {

	public static abstract class JajaAppBuilder<BA extends JajaApp<BFXA>, BB extends JajaAppBuilder<BA, BB, BFXA>, BFXA extends JajaFXApp> {

		private Optional<String> updatesUrl = Optional.empty();
		private Optional<String> defaultPhase = Optional.empty();
		private Optional<Class<? extends JajaFXApp>> appClazz = Optional.empty();
		private Optional<String> launcherId = Optional.empty();
		
		@SuppressWarnings("unchecked")
		public BB withApp(Class<? extends JajaFXApp> appClazz) {
			this.appClazz = Optional.of(appClazz);
			return (BB)this;
		}
		
		public BB withUpdatesUrl(String updatesUrl) {
			return withUpdatesUrl(Optional.of(updatesUrl));
		}

		@SuppressWarnings("unchecked")
		public BB withUpdatesUrl(Optional<String> updatesUrl) {
			this.updatesUrl = updatesUrl;
			return (BB) this;
		}

		public BB withDefaultPhase(String defaultPhase) {
			return withDefaultPhase(Optional.of(defaultPhase));
		}

		@SuppressWarnings("unchecked")
		public BB withDefaultPhase(Optional<String> defaultPhase) {
			this.defaultPhase = defaultPhase;
			return (BB) this;
		}

		public BB withLauncherId(String launcherId) {
			return withLauncherId(Optional.of(launcherId));
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

	private final Class<? extends JajaFXApp> appClazz;
	private final Optional<String> updatesUrl;
	private final Optional<String> defaultPhase;
	private final Optional<String> launcherId;

	private UpdateService updateService;

	private static JajaApp<?> instance;

	protected JajaApp(JajaAppBuilder<?, ?,?> builder) {
		instance = this;
		this.appClazz = builder.appClazz.orElseThrow(() -> new IllegalStateException("App class must be provided"));
		this.updatesUrl = builder.updatesUrl;
		this.defaultPhase = builder.defaultPhase;
		this.launcherId = builder.launcherId;
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
			public String getPhase() {
				return Preferences.userNodeForPackage(JajaApp.this.getClass()).get("phase",
						defaultPhase.orElse("default"));
			}

			@Override
			public void setPhase(String phase) {
				Preferences.userNodeForPackage(JajaApp.this.getClass()).put("phase", phase);

			}
		};
	}

	public String getUpdatesUrl() {
		if (updatesUrl.isPresent()) {
			return updatesUrl.get().replace("${phase}", getFrameworkConfiguration().getPhase());
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
}
