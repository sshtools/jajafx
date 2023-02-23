package com.sshtools.jajafx;

import java.io.IOException;


public class DummyUpdateService extends AbstractUpdateService {

	public DummyUpdateService(JajaApp<? extends JajaFXApp> context) {
		super(context);
	}

	@Override
	protected String doUpdate(boolean check) throws IOException {
		return null;
	}

}
