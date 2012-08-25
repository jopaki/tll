/**
 * The Logic Lab
 * @author jpk
 * Jan 27, 2009
 */
package com.tll.dao;

import com.tll.criteria.Criteria;
import com.tll.model.IEntity;
import com.tll.model.INamedEntity;
import com.tll.model.egraph.EntityBeanFactory;

/**
 * IEntityDaoTestHandler - Encapsulates entity lifecycle behavior for testing
 * against the generic entity dao.
 * @param <E> The entity type
 * @author jpk
 */
public interface IEntityDaoTestHandler<E extends IEntity> {

	/**
	 * Manual injection of dependencies.
	 * @param entityDao
	 * @param entityBeanFactory
	 * @param dbTrans This is necessary for datastores that don't support global
	 *        transactions (like GAE).
	 */
	void init(IEntityDao entityDao, EntityBeanFactory entityBeanFactory, IDbTrans dbTrans);

	/**
	 * @return The entity type this handler supports.
	 */
	Class<E> entityClass();

	/**
	 * Does this entity type support the dao paging related methods?
	 * @return true/false
	 */
	boolean supportsPaging();

	/**
	 * Hook to optionally persist related dependent entities.
	 */
	void persistDependentEntities();

	/**
	 * Corollary to {@link #persistDependentEntities()}
	 */
	void purgeDependentEntities();

	/**
	 * Assembles the test entity persisting any necessary related entities.
	 * @param e The test entity
	 * @throws Exception
	 */
	void assembleTestEntity(E e) throws Exception;

	/**
	 * Tears down the test entity ensuring any related entities are removed from
	 * the datastore.
	 * @param e The test entity
	 */
	void teardownTestEntity(E e);

	/**
	 * Makes unique the given test entity ensuring any related one/many entities
	 * that are part of the test entities life-cycle are as well.
	 * @param e The test entity
	 */
	void makeUnique(E e);

	/**
	 * Verifies the loaded state of the entity ensuring any related one/many
	 * entities are valid as well.
	 * @param e The test entity
	 * @throws Exception When the loaded state is not valid
	 */
	void verifyLoadedEntityState(E e) throws Exception;

	/**
	 * Alters the test entity such that
	 * @param e
	 */
	void alterTestEntity(E e);

	/**
	 * Verifies the alteration(s) to the test entity after dao update subject to
	 * alteration(s) made via {@link #alterTestEntity(IEntity)}.
	 * @param e the test entity
	 * @throws Exception if alteration(s) don't remain
	 */
	void verifyEntityAlteration(E e) throws Exception;

	/**
	 * @return Optional {@link Criteria} instance by which to test the dao find
	 *         entities by criteria routine.
	 */
	Criteria<E> getTestCriteria();

	/**
	 * @return Optional test sorting by which to test the dao find entities by
	 *         criteria routine.
	 */
	Sorting getTestSorting();

	/**
	 * @return The actual entity object property that holds the entity name value
	 *         if the entity implements {@link INamedEntity}. The default value is
	 *         expected to be {@link INamedEntity#NAME} even if the impl's target
	 *         entity type is not an {@link INamedEntity}. This method is a
	 *         testing convenience only but is necessary for some dao impls as
	 *         some query apis target the actual property names and NOT the bean
	 *         property names (java bean convention).
	 */
	String getActualNameProperty();

	/**
	 * @return An array of select named query definitions to test.
	 */
	// ISelectNamedQueryDef[] getQueriesToTest();

	/**
	 * Provides query params for a particular named select query to test.
	 * @param qdef the particular named query under testing
	 * @return the needed query params or <code>null</code> which indicates the
	 *         given query requires no parameters.
	 */
	// IQueryParam[] getParamsForTestQuery(ISelectNamedQueryDef qdef);

	/**
	 * @param qdef the particular named query under testing
	 * @return The desired sorting directive that may or may not be required to
	 *         resolve the fully qualified query name in the persistence context.
	 * @see ISelectNamedQueryDef for an explanation of the query naming
	 *      convention.
	 */
	// Sorting getSortingForTestQuery(ISelectNamedQueryDef qdef);
}
