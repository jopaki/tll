package com.tll.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LabelElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.SubmitButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author jpk
 * @since Aug 22, 2012
 */
public class LoginPanel extends Composite implements ClickHandler, ValueChangeHandler<String>, KeyPressHandler {

	private static LoginPanel2UiBinder uiBinder = GWT.create(LoginPanel2UiBinder.class);

	interface LoginPanel2UiBinder extends UiBinder<Widget, LoginPanel> {
	}

	static LoginConstants constants;
	static LoginResources res;

	static {
		constants = GWT.create(LoginConstants.class);
		res = GWT.create(LoginResources.class);
		res.css().ensureInjected();
	}

	static enum Mode {
		/**
		 * Login (default)
		 */
		LOGIN,
		/**
		 * Request to reset password. I.e. Forgot Password.
		 */
		RESET_PASSWORD,
		/**
		 * Valid only when user has reset password, this mode submits new password
		 * to server.
		 */
		CHANGE_PASSWORD,
	}

	Mode mode;

	@UiField DivElement title;
	@UiField DivElement subtitle;
	@UiField DivElement statusMsg;

	@UiField Element trUsername;
	@UiField LabelElement lblUsername;
	@UiField TextBox tbUsername;
	@UiField DivElement lblInvalidUsername;

	@UiField Element trPassword;
	@UiField LabelElement lblPassword;
	@UiField PasswordTextBox tbPassword;
	@UiField DivElement lblInvalidPassword;

	@UiField Element trRmbrMe;
	@UiField CheckBox cbRmbrMe;

	@UiField Element trConfirmPassword;
	@UiField LabelElement lblConfirmPassword;
	@UiField PasswordTextBox tbConfirmPassword;
	@UiField DivElement lblInvalidConfirmPassword;

	@UiField Anchor lnkTgl; // toggles btwn view modes
	@UiField SubmitButton btnSubmit;
	
	@UiField DivElement glass;
	DivElement throbber;

	/**
	 * Constructor
	 * @param resources
	 */
	public LoginPanel() {
		initWidget(uiBinder.createAndBindUi(this));
		tbUsername.getElement().setId(DOM.createUniqueId());
		tbPassword.getElement().setId(DOM.createUniqueId());
		tbConfirmPassword.getElement().setId(DOM.createUniqueId());
		lblUsername.setHtmlFor(tbUsername.getElement().getId());
		lblPassword.setHtmlFor(tbPassword.getElement().getId());
		lblConfirmPassword.setHtmlFor(tbConfirmPassword.getElement().getId());
		
		tbUsername.addValueChangeHandler(this);
		tbPassword.addValueChangeHandler(this);
		tbConfirmPassword.addValueChangeHandler(this);
		
		tbUsername.addKeyPressHandler(this);
		tbPassword.addKeyPressHandler(this);
		tbConfirmPassword.addKeyPressHandler(this);
		
		setMode(Mode.LOGIN);
	}

