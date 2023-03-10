package com.sshtools.jajafx;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sshtools.sequins.Prompter;

import javafx.application.Platform;

public class AppPrompter<C extends JajaFXApp<?>> implements Prompter {
	
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

	@Override
	public char[] password(PromptContext context, String fmt, Object... args) {
		var sem = new Semaphore(1);
		var buf = new StringBuilder();
		try {
			sem.acquire();
			Platform.runLater(() -> {
				var passwordPage = wiz.popup(PasswordPage.class);
				var txt = MessageFormat.format(fmt, args);
				passwordPage.title.setText(txt);
				passwordPage.text.setText(MessageFormat.format(resources.getString("passwordDialog.prompt"), context.use().orElse("")));
				passwordPage.ok.setOnAction((e) -> {
					buf.append(passwordPage.password.getText());
					wiz.remove(passwordPage);
					sem.release();
				});
				passwordPage.cancel.setOnAction((e) -> {
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
				var promptPage = wiz.popup(PromptPage.class);
				var txt = MessageFormat.format(fmt, args);
				promptPage.text.setText(txt);
				promptPage.text.setOnKeyTyped(f -> {
					if(f.getCharacter().equals("\r")) {
						buf.append(promptPage.prompt.getText());
						wiz.remove(promptPage);
						sem.release();
					}
				});
				promptPage.submit.setOnAction((e) -> {
					buf.append(promptPage.prompt.getText());
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
				var yesNoPage = wiz.popup(YesNoPage.class);

				yesNoPage.yes.setDefaultButton(preferYes);
				yesNoPage.no.setDefaultButton(!preferYes);
				yesNoPage.yes.setCancelButton(!preferYes);
				yesNoPage.no.setCancelButton(preferYes);
				
				var txt = MessageFormat.format(fmt, args);
				yesNoPage.text.setText(txt);
				yesNoPage.yes.setOnAction((e) -> {
					answer.set(true);
					wiz.remove(yesNoPage);
					sem.release();
				});
				yesNoPage.no.setOnAction((e) -> {
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
