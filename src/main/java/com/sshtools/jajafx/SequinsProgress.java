package com.sshtools.jajafx;

import static com.sshtools.jajafx.FXUtil.maybeQueue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

import com.sshtools.sequins.Progress;
import com.sshtools.sequins.Progress.Level;
import com.sshtools.sequins.RateLimitedProgress;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

public class SequinsProgress extends VBox implements Initializable {
	@FXML
	private VBox history;
	@FXML
	private ProgressBar progress;
	@FXML
	private Button cancel;
	@FXML
	private Optional<BiConsumer<String, Formattable>> logWriter = Optional.empty();

	private ObservableList<ProgressImpl> active = FXCollections
			.synchronizedObservableList(FXCollections.observableArrayList());
	private volatile boolean cancelled;

	public SequinsProgress() {
		var loader = new FXMLLoader(getClass().getResource(SequinsProgress.class.getSimpleName() + ".fxml"));
		loader.setController(this);
		loader.setRoot(this);
		try {
			loader.load();
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		active.addListener((ListChangeListener.Change<? extends ProgressImpl> c) -> {
			if (active.isEmpty()) {
				cancelled = false;
			}
			updateCancelButton();
		});
		cancel.managedProperty().bind(cancel.visibleProperty());
	}

	@FXML
	void cancel() {
		cancelled = true;
		synchronized (active) {
			for (var c : active) {
				c.cancel();
			}
		}
		updateCancelButton();
	}

	public Progress createProgress() {
		return createProgress(null);
	}

	public Progress createProgress(String message, Object... args) {
		var p = new ProgressImpl(message, args);
		p.postConstruct();
		active.add(p);
		return RateLimitedProgress.of(p, 100);
	}

	public void setLogWriter(BiConsumer<String, Formattable> logWriter) {
		this.logWriter = Optional.of(logWriter);
	}

	private void updateCancelButton() {
		if (active.isEmpty()) {
			cancel.setVisible(false);
			cancel.setDisable(false);
		} else {
			cancel.setVisible(true);
			cancel.setDisable(cancelled);
		}
	}

	public static class Formattable {
		Object[] args;
		Optional<Level> level;
		String pattern;

		public Formattable(Optional<Level> level, String pattern, Object... args) {
			super();
			this.pattern = pattern;
			this.args = args;
			this.level = level;
		}

		public Formattable(String pattern, Object... args) {
			this(Optional.empty(), pattern, args);
		}

		public Object[] args() {
			return args;
		}

		public Optional<Level> level() {
			return level;
		}

		public String pattern() {
			return pattern;
		}

		public String toString() {
			return MessageFormat.format(pattern, args);
		}

	}

	class ProgressImpl implements Progress {

		private int indent = 0;
		private Formattable current;
		private Formattable name;
		private boolean newlineNeeded;
		private List<Progress> jobs = new ArrayList<>();

		public ProgressImpl(String name, Object... args) {
			this(1, name, args);
		}

		protected ProgressImpl(int indent, String name, Object... args) {
			this.name = new Formattable(name, args);
			this.indent = indent;
		}

		@Override
		public void cancel() {
			Progress.super.cancel();
		}

		@Override
		public final void close() throws IOException {
			maybeQueue(() -> {
				active.remove(this);
				stopSpinner();
				printNewline();
			});
		}

		public int indent() {
			return indent;
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		public Formattable message() {
			return current;
		}

		@Override
		public final void message(Level level, String message, Object... args) {

			maybeQueue(() -> {
				stopSpinner();
				this.current = new Formattable(Optional.of(level), message, args);
				if (level == Level.TRACE) {
					logMessage(formatMessage(this.current, indent), this.current);
					return;
				} else {
					try {
						printNewline();
						printJob(indent);
					} finally {
						this.current = null;
					}
				}
			});
		}

		public Formattable name() {
			return name;
		}

		@Override
		public final Progress newJob(String name, Object... args) {
			var job = new ProgressImpl(indent + 1, name, args);
			jobs.add(job);
			maybeQueue(() -> {
				stopSpinner();
				clear();
				printNewline();
				job.postConstruct();
			});
			return job;
		}

		@Override
		public final void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
			maybeQueue(() -> {
				if (percent.isPresent()) {
					progress.setProgress((double) percent.get() / 100d);
				} else {
					stopSpinner();
				}

				if (message.isPresent()) {
					this.current = new Formattable(
							this.current == null ? Optional.of(Level.NORMAL) : this.current.level, message.get(), args);
					if (this.current.level.orElse(Level.NORMAL) == Level.TRACE) {
						logMessage(formatMessage(this.current, indent), this.current);
						return;
					} else {
						printJob(indent);
					}
				}
			});
		}

		@Override
		public List<Progress> jobs() {
			return jobs;
		}

		protected void postConstruct() {
			maybeQueue(() -> {
				if (name != null) {
					printJob(indent - 1);
				}
				startSpinner();
			});
		}

		void clear() {
			name = null;
		}

		String formatMessage(Formattable message, int indent) {
			var b = new StringBuilder();
			for (int i = 0; i < indent; i++) {
				b.append("   ");
			}
			if (indent > 1)
				b.append("○ ");
			else if (indent > 0)
				b.append("● ");
			if (message == null) {
				if (name != null) {
					b.append(name);
				}
			} else {
				b.append(message);

			}
			return b.toString();
		}

		void logMessage(String b, Formattable m) {
			logWriter.ifPresent(w -> w.accept(b, m));
		}

		void printJob(int indent) {
			var summary = (Label) history.getChildren().get(history.getChildren().size() - 1);
			newlineNeeded = true;
			var b = formatMessage(current, indent);
			var m = current;
			maybeQueue(() -> {
				var styl = summary.getStyleClass();
				styl.clear();
				if (m == null || m.level.isEmpty()) {
					styl.add("text-accent");
				} else {
					switch (m.level.get()) {
					case INFO:
						styl.add("text-info");
						break;
					case WARNING:
						styl.add("text-warning");
						break;
					case ERROR:
						styl.add("text-danger");
						break;
					case VERBOSE:
						styl.add("text-success");
						break;
					case NORMAL:
						styl.add("text-accent");
						break;
					default:
						break;
					}
				}
				summary.setText(b);
				summary.setTooltip(new Tooltip(b));
			});

			logMessage(b, m);
		}

		void printNewline() {
			if (newlineNeeded) {
				stopSpinner();
				var lbl = new Label();
				lbl.getStyleClass().clear();
				lbl.getStyleClass().add("text-accent");
				var children = history.getChildren();
				children.add(lbl);
				while (children.size() > 10)
					children.remove(0);
				var o = 1d;
				for (int i = children.size() - 1; i >= 0; i--) {
					children.get(i).setOpacity(o);
					o -= 0.1d;
				}
				newlineNeeded = false;
			}

		}

		void startSpinner() {
			progress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
		}

		void stopSpinner() {
			if (progress.getProgress() == ProgressBar.INDETERMINATE_PROGRESS) {
				progress.setProgress(1d);
			}
		}

		String trimAndFillTo(String str, int size, char fillWith) {
			if (str.length() > size)
				str = str.substring(0, size);
			while (str.length() < size) {
				str = str.concat(String.valueOf(fillWith));
			}
			return str;
		}

	}
}
