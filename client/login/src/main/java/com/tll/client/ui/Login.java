package com.tll.client.ui;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;
import com.tll.client.ui.LoginPanel;

/**
 * @author jpk
 * @since Aug 25, 2012
 */
public class Login implements EntryPoint {

	@Override
	public void onModuleLoad() {
		LoginPanel lp = new LoginPanel();
		RootPanel.get().add(lp);
	}

}
