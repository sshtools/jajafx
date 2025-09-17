package com.sshtools.jajafx.updateable;

import java.io.IOException;

import com.sshtools.jajafx.JajaApp;
import com.sshtools.jajafx.JajaFXApp;
import com.sshtools.jaul.DummyUpdater.DummyUpdaterBuilder;

public class AppDummyUpdateService extends AbstractAppUpdateService {

	public AppDummyUpdateService(UpdateableJajaApp<? extends UpdateableJajaFXApp<?, ?>, ?> context) {
		super(context);
	}

	@Override
	protected String doUpdate(boolean check) throws IOException {
		return DummyUpdaterBuilder.builder().withCheckOnly(check).build().call();
	}

}
