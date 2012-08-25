/**
 * The Logic Lab
 * @author jpk
 * @since Sep 15, 2009
 */
package com.tll.dao.db4o;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.NumberRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springextensions.db4o.Db4oCallback;
import org.springextensions.db4o.Db4oTemplate;
import org.springextensions.db4o.support.Db4oDaoSupport;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.dao.DataAccessException;

import com.db4o.EmbeddedObjectContainer;
import com.db4o.ObjectContainer;
import com.db4o.events.Event4;
import com.db4o.events.EventArgs;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.events.ObjectEventArgs;
import com.db4o.query.Constraint;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import com.google.inject.Inject;
import com.tll.criteria.Criteria;
import com.tll.criteria.Criterion;
import com.tll.criteria.CriterionGroup;
import com.tll.criteria.DBType;
import com.tll.criteria.ICriterion;
import com.tll.criteria.IQueryParam;
import com.tll.criteria.InvalidCriteriaException;
import com.tll.dao.EntityExistsException;
import com.tll.dao.EntityNotFoundException;
import com.tll.dao.IEntityDao;
import com.tll.dao.IPageResult;
import com.tll.dao.NonUniqueResultException;
import com.tll.dao.SearchResult;
import com.tll.dao.SortColumn;
import com.tll.dao.Sorting;
import com.tll.model.EntityMetadata;
import com.tll.model.IEntity;
import com.tll.model.INamedEntity;
import com.tll.model.IScalar;
import com.tll.model.ITimeStampEntity;
import com.tll.model.IVersionSupport;
import com.tll.model.NameKey;
import com.tll.model.Scalar;
import com.tll.model.bk.BusinessKeyFactory;
import com.tll.model.bk.BusinessKeyPropertyException;
import com.tll.model.bk.IBusinessKey;
import com.tll.model.bk.NonUniqueBusinessKeyException;
import com.tll.types.DateRange;
import com.tll.util.PropertyPath;

/**
 * Db4oEntityDao
 * @author jpk
 */
@SuppressWarnings({
	"unchecked", "serial" })
public class Db4oEntityDao extends Db4oDaoSupport implements IEntityDao {

	/**
	 * Scalarizes an entity by first transforming the entity into a map of
	 * property name/property value where the property names are those contained
	 * in the <code>inclusionProperties</code> argument.
	 * @param <E>
	 * @param entity
	 * @param inclusionProperties The properties to scalarize. If
	 *        <code>null</code>, all 1-st level deep entity properties are
	 *        scalarized.
	 * @return A new {@link IScalar} instance
	 */
	protected static <E extends IEntity> IScalar scalarize(final E entity, Collection<String> inclusionProperties) {
		final BeanWrapper bw = new BeanWrapperImpl(entity);
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		for(final PropertyDescriptor pd : bw.getPropertyDescriptors()) {
			final String pname = pd.getName();
			if(bw.isWritableProperty(pname)) {
				map.put(pname, bw.getPropertyValue(pname));
			}
		}
		return new Scalar(entity.entityClass(), map);
	}

	/**
	 * Transforms an entity list to {@link SearchResult} instances. If
	 * <code>inclusionProperties</code> are specified (non-<code>null</code>),
	 * {@link IScalar} instances are created in place of the {@link IEntity}
	 * containing only those properties specifed in the
	 * <code>inclusionProperties</code> argument.
	 * @param <E>
	 * @param entityList
	 * @param inclusionProperties May be <code>null</code>
	 * @return New list of transormed {@link SearchResult}s.
	 */
	protected static <E extends IEntity> List<SearchResult> transformEntityList(final List<E> entityList,
			final Collection<String> inclusionProperties) {
		final List<SearchResult> slist = new ArrayList<SearchResult>(entityList.size());
		for(final E e : entityList) {
			if(inclusionProperties != null) {
				slist.add(new SearchResult(scalarize(e, inclusionProperties)));
			}
			else {
				slist.add(new SearchResult(e));
			}
		}
		return slist;
	}

