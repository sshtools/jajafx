package com.sshtools.jajafx;

import java.io.IOException;
import java.io.InterruptedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.install4j.api.context.UserCanceledException;
import com.install4j.api.launcher.ApplicationLauncher;
import com.install4j.api.update.ApplicationDisplayMode;
import com.install4j.api.update.UpdateChecker;
import com.install4j.api.update.UpdateDescriptor;

public class Install4JUpdateServiceImpl extends AbstractUpdateService {

	static Logger log = LoggerFactory.getLogger(Install4JUpdateServiceImpl.class);

	public Install4JUpdateServiceImpl(JajaApp<? extends JajaFXApp> context) {
		super(context);
	}

	protected String buildUpdateUrl() {
		return context.getUpdatesUrl();
	}

	@Override
	protected String doUpdate(boolean checkOnly) throws IOException {

		var uurl = buildUpdateUrl();
		var versions = context.getCommandSpec().version()[0];
		log.info("Check for updates in " + versions + " from " + uurl);
		UpdateDescriptor update;
		try {
			update = UpdateChecker.getUpdateDescriptor(uurl,
					context.isConsoleMode() ? ApplicationDisplayMode.CONSOLE : ApplicationDisplayMode.GUI);
			var best = update.getPossibleUpdateEntry();
			if (best == null) {
				log.info("No version available.");
				return System.getProperty("jajafx.fakeUpdateVersion");
			}

			var availableVersion = best.getNewVersion();
			log.info(availableVersion + " is available.");

			/* TODO: This will allow downgrades. */
			if (!availableVersion.equals(versions)) {
				log.info("Update available.");
			} else {
				log.info("No update needed.");
				return null;
			}

			if (checkOnly) {
				return availableVersion;
			} else {
				if (!isNeedsUpdating())
					throw new IOException("Update not needed.");
				String[] args;
				if(context.isConsoleMode())
					args =new String[] { "-c" };
				else
					args = new String[0];
				ApplicationLauncher.launchApplicationInProcess("2103", args,
						new ApplicationLauncher.Callback() {
							public void exited(int exitValue) {
								context.exit();
							}

							public void prepareShutdown() {
								// TODO add your code here (not invoked on event dispatch thread)
							}
						}, ApplicationLauncher.WindowMode.FRAME, null);
			}
		} catch (UserCanceledException e) {
			log.info("Cancelled.");
			throw new InterruptedIOException("Cancelled.");
		} catch (Exception e) {
			log.info("Failed.", e);
		}
		return null;

	}

}
