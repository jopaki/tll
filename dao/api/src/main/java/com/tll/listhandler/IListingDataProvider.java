package com.tll.listhandler;

import java.util.Collection;
import java.util.List;

import com.tll.criteria.Criteria;
import com.tll.criteria.InvalidCriteriaException;
import com.tll.dao.IPageResult;
import com.tll.dao.SearchResult;
import com.tll.dao.Sorting;

/**
 * IListHandlerDataProvider - Definition for providing data to
 * {@link IListHandler}s. This definition supports in memory collection paging,
 * result set paging and id list based paging.
 * <p>
 * <b>NOTE: </b>All methods are subject to throwing a
 * <code>org.springframework.dao.DataAccessException</code>
 * @see ListHandlerType
 * @author jpk
 * @param <E> entity type
 */
public interface IListingDataProvider<E> {

	/**
	 * Retrieves a list of matching results for the given criteria.
	 * @param criteria
	 * @param sorting
	 * @return list of result elements or an empty list if no matches are found.
	 * @throws InvalidCriteriaException
	 */
	List<SearchResult> find(Criteria<E> criteria, Sorting sorting) throws InvalidCriteriaException;

	/**
	 * Retrieves the primary keys of the entities that match the given criteria.
	 * @param criteria
	 * @param sorting
	 * @return list of matching primary keys or an empty list if no matching
	 *         results are found.
	 * @throws InvalidCriteriaException
	 */
	List<Long> getPrimaryKeys(Criteria<E> criteria, Sorting sorting) throws InvalidCriteriaException;

	/**
	 * Retrieves entities from a collection of primary keys.
	 * @param entityClass The entity class the primary keys represent.
	 * @param pks List of primary key of the entities to retrieve.
	 * @param sorting the sorting directive May be null in which case the sorting
	 *        of the results is "undefined".
	 * @return list of matching entities.
	 */
	List<E> getEntitiesFromIds(Class<E> entityClass, Collection<Long> pks, Sorting sorting);

	/**
	 * Returns a page of matching results for the given criteria.
	 * @param criteria
	 * @param sorting
	 * @param offset
	 * @param pageSize
	 * @return the page result
	 * @throws InvalidCriteriaException
	 */
	IPageResult<SearchResult> getPage(Criteria<E> criteria, Sorting sorting, int offset, int pageSize)
			throws InvalidCriteriaException;
}
