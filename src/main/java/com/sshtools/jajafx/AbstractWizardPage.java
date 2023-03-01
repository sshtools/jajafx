package com.sshtools.jajafx;

public abstract class AbstractWizardPage<C> extends AbstractTile<C> {

	protected Wizard<C> getWizard() {
		return (Wizard<C>) getTiles();
	}
}
