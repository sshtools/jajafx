package com.sshtools.jajafx.updateable;

import static com.sshtools.jajafx.FXUtil.maybeQueue;
import static com.sshtools.jajafx.FXUtil.spin;
import static javafx.application.Platform.runLater;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import org.kordamp.ikonli.javafx.FontIcon;

import com.sshtools.jajafx.AboutPage;
import com.sshtools.jajafx.JajaApp;
import com.sshtools.jajafx.JajaFXApp;
import com.sshtools.jajafx.PageTransition;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class UpdateableAboutPage<A extends UpdateableJajaFXApp<? extends UpdateableJajaApp<A, ?>, ?>> extends AboutPage<A> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(UpdateableAboutPage.class.getName());

	@FXML
	Button checkForUpdates;
	@FXML
	Label result;
	@FXML
	FontIcon spinner;

	private AppUpdateService updateService;

	@Override
	protected void onConfigure() {
		super.onConfigure();
		updateService = getContext().getContainer().getUpdateService();

		var updating = updateService.updatingProperty();

		updating.addListener((c, o, n) -> {
			runLater(() -> {
				spin(spinner, n);
				if (n)
					result.setVisible(false);
			});
		});
		checkForUpdates.disableProperty().bind(updating);
		spinner.visibleProperty().bind(updating);

		spin(spinner, updating.get());
	}

	@SuppressWarnings("unchecked")
	@FXML
	private void checkForUpdates(ActionEvent evt) {
		getContext().getContainer().updateCheck((res) -> {
			maybeQueue(() -> {
				if (res) {
					getTiles().popup(UpdatePage.class, PageTransition.FROM_RIGHT);
				} else {
					result.setVisible(true);
					result.setText(RESOURCES.getString("noUpdates"));
					result.getStyleClass().setAll("text-info");
				}
			});
		}, (err) -> {
			maybeQueue(() -> {
				result.setVisible(true);
				result.setText(err.getMessage());
				result.getStyleClass().setAll("text-danger");
			});
		});
	}

}
