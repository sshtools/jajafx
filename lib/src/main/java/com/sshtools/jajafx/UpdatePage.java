package com.sshtools.jajafx;

import static com.sshtools.jajafx.FXUtil.maybeQueue;
import static com.sshtools.jajafx.FXUtil.spin;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UpdatePage<A extends JajaFXApp<? extends JajaApp<A>>> extends AbstractTile<A> {

	public final static ResourceBundle RESOURCES = ResourceBundle.getBundle(UpdatePage.class.getName());

	@FXML
	Label available;
	@FXML
	Label result;
	@FXML
	Button update;
	@FXML
	FontIcon spinner;

	private AppUpdateService updateService;
	private AtomicInteger seconds = new AtomicInteger();
	private Runnable onRemindMeTomorrow;
	private ScheduledFuture<?> task;

	@Override
	protected void onConfigure() {
		result.managedProperty().bind(result.visibleProperty());

		updateService = getContext().getContainer().getUpdateService();

		var updating = updateService.updatingProperty();
		updating.addListener((c, o, n) -> Platform.runLater(() -> {
			spin(spinner, n);
			updateRemaining();
			if (n)
				result.setVisible(false);
		}));
		spinner.visibleProperty().bind(updating);
		spin(spinner, updating.get());
	}

	@Override
	public void shown() {
		seconds.set(10);
		available.setText(MessageFormat.format(RESOURCES.getString("available"), updateService.getAvailableVersion()));

		result.setVisible(true);
		result.setText("");
		result.getStyleClass().clear();

		task = getContext().getContainer().getScheduler().scheduleAtFixedRate(() -> {
			if (seconds.decrementAndGet() == 0) {
				task.cancel(false);
				update(null);
			}
			Platform.runLater(() -> updateRemaining());
		}, 1, 1, TimeUnit.SECONDS);
		updateRemaining();
	}


	public void onRemindMeTomorrow(Runnable onRemindMeTomorrow) {
		this.onRemindMeTomorrow = onRemindMeTomorrow;
	}

	@Override
	public void hidden() {
		task.cancel(false);
	}

	@FXML
	private void update(ActionEvent evt) {
		getContext().getContainer().update((e) -> {
			maybeQueue(() -> {
				result.setVisible(true);
				result.setText(e.getMessage());
				result.getStyleClass().setAll("text-danger");
			});
		});
	}

	@FXML
	private void remindMeTomorrow(ActionEvent evt) {
		updateService.deferUpdate();
		task.cancel(false);
		var tiles = getTiles();
		if(tiles != null)
			tiles.remove(this);
		if(onRemindMeTomorrow != null) 
			onRemindMeTomorrow.run();
	}

	private void updateRemaining() {
		if (updateService.isUpdating())
			update.setText(MessageFormat.format(RESOURCES.getString("updating"), seconds.get()));
		else
			update.setText(MessageFormat.format(RESOURCES.getString("update"), seconds.get()));
	}

}
