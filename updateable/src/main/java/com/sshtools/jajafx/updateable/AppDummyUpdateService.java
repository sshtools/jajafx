package com.sshtools.jajafx.updateable;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

import com.sshtools.jaul.DummyUpdater.DummyUpdaterBuilder;

public class AppDummyUpdateService extends AbstractAppUpdateService {

	public AppDummyUpdateService(UpdateableJajaApp<? extends UpdateableJajaFXApp<?, ?>, ?> context) {
		this(context, null);
	}

	public AppDummyUpdateService(UpdateableJajaApp<? extends UpdateableJajaFXApp<?, ?>, ?> context, Function<Long, ScheduledFuture<?>> checkScheduler) {
		super(context, checkScheduler);
	}

	@Override
	protected String doUpdate(boolean check) throws IOException {
		return DummyUpdaterBuilder.builder().withCheckOnly(check).build().call();
	}

}
