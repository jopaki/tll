/**
 * 
 */
package com.tll.client.listing;

import com.tll.client.ui.option.Option;

/**
 * IRowOptionsDelegate - Indicates the ability to provide {@link Option}s for a
 * particular table row and handles row option selection events.
 * @author jpk
 */
public interface IRowOptionsDelegate {

	/**
	 * Provides {@link Option}s for use by a row specific popup Panel.
	 * @param rowIndex The row index of the targeted row
	 * @return Array of {@link Option}s.
	 */
	Option[] getOptions(int rowIndex);

	/**
	 * Handles option events for a single row.
	 * @param optionText The text of the selected option
	 * @param rowIndex The row index of associated with the selected option
	 */
	void handleOptionSelection(String optionText, int rowIndex);
}
