/**
 * The Logic Lab
 * @author jpk Jan 25, 2008
 */
package com.tll.client.ui.view;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.tll.client.ui.SimpleHyperLink;
import com.tll.client.view.IViewInitializer;
import com.tll.client.view.ShowViewRequest;
import com.tll.client.view.ViewManager;
import com.tll.client.view.ViewRef;

/**
 * ViewLink - Link that delegates a show view request to the mvc dispatcher.
 * @author jpk
 */
public final class ViewLink extends SimpleHyperLink {

	/**
	 * The view initializer.
	 */
	private IViewInitializer init;

	/**
	 * Constructor
	 */
	public ViewLink() {
		super();
		addClickHandler(new ClickHandler() {

			@SuppressWarnings("synthetic-access")
			@Override
			public void onClick(final ClickEvent event) {
				if(ViewLink.this.init == null) throw new IllegalStateException();
				ViewManager.get().dispatch(new ShowViewRequest(ViewLink.this.init, null));
			}
		});
	}

	/**
	 * Constructor
	 * @param shortViewName
	 * @param longViewName
	 */
	public ViewLink(final String shortViewName, final String longViewName) {
		this(shortViewName, longViewName, null);
	}

	/**
	 * Constructor
	 * @param shortViewName
	 * @param longViewName
	 * @param init
	 */
	public ViewLink(final String shortViewName, final String longViewName, final IViewInitializer init) {
		this();
		setViewNames(shortViewName, longViewName);
		setViewInitializer(init);
	}

	/**
	 * Constructor
	 * @param viewRef
	 */
	public ViewLink(final ViewRef viewRef) {
		this(viewRef.getShortViewName(), viewRef.getLongViewName(), viewRef.getViewInitializer());
	}

	/**
	 * Sets the view initializer.
	 * @param init
	 */
	public void setViewInitializer(final IViewInitializer init) {
		this.init = init;
	}

	/**
	 * Sets both the short and long view names.
	 * @param shortName
	 * @param longName
	 */
	public void setViewNames(final String shortName, final String longName) {
		setText(shortName);
		setTitle(longName);
	}
}
