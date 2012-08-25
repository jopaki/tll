/**
 * The Logic Lab
 * @author jpk
 * Jun 15, 2008
 */
package com.tll.common.data;

import com.tll.IMarshalable;
import com.tll.dao.Sorting;

/**
 * RemoteListingDefinition - Definition for server side listings. A unique
 * listing name must be bound to these types.
 * @author jpk
 * @param <S> The search type
 */
public final class RemoteListingDefinition<S extends IMarshalable> implements IMarshalable {

	private S searchCriteria;
	private String[] propKeys;
	private int pageSize;
	private Sorting initialSorting;

	/**
	 * Constructor
	 */
	public RemoteListingDefinition() {
		super();
	}

	/**
	 * Constructor
	 * @param searchCriteria The required search criteria
	 * @param propKeys The optional property keys filter array
	 * @param pageSize The required page size. <code>-1</code> means no paging.
	 * @param initialSorting The required default sorting directive
	 */
	public RemoteListingDefinition(S searchCriteria, String[] propKeys, int pageSize,
			Sorting initialSorting) {
		super();
		this.searchCriteria = searchCriteria;
		this.propKeys = propKeys;
		this.pageSize = pageSize;
		this.initialSorting = initialSorting;
	}

	/**
	 * The max number of elements to show per page.
	 * @return int
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * The properties to include in the listing data. If <code>null</code> all
	 * properties will be included in the listing data.
	 * @return the prop keys array.
	 */
	public String[] getPropKeys() {
		return propKeys;
	}

	/**
	 * @return The search criteria for the listing.
	 */
	public S getSearchCriteria() {
		return searchCriteria;
	}

	/**
	 * @return The default sorting directive.
	 */
	public Sorting getInitialSorting() {
		return initialSorting;
	}
}