	void setMode(Mode mode) {
		tbUsername.setValue(null);
		tbPassword.setValue(null);
		tbConfirmPassword.setValue(null);
		lblInvalidUsername.getStyle().setVisibility(Visibility.HIDDEN);
		lblInvalidPassword.getStyle().setVisibility(Visibility.HIDDEN);
		lblInvalidConfirmPassword.getStyle().setVisibility(Visibility.HIDDEN);
		statusMsg.setInnerText("");
		statusMsg.getStyle().setDisplay(Display.NONE);
		final Focusable toFocus;
		switch(mode) {
			default:
			case LOGIN:
				title.setInnerText(constants.loginTitle());
				subtitle.setInnerText(constants.loginSubtitle());
				trUsername.getStyle().clearDisplay();
				lblUsername.setInnerText(constants.labelUsername());
				lblPassword.setInnerText(constants.labelPassword());
				lblPassword.getStyle().clearDisplay();
				tbPassword.setVisible(true);
				trPassword.getStyle().clearDisplay();
				trConfirmPassword.getStyle().setDisplay(Display.NONE);
				trRmbrMe.getStyle().clearDisplay();
				lnkTgl.setVisible(true);
				lnkTgl.setTitle(constants.toResetPasswordText());
				lnkTgl.setText(constants.toResetPasswordText());
				btnSubmit.setText(constants.buttonLoginText());
				toFocus = tbUsername;
				break;
			case RESET_PASSWORD:
				title.setInnerText(constants.resetPasswordTitle());
				subtitle.setInnerText(constants.resetPasswordSubtitle());
				trUsername.getStyle().clearDisplay();
				lblUsername.setInnerText(constants.labelUsernameResetPassword());
				tbUsername.setName(constants.resetPasswordEmailFormName());
				trPassword.getStyle().setDisplay(Display.NONE);
				trConfirmPassword.getStyle().setDisplay(Display.NONE);
				trRmbrMe.getStyle().setDisplay(Display.NONE);
				lnkTgl.setVisible(true);
				lnkTgl.setTitle(constants.toLoginText());
				lnkTgl.setText(constants.toLoginText());
				btnSubmit.setText(constants.buttonResetPasswordText());
				toFocus = tbUsername;
				break;
			case CHANGE_PASSWORD:
				title.setInnerText(constants.passwordResetTitle());
				subtitle.setInnerText(constants.passwordResetSubtitle());
				trUsername.getStyle().setDisplay(Display.NONE);
				tbUsername.setText(""); // clear out
				lblPassword.setInnerText(constants.labelNewPassword());
				tbPassword.setText("");
				tbPassword.setName(constants.newPasswordFormName());
				tbConfirmPassword.setText("");
				trConfirmPassword.getStyle().clearDisplay();
				trPassword.getStyle().clearDisplay();
				trRmbrMe.getStyle().setDisplay(Display.NONE);
				btnSubmit.setText(constants.buttonSubmitNewPasswordText());
				lnkTgl.setVisible(false);
				toFocus = tbPassword;
				break;
		}
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				toFocus.setFocus(true);
			}
		});
		this.mode = mode;
	}
	
	private void submit() {
		if(!validate()) return;
		String path =
				mode == Mode.LOGIN ? constants.pathLogin() : mode == Mode.RESET_PASSWORD ? constants.pathResetPassword()
						: constants.pathChangePassword();
		String url = GWT.getModuleBaseURL() + path;
		RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, URL.encode(url));
		StringBuilder sb = new StringBuilder();
		if(mode == Mode.LOGIN || mode == Mode.RESET_PASSWORD) {
			sb.append('&');
			sb.append(mode == Mode.LOGIN ? constants.usernameFormName() : constants.resetPasswordEmailFormName());
			sb.append('=');
			sb.append(tbUsername.getValue());
		}
		if(mode == Mode.LOGIN || mode == Mode.CHANGE_PASSWORD) {
			sb.append('&');
			sb.append(constants.passwordFormName());
			sb.append('=');
			sb.append(tbPassword.getValue());
		}
		if(mode == Mode.LOGIN) {
			sb.append('&');
			sb.append(constants.cbRememberMeFormName());
			sb.append('=');
			sb.append(cbRmbrMe.getValue() == Boolean.TRUE ? "1" : "0");
		}
		if(mode == Mode.CHANGE_PASSWORD) {
			sb.append('&');
			sb.append(constants.confirmNewPasswordFormName());
			sb.append('=');
			sb.append(tbConfirmPassword.getValue());
		}
		String requestData = URL.encode(sb.substring(1));
		String sto = constants.xhrTimeoutMillis();
		if(sto != null) {
			int to;
			try {
				to = Integer.parseInt(sto);
			}
			catch(Exception e) {
				to = 0;
			}
			if(to > 0) builder.setTimeoutMillis(to);
		}
		builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
		// NOTE: Content-Length automatically set
		
		if(throbber == null) {
			throbber = Document.get().createDivElement();
			throbber.setClassName(res.css().throbber());
		}
		title.appendChild(throbber);
		throbber.getStyle().clearDisplay();
	
		// wait till current event loop completes to get accurate dimensions
		// due to onvaluechanged event being on the stack
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				int left = getAbsoluteLeft();
				int top = getAbsoluteTop();
				int width = getOffsetWidth();
				int height = getOffsetHeight();
				Style style = glass.getStyle();
				style.clearDisplay();
				style.setTop(top, Unit.PX);
				style.setLeft(left, Unit.PX);
				style.setWidth(width, Unit.PX);
				style.setHeight(height, Unit.PX);
			}
		});

		try {
			builder.sendRequest(requestData, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
					// Couldn't connect to server (could be timeout, SOP violation, etc.)
					glass.getStyle().setDisplay(Display.NONE);
					throbber.getStyle().setDisplay(Display.NONE);
					subtitle.setInnerText(constants.connectionErrorMsg());
				}

				public void onResponseReceived(Request request, Response response) {
					glass.getStyle().setDisplay(Display.NONE);
					throbber.getStyle().setDisplay(Display.NONE);
					if(200 == response.getStatusCode()) {
						switch(mode) {
							case LOGIN:
								Window.Location.replace(constants.pathLoginSuccess());
								break;
							case RESET_PASSWORD:
								setMode(Mode.CHANGE_PASSWORD);
								break;
							case CHANGE_PASSWORD:
								setMode(Mode.LOGIN);
								break;
						}
					} else if(401 == response.getStatusCode()) {
						if(mode == Mode.LOGIN) {
							// invalid credentials
							statusMsg.setInnerText(constants.resetPasswordInvalidCredentialsMsg());
						} else if(mode == Mode.RESET_PASSWORD) {
							statusMsg.setInnerText(constants.resetPasswordUnrecognizedEmail());
						} else {
							statusMsg.setInnerText(response.getStatusText());
						}
						tbUsername.setFocus(true);
						statusMsg.addClassName(res.css().error());
						statusMsg.getStyle().clearDisplay();
					} else {
						// Handle the error. Can get the status text from
						statusMsg.setInnerText(response.getStatusText());
						statusMsg.addClassName(res.css().error());
						statusMsg.getStyle().clearDisplay();
					}
				}
			});
		}
		catch(RequestException e) {
			// Couldn't connect to server
			statusMsg.setInnerText(constants.connectionErrorMsg());
			statusMsg.getStyle().clearDisplay();
		}
	}

	@Override
	@UiHandler(value = {
		"lnkTgl", "btnSubmit" })
	public void onClick(ClickEvent event) {
		statusMsg.setInnerText(""); // clear out
		Object src = event.getSource();
		if(src == lnkTgl) {
			// toggle view model
			setMode(mode == Mode.LOGIN ? Mode.RESET_PASSWORD : Mode.LOGIN);
		} else if(src == btnSubmit) {
			submit();
		}
	}

	@Override
	public void onValueChange(ValueChangeEvent<String> event) {
		// clear any validation errors in ui
		lblInvalidUsername.getStyle().setVisibility(Visibility.HIDDEN);
		lblInvalidPassword.getStyle().setVisibility(Visibility.HIDDEN);
		lblInvalidConfirmPassword.getStyle().setVisibility(Visibility.HIDDEN);
		statusMsg.getStyle().setDisplay(Display.NONE);
		statusMsg.setClassName(res.css().statusMsg());
	}

	private boolean validate() {
		boolean ok = true;
		if((mode == Mode.LOGIN || mode == Mode.RESET_PASSWORD) && tbUsername.getText().length() == 0) {
			lblInvalidUsername.setInnerText(mode == Mode.LOGIN ? constants.labelInvalidUsername() : constants.labelInvalidEmail());
			lblInvalidUsername.getStyle().setVisibility(Visibility.VISIBLE);
			ok = false;
		} else
			lblInvalidUsername.getStyle().setVisibility(Visibility.HIDDEN);

		if((mode == Mode.LOGIN || mode == Mode.CHANGE_PASSWORD) && tbPassword.getText().length() == 0) {
			lblInvalidPassword.getStyle().setVisibility(Visibility.VISIBLE);
			ok = false;
		} else
			lblInvalidPassword.getStyle().setVisibility(Visibility.HIDDEN);

		if(mode == Mode.CHANGE_PASSWORD) {
			if(tbConfirmPassword.getText().length() == 0 || !tbConfirmPassword.getText().equals(tbPassword.getText())) {
				lblInvalidConfirmPassword.getStyle().setVisibility(Visibility.VISIBLE);
				ok = false;
			} else
				lblInvalidConfirmPassword.getStyle().setVisibility(Visibility.HIDDEN);
		}
		
		return ok;
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		if(event.getCharCode() == KeyCodes.KEY_ENTER) {
			submit();
		}
	}

}
