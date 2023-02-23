package com.sshtools.jajafx;

import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command
public abstract class JajaApp<A extends JajaFXApp> implements Callable<Integer> {

	@Option(names = { "-W", "--standard-window-decorations" }, description = "Use standard window decorations.")
	boolean standardWindowDecorations;
	
	@Option(names = { "-d", "--dark" }, paramLabel = "PATH", description = "Use a dark theme.")
	boolean darkMode;

	private Class<? extends JajaFXApp> appClazz;
	private static JajaApp<?> instance;

	protected JajaApp(Class<A> appClazz) {
		instance = this;
		this.appClazz = appClazz;
	}
	
	public static JajaApp<?> getInstance() {
		return instance;
	}

	public Integer call() throws Exception {
		JajaFXApp.launch(appClazz, new String[0]);
		return 0;
	}
}
