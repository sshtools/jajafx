package com.sshtools.jajafx;

public interface FrameworkConfig {
	
	boolean isAutomaticUpdates();
	
	void setAutomaticUpdates(boolean automaticUpdates);
	
	Phase getPhase();
	
	void setPhase(Phase phase);

	long getUpdatesDeferredUntil();

	void setUpdatesDeferredUntil(long timeMs);
}
