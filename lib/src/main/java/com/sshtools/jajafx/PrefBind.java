package com.sshtools.jajafx;

import java.io.Closeable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

/**
 * Binds a limited set of JavaFX controls directly to a {@link Preferences}
 * node. The preference keys are derived from the <code>fx:id</code> on each
 * control. The binding is bi-directional, so a change to the control results in
 * an immediate change to the preference, and an external change to the
 * preference immediately updates the control.
 * <p>
 * The {@link #close()} method must be called when the bindings are finished
 * with, or you can {@link #unbind(Control)} each control individually.
 */
public class PrefBind implements PreferenceChangeListener, Closeable {

	static ScheduledExecutorService EXEC = Executors.newScheduledThreadPool(1);

	private final Preferences preferences;
	private final Map<String, Object> binds = new HashMap<>();

	private Map<Object, ChangeListener<?>> comboChangeListeners = new HashMap<>();
	private Map<Object, ChangeListener<? super String>> stringChangeListeners = new HashMap<>();
	private Map<Object, ChangeListener<? super Boolean>> booleanChangeListeners = new HashMap<>();
	private Map<Object, ChangeListener<? super Number>> numberChangeListeners = new HashMap<>();
	private Map<Object, ChangeListener<? super Toggle>> toggleChangeListeners = new HashMap<>();

	public PrefBind(Preferences preferences) {
		this.preferences = preferences;

		preferences.addPreferenceChangeListener(this);
	}

	public void unbind(Control text) {
		var key = text.getId();
		if (key == null || key.equals(""))
			throw new IllegalArgumentException("Cannot unbind controls without ID this way.");
		unbindImpl(text);
		binds.remove(key);
	}

	public void unbind(String key) {
		if (key == null || key.equals(""))
			throw new IllegalArgumentException("Cannot unbind controls without ID this way.");
		unbindImpl(binds.remove(key));
	}

	public void bind(TextInputControl... fields) {
		for (var k : fields)
			bind(k, k.getId());
	}

	public void bind(IntegerProperty text, String key) {
		var def = text.get();
		checkKey(key);
		binds.put(key, text);
		text.set(preferences.getInt(key, def));
		ChangeListener<? super Number> listener = (c, o, n) -> {
			EXEC.execute(() -> preferences.putInt(key, n.intValue()));
		};
		numberChangeListeners.put(text, listener);
		text.addListener(listener);
	}

	public void bind(TextInputControl text, String key) {
		checkKey(key);
		binds.put(key, text);
		text.setText(preferences.get(key, text.getText()));
		ChangeListener<? super String> listener = (c, o, n) -> {
			EXEC.execute(() -> preferences.put(key, n));
		};
		stringChangeListeners.put(text, listener);
		text.textProperty().addListener(listener);
	}

	@SafeVarargs
	public final <T extends Enum<T>> void bind(Class<T> type, ComboBox<T>... fields) {
		for (var k : fields)
			bind(type, k, k.getId());
	}

	public <T extends Enum<T>> void bind(Class<T> type, ComboBox<T> field, String key) {
		checkKey(key);
		binds.put(key, field);
		var defItem = field.getSelectionModel().getSelectedItem();
		if (defItem == null && field.getItems().size() > 0) {
			defItem = field.getItems().get(field.getItems().size() - 1);
		}
		var prefVal =preferences.get(key, defItem == null ? null : defItem.name());
		var ev = prefVal == null ? null : Enum.valueOf(type, prefVal);
		field.getSelectionModel().select(ev);
		ChangeListener<T> listener = (c, o, n) -> {
			EXEC.execute(() -> preferences.put(key, n.name()));
		};
		field.getProperties().put("type", type);
		comboChangeListeners.put(field, listener);
		field.getSelectionModel().selectedItemProperty().addListener(listener);
	}
	
	public <T> void bind(ComboBox<T> field) {
		bind(field, field.getId());
	}

	public <T> void bind(ComboBox<T> field, String key) {
		checkKey(key);
		binds.put(key, field);
		var defItem = field.getSelectionModel().getSelectedItem();
		if (defItem == null && field.getItems().size() > 0) {
			defItem = field.getItems().get(field.getItems().size() - 1);
		}
		var prefVal =preferences.get(key, defItem == null ? null : field.getConverter().toString(defItem));
		var ev = prefVal == null ? null : field.getConverter().fromString(prefVal);
		field.getSelectionModel().select(ev);
		ChangeListener<T> listener = (c, o, n) -> {
			EXEC.execute(() -> preferences.put(key, field.getConverter().toString(n)));
		};
		comboChangeListeners.put(field, listener);
		field.getSelectionModel().selectedItemProperty().addListener(listener);
	}

	public <N extends Number> void bind(Spinner<N> field, Class<N> clazz) {
		bind(field, field.getId(), clazz);
	}

