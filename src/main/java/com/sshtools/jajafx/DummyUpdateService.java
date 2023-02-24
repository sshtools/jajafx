package com.sshtools.jajafx;

import java.io.IOException;


public class DummyUpdateService extends AbstractUpdateService {

	public DummyUpdateService(JajaApp<? extends JajaFXApp<?>> context) {
		super(context);
	}

	@Override
	protected String doUpdate(boolean check) throws IOException {
		if(check) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
			return "999.999.999";
		}
		else {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			throw new IOException("Failed to update.");
		}
	}

}
