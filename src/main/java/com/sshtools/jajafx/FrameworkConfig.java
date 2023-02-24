package com.sshtools.jajafx;

public interface FrameworkConfig {
	
	Phase getPhase();
	
	void setPhase(Phase phase);

	long getUpdatesDeferredUntil();

	void setUpdatesDeferredUntil(long timeMs);
}
