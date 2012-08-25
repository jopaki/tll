/**
 * The Logic Lab
 * @author jpk
 * Jan 27, 2009
 */
package com.tll.dao;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.tll.criteria.Criteria;
import com.tll.model.IEntity;
import com.tll.model.INamedEntity;
import com.tll.model.bk.BusinessKeyFactory;
import com.tll.model.egraph.EntityBeanFactory;

/**
 * @param <E> The entity type
 * @author jpk
 */
public abstract class AbstractEntityDaoTestHandler<E extends IEntity> implements IEntityDaoTestHandler<E> {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private IEntityDao entityDao;
	private EntityBeanFactory entityBeanFactory;

	/**
	 * Necessary for datastore impls that do not support global transactions and a
	 * more granular approach to transactions is required.
	 */
	private IDbTrans dbTrans;

	@Override
	public void init(IEntityDao anEntityDao, EntityBeanFactory anEntityBeanFactory, IDbTrans aDbTrans) {
		this.entityDao = anEntityDao;
		this.entityBeanFactory = anEntityBeanFactory;
		this.dbTrans = aDbTrans;
	}

	/**
	 * Provides a fresh entity copy of a desired type.
	 * <p>
	 * Shortcut to {@link EntityBeanFactory#getEntityCopy(Class)}.
	 * @param <D>
	 * @param entityType
	 * @param makeUnique
	 * @return New entity copy
	 */
	protected final <D extends IEntity> D create(Class<D> entityType, boolean makeUnique) {
		log.debug("Creating " + (makeUnique ? "UNIQUE" : "NON-UNIQUE") + " entity of type: " + entityType);
		D e = entityBeanFactory.getEntityCopy(entityType);
		if(makeUnique) BusinessKeyFactory.makeBusinessKeyUnique(e);
		return e;
	}

	/**
	 * Shortcut to {@link EntityBeanFactory#getAllEntityCopies(Class)}.
	 * @param <D>
	 * @param entityType
	 * @return set of entities
	 */
	protected final <D extends IEntity> Set<D> getAll(Class<D> entityType) {
		return entityBeanFactory.getAllEntityCopies(entityType);
	}
	
	private void maybeStartTrans() {
		if(!dbTrans.isGlobalTrans() && !dbTrans.isTransStarted()) {
			log.debug("Starting NON-global trans");
			dbTrans.startTrans();
			dbTrans.setComplete();
		}
	}
	
	private void endAnyTrans() {
		if(!dbTrans.isGlobalTrans() && dbTrans.isTransStarted()) {
			log.debug("Ending NON-global trans");
			dbTrans.endTrans();
		}
	}

	/**
	 * Loads an entity by primary key from the datastore.
	 * @param entityType entity type
	 * @param pk primary key
	 * @return the loaded entity
	 */
	protected final <E1 extends IEntity> E1 load(Class<E1> entityType, Long pk) {
		try {
			maybeStartTrans();
			log.debug("Loading entity by primary key: " + pk);
			return entityDao.load(entityType, pk);
		}
		finally {
			endAnyTrans();
		}
	}

	/**
	 * Persists the given entity to the datastore returning the persisted entity.
	 * @param <D>
	 * @param entity
	 * @return The persisted entity
	 */
	protected final <D extends IEntity> D persist(D entity) {
		try {
			maybeStartTrans();
			log.debug("Persisting entity: " + entity);
			return entityDao.persist(entity);
		}
		finally {
			endAnyTrans();
		}
	}

	/**
	 * Purges the given entity from the datastore.
	 * @param <D>
	 * @param entity
	 */
	protected final <D extends IEntity> void purge(D entity) {
		try {
			maybeStartTrans();
			log.debug("Purging entity: " + entity);
			entityDao.purge(entity);
		}
		finally {
			endAnyTrans();
		}
	}

	/**
	 * Purges the given entity from the datastore by primary key.
	 * @param key
	 */
	protected final void purge(Class<? extends IEntity> entityType, Long key) {
		try {
			maybeStartTrans();
			log.debug("Purging entity by primary key: " + key);
			entityDao.purge(entityType, key);
		}
		finally {
			endAnyTrans();
		}
	}

	/**
	 * Convenience method that creates an entity copy via the given mock entity
	 * factory the persists it via the given entity dao.
	 * @param <D>
	 * @param entityType
	 * @param makeUnique
	 * @return The created and persisted entity
	 */
	protected final <D extends IEntity> D createAndPersist(Class<D> entityType, boolean makeUnique) {
		try {
			maybeStartTrans();
			D e = entityDao.persist(entityBeanFactory.getEntityCopy(entityType));
			if(makeUnique) BusinessKeyFactory.makeBusinessKeyUnique(e);
			return e;
		}
		finally {
			endAnyTrans();
		}
	}

	@Override
	public boolean supportsPaging() {
		return true; // true by default
	}
	
	@Override
	public final void persistDependentEntities() {
		if(dbTrans.isGlobalTrans()) dbTrans.startTrans();
		try {
			doPersistDependentEntities();
			if(dbTrans.isGlobalTrans()) dbTrans.setComplete();
		}
		finally {
			if(dbTrans.isGlobalTrans()) dbTrans.endTrans();
		}
	}

	protected void doPersistDependentEntities() {
		// base impl no-op
	}

	@Override
	public final void purgeDependentEntities() {
		if(dbTrans.isGlobalTrans()) dbTrans.startTrans();
		try {
			doPurgeDependentEntities();
			if(dbTrans.isGlobalTrans()) dbTrans.setComplete();
		}
		finally {
			if(dbTrans.isGlobalTrans()) dbTrans.endTrans();
		}
	}
	
	protected void doPurgeDependentEntities() {
		// base impl no-op
	}
	
	@Override
	public void makeUnique(E e) {
		BusinessKeyFactory.makeBusinessKeyUnique(e);
	}

	@Override
	public final void teardownTestEntity(E e) {
		purge(e);
	}

	@Override
	public void verifyLoadedEntityState(E e) throws Exception {
		if(e instanceof INamedEntity) {
			Assert.assertNotNull(((INamedEntity) e).getName(), "The name property is null");
		}
	}

	@Override
	public void alterTestEntity(E e) {
		if(e instanceof INamedEntity) {
			((INamedEntity) e).setName("altered");
		}
	}

	@Override
	public void verifyEntityAlteration(E e) throws Exception {
		if(e instanceof INamedEntity) {
			Assert.assertTrue("altered".equals(((INamedEntity) e).getName()), "Named entity alteration does not match");
		}
	}

	/*
	@Override
	public ISelectNamedQueryDef[] getQueriesToTest() {
		// default is no named queries
		return null;
	}

	@Override
	public IQueryParam[] getParamsForTestQuery(ISelectNamedQueryDef qdef) {
		// default in no params
		return null;
	}

	@Override
	public Sorting getSortingForTestQuery(ISelectNamedQueryDef qdef) {
		// default no sorting
		return null;
	}
	 */

	@Override
	public Criteria<E> getTestCriteria() {
		return null;
	}

	@Override
	public Sorting getTestSorting() {
		return null;
	}

	@Override
	public String getActualNameProperty() {
		// default
		return INamedEntity.NAME;
	}

	@Override
	public String toString() {
		return entityClass().toString();
	}
}
