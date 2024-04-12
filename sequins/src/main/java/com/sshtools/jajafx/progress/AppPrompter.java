package com.sshtools.jajafx.progress;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sshtools.jajafx.JajaFXApp;
import com.sshtools.jajafx.Tiles;
import com.sshtools.sequins.Prompter;

import javafx.application.Platform;

public class AppPrompter<C extends JajaFXApp<?, ?>> implements Prompter {
	
	private final Tiles<C> wiz;
	private final ResourceBundle resources;

	public AppPrompter(Tiles<C> wizard, ResourceBundle resources) {
		this.wiz = wizard;
		this.resources = resources;
	}

	@Override
	public char[] password() {
		return password("Password");
	}

	public char[] password(String fmt, Object... args) {
		return password(PromptContext.empty(), fmt, args);
	}

	@SuppressWarnings("unchecked")
	@Override
	public char[] password(PromptContext context, String fmt, Object... args) {
		var sem = new Semaphore(1);
		var buf = new StringBuilder();
		try {
			sem.acquire();
			Platform.runLater(() -> {
				var passwordPage = wiz.popup(PasswordPage.class);
				var txt = MessageFormat.format(fmt, args);
				passwordPage.titleText().set(txt);
				passwordPage.textText().set(MessageFormat.format(resources.getString("passwordDialog.prompt"), context.use().orElse("")));
				passwordPage.onConfirm(e -> {
					buf.append(passwordPage.password().get());
					wiz.remove(passwordPage);
					sem.release();
				});
				passwordPage.onCancel((e) -> {
					wiz.remove(passwordPage);
					sem.release();
				});
			});
			sem.acquire();
		} catch (InterruptedException ie) {
			throw new IllegalStateException("Interrupted.", ie);
		} finally {
			sem.release();
		}
		return buf.length() == 0 ? null : buf.toString().toCharArray();
	}

	@Override
	public String prompt() {
		return prompt("");
	}
	@Override
	public String prompt(String fmt, Object... args) {
		return prompt(PromptContext.empty(), fmt, args);
	}

	@Override
	public String prompt(PromptContext context, String fmt, Object... args) {
		var sem = new Semaphore(1);
		var buf = new StringBuilder();
		try {
			sem.acquire();
			Platform.runLater(() -> {
				@SuppressWarnings("unchecked")
				var promptPage = (PromptPage<C>)wiz.popup(PromptPage.class);
				var txt = MessageFormat.format(fmt, args);
				promptPage.text().setText(txt);
				promptPage.text().setOnKeyTyped(f -> {
					if(f.getCharacter().equals("\r")) {
						buf.append(promptPage.prompt().getText());
						wiz.remove(promptPage);
						sem.release();
					}
				});
				promptPage.submit().setOnAction((e) -> {
					buf.append(promptPage.prompt().getText());
					wiz.remove(promptPage);
					sem.release();
				});
			});
			sem.acquire();
		} catch (InterruptedException ie) {
			throw new IllegalStateException("Interrupted.", ie);
		} finally {
			sem.release();
		}
		return buf.length() == 0 ? null : buf.toString();
	}

	@Override
	public boolean noYes() {
		return noYes("");
	}

	@Override
	public boolean noYes(String fmt, Object... args) {
		return yesOrNo(PromptContext.empty(), false, fmt, args);
	}

	@Override
	public boolean noYes(PromptContext context, String fmt, Object... args) {
		return yesOrNo(context, false, fmt, args);
	}

	@Override
	public boolean yesNo() {
		return yesNo("");
	}

	@Override
	public boolean yesNo(String fmt, Object... args) {
		return yesOrNo(PromptContext.empty(), true, fmt, args);
	}

	@Override
	public boolean yesNo(PromptContext context, String fmt, Object... args) {
		return yesOrNo(context, true, fmt, args);
	}

	private boolean yesOrNo(PromptContext context, boolean preferYes, String fmt, Object... args) {
		var sem = new Semaphore(1);
		var answer = new AtomicBoolean();
		try {
			sem.acquire();
			Platform.runLater(() -> {
				@SuppressWarnings("unchecked")
				var yesNoPage = (YesNoPage<C>)wiz.popup(YesNoPage.class);
				yesNoPage.preferYes();
				
				var txt = MessageFormat.format(fmt, args);
				yesNoPage.textText().set(txt);
				yesNoPage.onYes((e) -> {
					answer.set(true);
					wiz.remove(yesNoPage);
					sem.release();
				});
				yesNoPage.onNo((e) -> {
					answer.set(false);
					wiz.remove(yesNoPage);
					sem.release();
				});
			});
			sem.acquire();
		} catch (InterruptedException ie) {
			throw new IllegalStateException("Interrupted.", ie);
		} finally {
			sem.release();
		}
		return answer.get();
	}
}
