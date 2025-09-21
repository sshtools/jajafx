package com.sshtools.jajafx;

import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

@Command
public abstract class JajaApp<FXA extends JajaFXApp<?, WND>, WND extends JajaFXAppWindow<FXA>> implements Callable<Integer> {

	public static abstract class JajaAppBuilder<BA extends JajaApp<BFXA, ?>, BB extends JajaAppBuilder<BA, BB, BFXA>, BFXA extends JajaFXApp<?, ?>> {

		private Optional<Class<? extends JajaFXApp<?, ?>>> appClazz = Optional.empty();
		private Optional<Integer> inceptionYear;
		private Optional<ResourceBundle> appResources = Optional.empty();

		@SuppressWarnings("unchecked")
		public BB withAppResources(ResourceBundle appResources) {
			this.appResources = Optional.of(appResources);
			return (BB) this;
		}

		@SuppressWarnings("unchecked")
		public BB withApp(Class<? extends JajaFXApp<?, ?>> appClazz) {
			this.appClazz = Optional.of(appClazz);
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

	@Option(names = { "-W", "--standard-window-decorations" }, negatable = true, description = "Either prevent or force the use standard window decorations.")
	Optional<Boolean> standardWindowDecorations;

	@Spec
	protected
	CommandSpec spec;

	protected final Class<? extends JajaFXApp<?, ?>> appClazz;
	private final Optional<Integer> inceptionYear;
	private final ResourceBundle appResources;
	
	protected ScheduledExecutorService scheduler;
	private FXA fxApp;

	private static JajaApp<?, ?> instance;

	protected JajaApp(JajaAppBuilder<?, ?, ?> builder) {
		
		instance = this;
		this.appResources = builder.appResources
				.orElseThrow(() -> new IllegalStateException("App resources must be provided."));
		this.inceptionYear = builder.inceptionYear;
		this.appClazz = builder.appClazz.orElseThrow(() -> new IllegalStateException("App class must be provided"));
		scheduler = Executors.newScheduledThreadPool(1);
	}

	@SuppressWarnings("unchecked")
	public void init(JajaFXApp<?, ?> fxApp) {
		this.fxApp = (FXA) fxApp;
	}

	public final FXA getFXApp() {
		return fxApp;
	}

	public final ScheduledExecutorService getScheduler() {
		return scheduler;
	}

	public static JajaApp<?, ?> getInstance() {
		return instance;
	}

	public void exit() {
		exit(0);
	}

	public void exit(int code) {
		scheduler.shutdown();
		System.exit(code);
	}

	public final boolean isConsoleMode() {
		return false;
	}

	public final CommandSpec getCommandSpec() {
		return spec;
	}

	public Integer call() throws Exception {
		initCall();
		beforeCall();
		JajaFXApp.launch(appClazz, new String[0]);
		return 0;
	}
	
	protected void beforeCall() throws Exception {
	}

	protected void initCall() throws Exception {
	}

	public final int getInceptionYear() {
		return inceptionYear.orElse(1900);
	}

	public final ResourceBundle getAppResources() {
		return appResources;
	}

}
