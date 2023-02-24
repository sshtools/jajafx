package com.sshtools.jajafx;

import static com.sshtools.jajafx.FXUtil.maybeQueue;
import static com.sshtools.jajafx.FXUtil.spin;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import org.kordamp.ikonli.javafx.FontIcon;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AboutPage<A extends JajaFXApp<?>> extends AbstractWizardPage<A> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(AboutPage.class.getName());

	@FXML
	Label name;
	@FXML
	Label description;
	@FXML
	Label copyright;
	@FXML
	Button checkForUpdates;
	@FXML
	Label version;
	@FXML
	Label result;
	@FXML
	FontIcon spinner;

	private UpdateService updateService;

	@Override
	protected void onConfigure() {
		var thisYear = Calendar.getInstance().get(Calendar.YEAR);
		var inceptionYear = JajaApp.getInstance().getInceptionYear();

		version.setText(String.join("\n", JajaApp.getInstance().getCommandSpec().version()));
		copyright.setText(MessageFormat.format(RESOURCES.getString("copyright"),
				inceptionYear == thisYear ? String.valueOf(inceptionYear) : inceptionYear + "-" + thisYear));
		name.setText(JajaApp.getInstance().getAppResources().getString("about.name"));
		description.setText(JajaApp.getInstance().getAppResources().getString("about.description"));

		updateService = getContext().getContainer().getUpdateService();

		var updating = updateService.updatingProperty();

		updating.addListener((c, o, n) -> {
			spin(spinner, n);
			if (n)
				result.setVisible(false);
		});
		checkForUpdates.disableProperty().bind(updating);
		spinner.visibleProperty().bind(updating);

		spin(spinner, updating.get());
	}

	@Override
	public void shown() {
		getWizard().nextVisibleProperty().set(false);
	}

	@Override
	public void hidden() {
		getWizard().nextVisibleProperty().set(true);
	}

	@FXML
	private void checkForUpdates(ActionEvent evt) {
		getContext().getContainer().updateCheck((res) -> {
			maybeQueue(() -> {
				if (res) {
					getWizard().popup(UpdatePage.class);
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

	@FXML
	private void link(ActionEvent evt) {
		getContext().getHostServices().showDocument(RESOURCES.getString("vendor"));

	}

}
