/**
 * The Logic Lab
 * @author jpk Jan 3, 2008
 */
package com.tll.client.ui.view;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tll.client.ui.HtmlListPanel;
import com.tll.client.view.IViewChangeHandler;
import com.tll.client.view.ViewChangeEvent;
import com.tll.client.view.ViewManager;
import com.tll.client.view.ViewRef;

/**
 * RecentViewsPanel - Displays view links vertically that are currently in the
 * view cache that are NOT in the popped state.
 * @author jpk
 */
public final class RecentViewsPanel extends Composite implements IViewChangeHandler {

	/**
	 * Styles
	 * @author jpk
	 */
	protected static class Styles {

		/**
		 * Style applied to the widget containing the recent views listing.
		 */
		public static final String RECENT_VIEWS = "recentviews";
	}

	private final int capacity;

	/**
	 * The topmost (parent) ulPanel of this {@link Widget}.
	 */
	private final FlowPanel container = new FlowPanel();

	/**
	 * AbstractView history links.
	 */
	private final HtmlListPanel ulPanel = new HtmlListPanel(false);

	/**
	 * Constructor
	 * @param capacity the max number of view links to display
	 */
	public RecentViewsPanel(final int capacity) {
		this.capacity = capacity;
		container.setStyleName(Styles.RECENT_VIEWS);
		container.add(ulPanel);
		initWidget(container);
	}

	@Override
	public void onViewChange(final ViewChangeEvent event) {
		// NOTE: rebuild the ulPanel (it's MUCH easier than trying to remove/insert)
		ulPanel.clear();

		final ViewRef[] refs = ViewManager.get().getViewRefs(capacity, false);
		final int count = refs.length;

		// re-build the recent view list
		// NOTE: ending at 1 before last element (skip the current view)
		for(int i = 0; i < count - 1; i++) {
			ulPanel.append(new ViewLink(refs[i]));
		}
	}
	
	private HandlerRegistration vch;

	@Override
	protected void onLoad() {
		super.onLoad();
		if(vch != null) throw new IllegalStateException();
		vch = ViewManager.get().addViewChangeHandler(this);
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		if(vch != null) {
			vch.removeHandler();
			vch = null;
		}
	}

}