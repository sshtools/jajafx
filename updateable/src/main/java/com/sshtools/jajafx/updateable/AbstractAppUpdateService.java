package com.sshtools.jajafx.updateable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sshtools.jaul.AbstractUpdateService;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class AbstractAppUpdateService extends AbstractUpdateService implements AppUpdateService {

	static Logger log = LoggerFactory.getLogger(AbstractAppUpdateService.class);

	private BooleanProperty updating = new SimpleBooleanProperty();
	private BooleanProperty checkOnly = new SimpleBooleanProperty();
	private StringProperty availableVersion = new SimpleStringProperty();;
	private BooleanProperty needsUpdating = new SimpleBooleanProperty();

	protected UpdateableJajaApp<? extends UpdateableJajaFXApp<?, ?>, ?> context;

	protected AbstractAppUpdateService(UpdateableJajaApp<? extends UpdateableJajaFXApp<?, ?>, ?> context) {
		super(context.getUpdateContext());
		this.context = context; 
		needsUpdating.bind(Bindings.isNotNull(availableVersion));
	}

	@Override
	public ReadOnlyStringProperty availableVersionProperty() {
		return availableVersion;
	}

	@Override
	public ReadOnlyBooleanProperty needsUpdatingProperty() {
		return needsUpdating;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public boolean isUpdating() {
		return updatingProperty().get();
	}

	@Override
	protected void setAvailableVersion(String availableVersion) {
		super.setAvailableVersion(availableVersion);
		this.availableVersion.set(availableVersion);
	}

	@Override
	protected void setUpdating(boolean updating, boolean checkOnly) {
		super.setUpdating(updating, checkOnly);
		this.updating.set(updating);
		this.checkOnly.set(checkOnly);
	}

	@Override
	public final ReadOnlyBooleanProperty updatingProperty() {
		return updating;
	}

	@Override
	public final ReadOnlyBooleanProperty checkOnlyProperty() {
		return checkOnly;
	}

	@Override
	public final String getAvailableVersion() {
		return availableVersion.get();
	}

	@Override
	public final boolean isUpdatesEnabled() {
		return "false".equals(System.getProperty("hypersocket.development.noUpdates", "false"));
	}


}
