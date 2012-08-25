package com.tll.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.tll.client.ui.view.RecentViewsPanel;
import com.tll.client.ui.view.ViewPathPanel;
import com.tll.client.ui.view.ViewToolbar;
import com.tll.client.view.ShowViewRequest;
import com.tll.client.view.ViewAInit;
import com.tll.client.view.ViewBInit;
import com.tll.client.view.ViewC;
import com.tll.client.view.ViewD;
import com.tll.client.view.ViewE;
import com.tll.client.view.ViewManager;
import com.tll.client.view.ViewOptions;

/**
 * UI Tests - GWT module for the sole purpose of verifying the DOM/Style of
 * compiled GWT code.
 */
public final class UITests extends AbstractUITest {

	@Override
	protected String getTestSubjectName() {
		return "client-mvc module";
	}

	@Override
	protected UITestCase[] getTestCases() {
		return new UITestCase[] {
			new ViewToolbarTest(), new MvcTest() };
	}

	/**
	 * ViewToolbarTest - Tests the styling and functionality of the view toolbar.
	 * @author jpk
	 */
	static final class ViewToolbarTest extends DefaultUITestCase {

		ViewToolbar vt;

		/**
		 * Constructor
		 */
		public ViewToolbarTest() {
			super("ViewToolbar test", "Tests the ViewToolbar styling and functionality");
		}

		@Override
		protected Widget getContext() {
			return vt;
		}

		@Override
		protected void init() {
			final ViewOptions vo = new ViewOptions(true, true, true, true, false, false);
			vt = new ViewToolbar("Testing", vo, new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					Window.alert("Click");
				}
			});

			// add some view option buttons
			vt.addViewOpButton(new PushButton("Option 1"), "Option 1");
			vt.addViewOpButton(new PushButton("Option 2"), "Option 2");
		}

		@Override
		protected void teardown() {
			vt.removeFromParent();
			vt = null;
		}
	} // ViewToolbarTest

	/**
	 * MvcTest - tests the rendering of fields and their value change
	 * event handling for all defined field widget types.
	 * @author jpk
	 */
	static final class MvcTest extends DefaultUITestCase {

		VerticalPanel context;
		FlowPanel viewContainer;
		String dyn;

		/**
		 * Constructor
		 */
		public MvcTest() {
			super("MVC Test", "Tests the basic MVC operation");
		}

		@Override
		protected void init() {

			context = new VerticalPanel();
			context.setBorderWidth(2);
			context.setSpacing(4);
			context.add(new ViewPathPanel(4));
			context.add(new RecentViewsPanel(3));

			viewContainer = new FlowPanel();
			viewContainer.add(new Label("This is line 1"));
			viewContainer.add(new Label("This is line 2"));
			viewContainer.add(new Label("This is line 3"));
			context.add(viewContainer);

			ViewManager.initialize(viewContainer, 3);

			dyn = "dyn";
		}

		@Override
		protected void teardown() {
			context.removeFromParent();
			context = null;
			viewContainer = null;
			ViewManager.shutdown();
		}

		@Override
		protected Widget getContext() {
			return context;
		}

		@Override
		protected Button[] getTestActions() {
			return new Button[] {
				new Button("View A", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ViewManager.get().dispatch(new ShowViewRequest(new ViewAInit(), null));
					}
				}),
				new Button("View B", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ViewManager.get().dispatch(new ShowViewRequest(new ViewBInit(dyn), null));
					}
				}),
				new Button("View C", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ViewManager.get().dispatch(new ShowViewRequest(ViewC.klas));
					}
				}),
				new Button("View D", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ViewManager.get().dispatch(new ShowViewRequest(ViewD.klas));
					}
				}),
				new Button("View E", new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {
						ViewManager.get().dispatch(new ShowViewRequest(ViewE.klas));
					}
				})
			};
		}
	} // MvcTest

}