	private static void registerCallbacks(ObjectContainer oc) {
		final EventRegistry registry = EventRegistryFactory.forObjectContainer(oc);
		registry.creating().addListener(new Timestamper(true));
		registry.updating().addListener(new Timestamper(false));
		final Versioner vsnr = new Versioner();
		registry.created().addListener(vsnr);
		registry.updated().addListener(vsnr);
	}

	/**
	 * Timestamper
	 * @author jpk
	 */
	@SuppressWarnings("rawtypes")
	static class Timestamper implements EventListener4 {

		static final Logger log = LoggerFactory.getLogger(Timestamper.class);

		private final boolean creating;

		public Timestamper(boolean creating) {
			super();
			this.creating = creating;
		}

		@Override
		public void onEvent(Event4 e, EventArgs args) {
			final ObjectEventArgs queryArgs = ((ObjectEventArgs) args);
			final Object o = queryArgs.object();
			if(o instanceof ITimeStampEntity) {
				final Date now = new Date();
				if(creating) ((ITimeStampEntity) o).setDateCreated(now);
				((ITimeStampEntity) o).setDateModified(now);
				log.debug("Timestamped entity: " + o);
			}
		}

	} // Timestamper

	/**
	 * Versioner
	 * @author jpk
	 */
	@SuppressWarnings("rawtypes")
	static class Versioner implements EventListener4 {

		static final Logger log = LoggerFactory.getLogger(Versioner.class);

		@Override
		public void onEvent(Event4 e, EventArgs args) {
			final ObjectEventArgs queryArgs = ((ObjectEventArgs) args);
			final Object o = queryArgs.object();
			if(o instanceof IVersionSupport) {
				final Integer cv = ((IVersionSupport) o).getVersion();
				((IVersionSupport) o).setVersion(Integer.valueOf(cv == null ? 0 : cv.intValue() + 1));
				log.debug("Versioned entity: " + o);
			}
		}

	} // Versioner

	private final IDb4oNamedQueryTranslator nqt;

	private BusinessKeyFactory businessKeyFactory;

	/**
	 * Constructor
	 * @param container The required db4o object container
	 * @param namedQueryTranslator optional named query translator to handle named
	 *        query based queries
	 * @param businessKeyFactory
	 */
	@Inject
	public Db4oEntityDao(EmbeddedObjectContainer container, IDb4oNamedQueryTranslator namedQueryTranslator,
			BusinessKeyFactory businessKeyFactory) {
		super();
		this.nqt = namedQueryTranslator;
		this.businessKeyFactory = businessKeyFactory;
		setObjectContainer(container);
	}

	@Override
	protected Db4oTemplate createDb4oTemplate(ObjectContainer container) {
		final Db4oTemplate t = super.createDb4oTemplate(container);
		registerCallbacks(t.getObjectContainer());
		return t;
	}

