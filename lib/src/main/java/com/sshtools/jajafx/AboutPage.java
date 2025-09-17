package com.sshtools.jajafx;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class AboutPage<A extends JajaFXApp<? extends JajaApp<A, ?>, ?>> extends AbstractTile<A> {

	final static ResourceBundle RESOURCES = ResourceBundle.getBundle(AboutPage.class.getName());

	@FXML
	Label name;
	@FXML
	Label description;
	@FXML
	Label copyright;
	@FXML
	Label version;

	@Override
	protected void onConfigure() {
		var thisYear = Calendar.getInstance().get(Calendar.YEAR);
		var inceptionYear = JajaApp.getInstance().getInceptionYear();

		version.setText(String.join("\n", JajaApp.getInstance().getCommandSpec().version()));
		copyright.setText(MessageFormat.format(RESOURCES.getString("copyright"),
				inceptionYear == thisYear ? String.valueOf(inceptionYear) : inceptionYear + "-" + thisYear));
		name.setText(JajaApp.getInstance().getAppResources().getString("about.name"));
		description.setText(JajaApp.getInstance().getAppResources().getString("about.description"));

	}

	@FXML
	private void back(ActionEvent evt) {
		getTiles().remove(this);
	}

	@FXML
	private void link(ActionEvent evt) {
		getContext().getHostServices().showDocument(RESOURCES.getString("vendor"));

	}

}