	@SuppressWarnings("unchecked")
	public <N extends Number> void bind(Spinner<N> field, String key, Class<N> clazz) {
		checkKey(key);
		binds.put(key, field);
		if(clazz.equals(Integer.class)) {
			((IntegerSpinnerValueFactory)field.getValueFactory()).setValue(preferences.getInt(key, field.getValue().intValue()));	
		}
		else if(clazz.equals(Double.class)) {
			((DoubleSpinnerValueFactory)field.getValueFactory()).setValue(preferences.getDouble(key, field.getValue().doubleValue()));	
		}
		else 
			throw new IllegalArgumentException("Unsuported type.");
		ChangeListener<N> listener = (c, o, n) -> {
			if(clazz.equals(Integer.class)) {
				EXEC.execute(() -> preferences.putInt(key, n.intValue()));	
			}
			else if(clazz.equals(Double.class)) {
				EXEC.execute(() -> preferences.putDouble(key, n.doubleValue()));	
			}
			else 
				throw new IllegalArgumentException("Unsuported type.");
		};
		numberChangeListeners.put(field, (ChangeListener<? super Number>) listener);
		field.valueProperty().addListener(listener);
	}

	public void bind(CheckBox... fields) {
		for (var k : fields)
			bind(k, k.getId());
	}

	public void bind(CheckBox cb, String key) {
		checkKey(key);
		binds.put(key, cb);
		cb.setSelected(preferences.getBoolean(key, cb.isSelected()));
		ChangeListener<? super Boolean> listener = (c, o, n) -> {
			EXEC.execute(() -> preferences.putBoolean(key, n));
		};
		booleanChangeListeners.put(cb, listener);
		cb.selectedProperty().addListener(listener);
	}

	public void bind(ToggleGroup cb, String key) {
		checkKey(key);
		binds.put(key, cb);

		String selId = null;
		Map<String, Toggle> map = new HashMap<>();
		for (var toggle : cb.getToggles()) {
			if (toggle instanceof Control) {
				var ctrl = (Control) toggle;
				var id = ctrl.getId();
				if (id == null)
					throw new IllegalArgumentException("All toggles must have an ID.");
				map.put(id, toggle);
				if (toggle.isSelected()) {
					selId = id;
				}
			} else
				throw new IllegalArgumentException("Can only bind toggles that are controls.");
		}
		if (selId == null) {
			selId = map.keySet().iterator().next();
		}
		selId = preferences.get(key, selId);
		var item = map.get(selId);
		if (item != null)
			cb.selectToggle(item);

		ChangeListener<? super Toggle> listener = (c, o, n) -> {
			EXEC.execute(() -> preferences.put(key, ((Control) n).getId()));
		};
		toggleChangeListeners.put(cb, listener);
		cb.selectedToggleProperty().addListener(listener);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void preferenceChange(PreferenceChangeEvent evt) {
		EXEC.execute(() -> {
			var c = binds.get(evt.getKey());
			if (c != null) {
				if (c instanceof TextInputControl) {
					var textField = (TextInputControl) c;
					if (!textField.getText().equals(evt.getNewValue())) {
						Platform.runLater(() -> textField.setText(evt.getNewValue()));
					}
				} else if (c instanceof CheckBox) {
					var cb = (CheckBox) c;
					var n = evt.getNewValue().equals("true");
					if (cb.isSelected() != n) {
						Platform.runLater(() -> cb.setSelected(n));
					}
				} else if (c instanceof ComboBox) {
					@SuppressWarnings("rawtypes")
					var cb = (ComboBox<? extends Enum>) c;
					@SuppressWarnings("rawtypes")
					var et = (Class) cb.getProperties().get("type");
					setComboBox(cb, et, evt.getNewValue());
				}
				else
					throw new UnsupportedOperationException();
			}
		});
	}

	@Override
	public void close() {
		binds.forEach((k, v) -> unbindImpl(v));
		binds.clear();
	}

	private <E extends Enum<E>> void setComboBox(ComboBox<E> cb, Class<E> et, String newVal) {
		var val = Enum.valueOf(et, newVal);
		if (val != cb.getSelectionModel().getSelectedItem()) {
			Platform.runLater(() -> cb.getSelectionModel().select(val));
		}
	}

	private void checkKey(String key) {
		if (key == null || key.equals(""))
			throw new IllegalArgumentException("Key must not be blank.");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void unbindImpl(Object v) {
		if (v instanceof IntegerProperty) {
			((IntegerProperty) v).removeListener(numberChangeListeners.remove(v));
		} else if (v instanceof Spinner) {
			((Spinner) v).valueProperty().removeListener(numberChangeListeners.remove(v));
		} else if (v instanceof TextInputControl) {
			((TextInputControl) v).textProperty().removeListener(stringChangeListeners.remove(v));
		} else if (v instanceof CheckBox) {
			((CheckBox) v).selectedProperty().removeListener(booleanChangeListeners.remove(v));
		} else if (v instanceof ToggleGroup) {
			((ToggleGroup) v).selectedToggleProperty().removeListener(toggleChangeListeners.remove(v));
		} else if (v instanceof ComboBox) {
			((ComboBox) v).getSelectionModel().selectedItemProperty().removeListener(comboChangeListeners.remove(v));
		} else {
			throw new UnsupportedOperationException(
					MessageFormat.format("Binding of type {0}", v.getClass().getName()));
		}
	}
}