	@Override
	public int executeQuery(String queryName, IQueryParam[] params) throws DataAccessException {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends IEntity> List<SearchResult> find(Criteria<E> criteria, Sorting sorting)
			throws InvalidCriteriaException, DataAccessException {
		if(criteria == null) {
			throw new InvalidCriteriaException("No criteria specified.");
		}
		if(criteria.getCriteriaType() == null) {
			throw new InvalidCriteriaException("A criteria type must be specified.");
		}
		final List<E> list = findEntities(criteria, sorting);

		// transform list
		// TODO handle case where we want a sub-set of properties (a tuple scalar)
		return transformEntityList(list, null);
	}

	@Override
	public <E extends IEntity> List<E> findByPrimaryKeys(Class<E> entityType, final Collection<Long> ids, Sorting sorting)
			throws DataAccessException {
		return getDb4oTemplate().query(new Predicate<E>(entityType) {

			@Override
			public boolean match(E candidate) {
				return ids.contains((candidate.getId()));
			}
		});
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <E extends IEntity> List<E> findEntities(Criteria<E> criteria, final Sorting sorting)
			throws InvalidCriteriaException, DataAccessException {
		if(criteria == null) throw new InvalidCriteriaException("No criteria specified.");

		final Query query = getDb4oTemplate().query();

		if(criteria.getCriteriaType().isQuery()) {
			if(nqt == null) throw new InvalidCriteriaException("No db4o named query translator specified.");
			nqt.translateNamedQuery(criteria.getNamedQueryDefinition(), criteria.getQueryParams(), query);
		}
		else {
			query.constrain(criteria.getEntityClass());
			final CriterionGroup pg = criteria.getPrimaryGroup();
			if(pg != null && pg.isSet()) {
				for(final ICriterion ic : pg) {
					if(ic.isGroup()) throw new InvalidCriteriaException("Nested criterion groups are not supported");
					if(!ic.isSet()) throw new InvalidCriteriaException("criterion not set");
					final Criterion ctn = (Criterion) ic;
					final Object checkValue = ctn.getValue();
					final String pname = ctn.getPropertyName();

					Query pquery;
					if(pname.indexOf('.') > 0) {
						pquery = query;
						// descend one time for each node in the pname (which may be a dot
						// notated property path)
						final PropertyPath path = new PropertyPath(pname);
						for(final String node : path.nodes()) {
							pquery = pquery.descend(node);
						}
					}
					else {
						pquery = query.descend(pname);
					}

					switch(ctn.getComparator()) {
						case BETWEEN: {
							Object min, max;
							if(checkValue instanceof NumberRange) {
								final NumberRange range = (NumberRange) checkValue;
								min = range.getMinimumNumber();
								max = range.getMaximumNumber();
							}
							else if(checkValue instanceof DateRange) {
								final DateRange range = (DateRange) checkValue;
								min = range.getStartDate();
								max = range.getEndDate();
							}
							else {
								// presume an object array
								final Object[] oarr = (Object[]) checkValue;
								min = oarr[0];
								max = oarr[1];
							}
							pquery.constrain(min).greater().equal().or(pquery.constrain(max).smaller().equal());
							break;
						}
						case CONTAINS:
							pquery.constrain(checkValue).contains();
							break;
						case ENDS_WITH:
							pquery.constrain(checkValue).endsWith(ctn.isCaseSensitive());
							break;
						case EQUALS:
							if(!ctn.isCaseSensitive())
								throw new InvalidCriteriaException("Case insensitive equals checking is currently not supported");
							pquery.constrain(checkValue);
							break;
						case GREATER_THAN:
							pquery.constrain(checkValue).greater();
							break;
						case GREATER_THAN_EQUALS:
							pquery.constrain(checkValue).greater().equal();
							break;
						case IN: {
							Object[] arr;
							if(checkValue.getClass().isArray()) {
								arr = (Object[]) checkValue;
							}
							else if(checkValue instanceof Collection<?>) {
								arr = ((Collection) checkValue).toArray();
							}
							else if(checkValue instanceof String) {
								// assume comma-delimited string
								arr =
										org.springframework.util.ObjectUtils.toObjectArray(org.springframework.util.StringUtils
												.commaDelimitedListToStringArray((String) checkValue));
							}
							else {
								throw new InvalidCriteriaException(
										"Unsupported or null type for IN comparator: " + checkValue == null ? "<null>" : checkValue
												.getClass().toString());
							}
							Constraint c = null;
							for(final Object o : arr) {
								if(c == null) {
									c = pquery.constrain(o);
								}
								else {
									c.or(pquery.constrain(o));
								}
							}
							break;
						}
						case IS:
							if(checkValue instanceof DBType == false) {
								throw new InvalidCriteriaException("IS clauses support only check values of type: "
										+ DBType.class.getSimpleName());
							}
							final DBType dbType = (DBType) checkValue;
							if(dbType == DBType.NULL) {
								// null
								pquery.constrain(null);
							}
							else {
								// not null
								pquery.constrain(null).not();
							}
						case LESS_THAN:
							pquery.constrain(checkValue).smaller();
							break;
						case LESS_THAN_EQUALS:
							pquery.constrain(checkValue).smaller().equal();
							break;
						case LIKE:
							pquery.constrain(checkValue).like();
							break;
						case NOT_EQUALS:
							pquery.constrain(checkValue).not();
							break;
						case STARTS_WITH:
							pquery.constrain(checkValue).startsWith(ctn.isCaseSensitive());
							break;
					} // comparator switch
				}
			}
		}

		applySorting(query, sorting);

		return runQuery(query);
	}

	/**
	 * Runs the query in spring's context to take advantage of spring's unified
	 * data access exceptions.
	 * @param <E>
	 * @param q
	 * @return
	 */
	protected <E> List<E> runQuery(final Query q) {
		return (List<E>) getDb4oTemplate().execute(new Db4oCallback() {

			@Override
			public Object doInDb4o(ObjectContainer container) throws RuntimeException {
				return q.execute();
			}
		});
	}

	/**
	 * Apply sorting to the given query.
	 * @param query
	 * @param sorting
	 */
	protected void applySorting(final Query query, final Sorting sorting) {
		if(sorting != null) {
			for(final SortColumn sc : sorting.getColumns()) {
				if(sc.isAscending()) {
					query.descend(sc.getPropertyName()).orderAscending();
				}
				else {
					query.descend(sc.getPropertyName()).orderDescending();
				}
			}
		}
	}

	@Override
	public <E extends IEntity> E findEntity(Criteria<E> criteria) throws InvalidCriteriaException,
			EntityNotFoundException, NonUniqueResultException, DataAccessException {
		final List<E> list = findEntities(criteria, null);
		if(list == null || list.size() < 1) {
			throw new EntityNotFoundException("No matching entity found.");
		}
		else if(list.size() > 1) {
			throw new NonUniqueResultException("More than one matching entity found.");
		}
		assert list.size() == 1;
		return list.get(0);
	}

	@Override
	public <E extends IEntity> List<Long> getPrimaryKeys(Criteria<E> criteria, Sorting sorting)
			throws InvalidCriteriaException, DataAccessException {
		final List<E> list = findEntities(criteria, sorting);
		if(list == null) {
			return null;
		}
		final ArrayList<Long> idlist = new ArrayList<Long>();
		for(final E e : list) {
			idlist.add(e.getId());
		}
		return idlist;
	}

	@Override
	public <E extends IEntity> IPageResult<SearchResult> getPage(Criteria<E> criteria, Sorting sorting, int offset,
			int pageSize) throws InvalidCriteriaException, DataAccessException {
		List<SearchResult> elist = find(criteria, sorting);
		if(elist == null) {
			elist = new ArrayList<SearchResult>();
		}
		final int size = elist.size();
		if(size >= 1) {
			int fi = offset;
			int li = fi + pageSize;
			if(fi > size - 1) {
				fi = size - 1;
			}
			if(li > size - 1) {
				li = size; // NOTE: exclusive index
			}
			elist = elist.subList(fi, li);
		}
		final List<SearchResult> subList = elist;
		return new IPageResult<SearchResult>() {

			@Override
			public List<SearchResult> getPageList() {
				return subList;
			}

			@Override
			public int getResultCount() {
				return size;
			}
		};
	}

	/**
	 * Loads all objects of a given type.
	 * @param <E> object type
	 * @param type
	 * @param sorting optional
	 */
	protected <E> List<E> loadAllOfType(Class<E> type, Sorting sorting) {
		final Query q = getDb4oTemplate().query();
		q.constrain(type);
		applySorting(q, sorting);
		return runQuery(q);
	}

	/**
	 * Loads entities by a given {@link Predicate}.
	 * @param <E>
	 * @param p the predicate
	 * @param key The key that identifies the entity to be loaded
	 * @return All matching entities
	 */
	protected <E extends IEntity> E loadByPredicate(Predicate<E> p, Object key) throws EntityNotFoundException,
			DataAccessException {
		final List<E> list = getDb4oTemplate().query(p);
		if(list == null || list.size() < 1) {
			final String msg = "No matching entity found for key: [" + key + ']';
			logger.debug(msg);
			throw new EntityNotFoundException(msg);
		}
		if(list.size() > 1) {
			final String msg = list.size() + " matching entities found (not one) for key: [" + key + ']';
			logger.debug(msg);
			throw new EntityNotFoundException(msg);
		}
		assert list.size() == 1;
		return list.get(0);
	}

	@Override
	public <E extends IEntity> E load(Class<E> entityType, final Long key) throws EntityNotFoundException,
			DataAccessException {
		logger.debug("Loading entity by PK: " + key);
		return loadByPredicate(new Predicate<E>(entityType) {

			@Override
			public boolean match(E candidate) {
				return candidate.getId().equals(key);
			}
		}, key);
	}

	@Override
	public <E extends IEntity> E load(final IBusinessKey<E> key) throws EntityNotFoundException, DataAccessException {
		return loadByPredicate(new Predicate<E>(key.getType()) {

			@Override
			public boolean match(E candidate) {
				BusinessKeyFactory bkf = new BusinessKeyFactory(new EntityMetadata());
				return bkf.equals(candidate, key);
			}
		}, key);
	}

	@Override
	public <N extends INamedEntity> N load(final NameKey<N> nameKey) throws EntityNotFoundException,
			NonUniqueResultException, DataAccessException {
		return loadByPredicate(new Predicate<N>(nameKey.getType()) {

			@Override
			public boolean match(INamedEntity candidate) {
				final String name = nameKey.getName();
				if(nameKey.getNameProperty().equals(NameKey.DEFAULT_FIELDNAME)) {
					return name == null ? candidate.getName() == null : name.equals(candidate.getName());
				}
				BeanWrapper bw = new BeanWrapperImpl(candidate);
				Object val = bw.getPropertyValue(nameKey.getNameProperty());
				return name == null ? val == null : name.equals(val);
			}
		}, nameKey);
	}

	@Override
	public <E extends IEntity> List<E> loadAll(Class<E> entityType) throws DataAccessException {
		final List<E> list = getDb4oTemplate().query(new Predicate<E>(entityType) {

			@Override
			public boolean match(E candidate) {
				return true;
			}
		});
		return list;
	}

	@Override
	public <E extends IEntity> E persist(E entity) throws EntityExistsException, DataAccessException {
		// we require a pre-set id
		if(entity.isNew() && entity.getId() == null) throw new IllegalArgumentException("No entity id set");

		logger.debug("Persisting entity: " + entity);

		// must check for business key uniqueness first!
		try {
			final List<E> list = (List<E>) loadAll(entity.entityClass());
			final ArrayList<E> mlist = new ArrayList<E>((list == null ? 0 : list.size()) + 1);
			mlist.addAll(list);
			if(entity.isNew()) {
				mlist.add(entity);
			}
			else {
				// find it in mlist and replace
				for(int i = 0; i < mlist.size(); i++) {
					final E e = mlist.get(i);
					if(e.equals(entity)) {
						mlist.set(i, entity);
						break;
					}
				}
			}
			businessKeyFactory.isBusinessKeyUnique(mlist);
		}
		catch(final NonUniqueBusinessKeyException e) {
			throw new EntityExistsException(e.getMessage());
		}
		catch(final BusinessKeyPropertyException e) {
			throw new IllegalStateException(e);
		}

		// we must purge the existing entity first!
		if(!entity.isNew()) {
			try {
				purge(entity);
				if(logger.isDebugEnabled()) logger.debug("Purged existing entity by id before updating: " + entity);
			}
			catch(EntityNotFoundException e) {
				// ok
			}
		}

		getDb4oTemplate().store(entity);

		return entity;
	}

	@Override
	public <E extends IEntity> Collection<E> persistAll(Collection<E> entities) throws DataAccessException {
		if(entities != null) {
			for(final E e : entities) {
				persist(e);
			}
		}
		return entities;
	}

	@Override
	public <E extends IEntity> void purge(E entity) throws EntityNotFoundException, DataAccessException {
		logger.debug("Purging entity: " + entity);
		purge(entity.entityClass(), entity.getId());
	}

	@Override
	public <E extends IEntity> void purge(Class<E> entityType, Long pk) throws EntityNotFoundException,
			DataAccessException {
		final E existing = load(entityType, pk);
		if(existing == null) throw new EntityNotFoundException("Entity of primary key: " + pk + " not found for purging");
		getDb4oTemplate().delete(existing);
		getDb4oTemplate().purge(existing);
	}

	@Override
	public <E extends IEntity> void purgeAll(Collection<E> entities) throws DataAccessException {
		if(entities != null) {
			for(final E e : entities) {
				purge(e);
			}
		}
	}

}
