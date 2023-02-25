package com.sshtools.jajafx;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sshtools.jaul.Install4JUpdater.Install4JUpdaterBuilder;

public class Install4JUpdateServiceImpl extends AbstractAppUpdateService {

	static Logger log = LoggerFactory.getLogger(Install4JUpdateServiceImpl.class);

	public Install4JUpdateServiceImpl(JajaApp<? extends JajaFXApp<?>> context) {
		super(context);
	}

	protected String buildUpdateUrl() {
		return context.getUpdatesUrl();
	}

	@Override
	protected String doUpdate(boolean checkOnly) throws IOException {
		return Install4JUpdaterBuilder.builder().
				withCheckOnly(checkOnly).
				withCurrentVersion(context.getCommandSpec().version()[0]).
				withLauncherId(context.getLauncherId()).
				withUpdateUrl(context.getUpdatesUrl()).
				onExit((e) -> context.exit(e)).build().call();

	}

}
