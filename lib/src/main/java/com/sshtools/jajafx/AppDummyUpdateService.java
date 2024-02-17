package com.sshtools.jajafx;

import java.io.IOException;

import com.sshtools.jaul.DummyUpdater.DummyUpdaterBuilder;

public class AppDummyUpdateService extends AbstractAppUpdateService {

	public AppDummyUpdateService(JajaApp<? extends JajaFXApp<?>> context) {
		super(context);
	}

	@Override
	protected String doUpdate(boolean check) throws IOException {
		return DummyUpdaterBuilder.builder().withCheckOnly(check).build().call();
	}

}
