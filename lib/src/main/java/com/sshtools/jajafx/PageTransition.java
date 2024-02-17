package com.sshtools.jajafx;

public enum PageTransition {
	FROM_LEFT, FROM_RIGHT, FROM_TOP, FROM_BOTTOM, FADE, NONE;

	public PageTransition opposite() { 
		switch (this) {
		case FROM_LEFT:
			return FROM_RIGHT;
		case FROM_RIGHT:
			return FROM_LEFT;
		case FROM_TOP:
			return FROM_BOTTOM;
		case FROM_BOTTOM:
			return FROM_TOP;
		case FADE:
			return FADE;
		default:
			return NONE;
		}
	}
}
