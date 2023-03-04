package com.sshtools.jajafx;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

public class FXUtil {
	
	public static void load(Object controller) {
		var clazz = controller.getClass();
		var loader = new FXMLLoader(clazz.getResource(clazz.getSimpleName() + ".fxml"));
		loader.setController(controller);
		loader.setRoot(controller);
		try {
			loader.setResources(ResourceBundle.getBundle(clazz.getName()));
		}
		catch(Exception e) {
		}
		try {
			loader.load();
		}/* catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		} */
		catch(Throwable t) {
			// Sinking this exception because of apparent bug in SceneBuiklder
			t.printStackTrace();
		}
	}

	public static Optional<File> chooseFileAndRememeber(Preferences preferences, FileChooser chooser,
			Path defaultDirectory, String key, Window ownerWindow) {
		var initialDirKey = key + ".initialDirectory";
		chooser.setInitialDirectory(new File(preferences.get(initialDirKey, defaultDirectory.toString())));
		var initialFileKey = key + ".initialFile";
		var initialFile = preferences.get(initialFileKey, "");
		chooser.setInitialFileName(initialFile);
		var file = chooser.showOpenDialog(ownerWindow);
		if (file != null) {
			preferences.put(initialDirKey, file.getParentFile().getAbsolutePath());
			preferences.put(initialFileKey, file.getName());
		}
		return Optional.ofNullable(file);
	}

	public static void maybeQueue(Runnable r) {
		if (Platform.isFxApplicationThread())
			r.run();
		else
			Platform.runLater(r);
	}

	public static void spin(Node node, boolean animate) {
		var rotate = (RotateTransition) node.getProperties().get(RotateTransition.class.getName());
		if (animate && rotate == null) {
			rotate = new RotateTransition(Duration.seconds(1));
			rotate.setByAngle(360);
			rotate.setCycleCount(RotateTransition.INDEFINITE);
			rotate.setNode(node);
			rotate.play();
		} else if (!animate && rotate != null) {
			rotate.stop();
			rotate = null;
		}
	}

	public static <O> ObservableList<O> compoundList(ObservableList<O> l1, ObservableList<O> l2) {
		ObservableList<O> l = FXCollections.observableArrayList();
		l.addAll(l1);
		l1.addListener(new ListChangeListener<O>() {
			@Override
			public void onChanged(Change<? extends O> c) {
				while (c.next()) {
					for (var o : c.getAddedSubList()) {
						l.add(o);
					}
					for (var o : c.getRemoved()) {
						l.remove(o);
					}
				}
			}
		});
		l.addAll(l2);
		l2.addListener(new ListChangeListener<O>() {
			@Override
			public void onChanged(Change<? extends O> c) {
				while (c.next()) {
					for (var o : c.getAddedSubList()) {
						l.add(o);
					}
					for (var o : c.getRemoved()) {
						l.remove(o);
					}
				}
			}
		});
		return FXCollections.unmodifiableObservableList(l);
	}

	public static void makeIntegerTextField(int minValue, int maxValue, TextInputControl tc) {

		var val = minValue;
		var prompt = tc.getPromptText();
		if (tc.getText().equals("")) {
			try {
				val = Integer.parseInt(prompt);
			} catch (Exception e) {
			}
		} else {
			try {
				val = Integer.parseInt(tc.getText());
			} catch (Exception e) {
				tc.setText(String.valueOf(val));
			}
		}
		var value = new SimpleIntegerProperty(val);

		// make sure the value property is clamped to the required range
		// and update the field's text to be in sync with the value.
		value.addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				if (newValue == null) {
					tc.setText("");
				} else {
					if (newValue.intValue() < minValue) {
						value.setValue(minValue);
						return;
					}

					if (newValue.intValue() > maxValue) {
						value.setValue(maxValue);
						return;
					}

					if (newValue.intValue() == 0
							&& (tc.textProperty().get() == null || "".equals(tc.textProperty().get()))) {
						// no action required, text property is already blank, we don't need to set it
						// to 0.
					} else {
						tc.setText(newValue.toString());
					}
				}
			}
		});

		// restrict key input to numerals.
		tc.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyEvent) {
				if (minValue < 0) {
					if (!"-0123456789".contains(keyEvent.getCharacter())) {
						keyEvent.consume();
					}
				} else {
					if (!"0123456789".contains(keyEvent.getCharacter())) {
						keyEvent.consume();
					}
				}
			}
		});

		// ensure any entered values lie inside the required range.
		tc.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
				if (newValue == null || "".equals(newValue) || (minValue < 0 && "-".equals(newValue))) {
					value.setValue(0);
					return;
				}

				final int intValue = Integer.parseInt(newValue);

				if (minValue > intValue || intValue > maxValue) {
					tc.textProperty().setValue(oldValue);
				}

				value.set(Integer.parseInt(tc.textProperty().get()));
			}
		});
	}

	public static int intTextfieldValue(TextInputControl text) {
		try {
			return Integer.parseInt(text.getText());
		} catch (Exception e) {
			try {
				return Integer.parseInt(text.getPromptText());
			} catch (Exception e2) {
				return 0;
			}
		}
	}

	public static String textOrPrompt(TextInputControl text) {
		var txt = text.getText();
		if (txt.equals(""))
			return text.getPromptText();
		else
			return txt;
	}

	public static List<String> addIfNotAdded(List<String> target, String... source) {
		for (var s : source) {
			if (!target.contains(s))
				target.add(s);
		}
		return target;
	}

	public static Optional<String> optionalText(TextInputControl text) {
		var txt = text.getText();
		return txt.equals("") ? Optional.empty() : Optional.of(txt);
	}

	public static Optional<Path> emptyPathIfBlankString(String pathText) {
		return pathText == null || pathText.equals("") ? Optional.empty() : Optional.of(Path.of(pathText));
	}

}
