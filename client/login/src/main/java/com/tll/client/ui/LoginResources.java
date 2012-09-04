package com.tll.client.ui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * @author jpk
 */
public interface LoginResources extends ClientBundle {

	public interface Css extends CssResource {

		String loginPanel();
		
		String outerTbl();

		String title();
		
		String subtitle();

		String statusMsg();
		
		String error();
		
		String info();
		
		String invalid();
		
		String logoPane();
		
		String formPane();
		
		String logo();
		
		String cbRmbrMe();
		
		String glass();
		
		String throbber();
		
		String submitButton();
		
		String btnRow();
		
		String lnkTgl();
	}
	
	@Source("logo.png")
	@ImageOptions(repeatStyle=RepeatStyle.None, width=150)
	ImageResource imgLogo();

	@Source("Login.css")
	Css css();

}
