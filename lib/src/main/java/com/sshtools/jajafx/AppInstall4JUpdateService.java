package com.sshtools.jajafx;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sshtools.jaul.AppRegistry.App;
import com.sshtools.jaul.Install4JUpdater.Install4JUpdaterBuilder;

public class AppInstall4JUpdateService extends AbstractAppUpdateService {

	static Logger log = LoggerFactory.getLogger(AppInstall4JUpdateService.class);

	private final App app;

	public AppInstall4JUpdateService(JajaApp<? extends JajaFXApp<?, ?>, ?> context, App app) {
		super(context);
		this.app = app;
	}

	protected String buildUpdateUrl() {
		return app.getUpdatesUrl().orElseThrow(() -> new IllegalStateException("No updates URL set."))
				.replace("${phase}", getContext().getPhase().name().toLowerCase());
	}

	@Override
	protected String doUpdate(boolean checkOnly) throws IOException {
		return Install4JUpdaterBuilder.builder().
				withCheckOnly(checkOnly).
				withCurrentVersion(context.getCommandSpec().version()[0]).withLauncherId(app.getLauncherId()).
				withUpdateUrl(buildUpdateUrl()).onExit((e) -> context.exit(e)).
				build().call();

	}

}
