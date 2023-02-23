package com.sshtools.jajafx;

public interface FrameworkConfig {
	
	String getPhase();
	
	void setPhase(String phase);

	long getUpdatesDeferredUntil();

	void setUpdatesDeferredUntil(long timeMs);
}
