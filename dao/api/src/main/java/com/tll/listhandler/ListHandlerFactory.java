package com.tll.listhandler;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.tll.criteria.Criteria;
import com.tll.criteria.InvalidCriteriaException;
import com.tll.dao.SearchResult;
import com.tll.dao.Sorting;
import com.tll.util.CollectionUtil;

/**
 * Factory for creating concrete {@link IListHandler}s.
 * @author jpk
 */
public final class ListHandlerFactory {

	/**
	 * Creates an {@link InMemoryListHandler} given a {@link Collection} and
	 * {@link Sorting} directive.
	 * @param <T> the collection element type
	 * @param c an arbitrary collection. If the collection isn't a {@link List}, a
	 *        newly created {@link List} is created containing all collection
	 *        elements whose element order is dicated by the collection's
	 *        {@link Iterator}.
	 * @param sorting the sorting directive. May be <code>null</code>.
	 * @return IListHandler instance
	 * @throws ListHandlerException When a sorting related occurs.
	 * @throws IllegalArgumentException When the given collection is
	 *         <code>null</code>
	 */
	public static <T> IListHandler<T> create(Collection<T> c, Sorting sorting) throws ListHandlerException,
	IllegalArgumentException {
		return new InMemoryListHandler<T>(CollectionUtil.listFromCollection(c), sorting);
	}

	/**
	 * Creates a criteria based list handler.
	 * @param <E>
	 * @param criteria
	 * @param sorting
	 * @param type
	 * @param dataProvider
	 * @return The generated search based {@link IListHandler}
	 * @throws InvalidCriteriaException When the criteria or the sorting directive
	 *         is not specified.
	 * @throws EmptyListException When the list handler type is
	 *         {@link ListHandlerType#IN_MEMORY} and no matching results exist.
	 * @throws ListHandlerException When the list handler type is
	 *         {@link ListHandlerType#IN_MEMORY} and the sorting directive is
	 *         specified but mal-formed.
	 * @throws IllegalStateException when the list handler type is un-supported.
	 */
	public static <E> IListHandler<SearchResult> create(Criteria<E> criteria, Sorting sorting,
			ListHandlerType type, IListingDataProvider<E> dataProvider) throws InvalidCriteriaException, EmptyListException,
			ListHandlerException, IllegalStateException {

		SearchListHandler<E> slh = null;

		switch(type) {

		case IN_MEMORY:
			return create(dataProvider.find(criteria, null), sorting);

		case IDLIST:
			if(criteria.getCriteriaType().isQuery()) {
				throw new InvalidCriteriaException("Id list handling does not support query based criteria");
			}
			slh = new PrimaryKeyListHandler<E>(dataProvider, criteria, sorting);
			break;

		case PAGE:
			slh = new PagingSearchListHandler<E>(dataProvider, criteria, sorting);
			break;

		default:
			throw new IllegalStateException("Unhandled list handler type: " + type);
		}

		return slh;
	}

	/**
	 * Constructor
	 */
	private ListHandlerFactory() {
	}
}