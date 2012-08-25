/**
 * The Logic Lab
 * @author jpk
 * @since Jun 27, 2009
 */
package com.tll.server.listing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tll.IMarshalable;
import com.tll.common.data.ListingOp;
import com.tll.common.data.Page;
import com.tll.common.data.RemoteListingDefinition;
import com.tll.common.data.rpc.ListingPayload;
import com.tll.common.data.rpc.ListingPayload.ListingStatus;
import com.tll.common.data.rpc.ListingRequest;
import com.tll.common.msg.Msg.MsgAttr;
import com.tll.common.msg.Msg.MsgLevel;
import com.tll.common.msg.Status;
import com.tll.dao.Sorting;
import com.tll.listhandler.EmptyListException;
import com.tll.listhandler.IListHandler;
import com.tll.server.rpc.RpcServlet;

/**
 * ListingProcessor - Handles listing requests
 * @author jpk
 */
final class ListingProcessor {

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	/**
	 * Processes all listing requests.
	 * @param <S> search type
	 * @param <R> row data type
	 * @param sessionId the unique user session id
	 * @param context required listing context
	 * @param request the listing request
	 * @return the resultant listing payload
	 */
	public <S extends IMarshalable, R extends IMarshalable> ListingPayload<R> process(final String sessionId, final ListingContext context, final ListingRequest<S> request) {
		final Status status = new Status();

		if(request == null) {
			status.addMsg("No listing request specified.", MsgLevel.ERROR, MsgAttr.STATUS.flag);
		}

		final String listingId = request == null ? null : request.getListingId();
		if(listingId == null) {
			status.addMsg("No listing id specified.", MsgLevel.ERROR, MsgAttr.STATUS.flag);
		}

		final ListingOp listingOp = request == null ? null : request.getListingOp();
		if(listingOp == null) {
			status.addMsg("No listing op specified.", MsgLevel.ERROR, MsgAttr.STATUS.flag);
		}

		ListingHandler<R> handler = null;
		ListingStatus listingStatus = null;

		if(!status.hasErrors() && request != null) {
			if(sessionId == null) {
				status.addMsg("No session id specified.", MsgLevel.ERROR, MsgAttr.STATUS.flag);
			}
			else {
				final ListingCache lcache = context.getListingCache();
				assert lcache != null : "Null ListingCache";
				Integer offset = request.getOffset();
				Sorting sorting = request.getSorting();

				// get listing state (if cached)
				final ListingState state = lcache.getState(sessionId, listingId);
				if(state != null) {
					log.debug("Found cached state for listing '{}': {}", listingId, state.toString());
					if(offset == null) {
						offset = state.getOffset();
						assert offset != null;
						log.debug("Setting offset ({}) from cache for listing: {}", offset, listingId);
					}
					if(sorting == null) {
						sorting = state.getSorting();
						assert sorting != null;
						log.debug("Setting sorting ({}) from cache for listing: {}", sorting.toString(), listingId);
					}
				}

				handler = lcache.getHandler(sessionId, listingId);
				listingStatus = (handler == null ? ListingStatus.NOT_CACHED : ListingStatus.CACHED);
				log.debug("Listing status: {}", listingStatus);

				try {
					// acquire the listing handler
					if(handler == null || listingOp == ListingOp.REFRESH) {

						log.debug("Generating listing handler for listing: '{}'...", listingId);

						final RemoteListingDefinition<S> listingDef = request.getListingDef();
						if(listingDef == null) {
							status.addMsg("No listing def specified.", MsgLevel.ERROR, MsgAttr.STATUS.flag);
						}
						else {
							/*
							final S search = listingDef.getSearchCriteria();
							if(search == null) {
								throw new ListingException(listingId, "No search criteria specified.");
							}

							// translate client side criteria to server side criteria
							final Criteria<IEntity> criteria;
							try {
								// delegate
								criteria =
										(Criteria<IEntity>) context.getSearchTranslator().translateListingSearchCriteria(context, search);
							}
							catch(final IllegalArgumentException iae) {
								throw new ListingException(listingId, "Unable to translate listing search criteria: "
										+ request.descriptor(), iae);
							}

							// resolve the listing handler data provider
							final IListingDataProvider dataProvider = 
								context.getListingDataProviderResolver().resolve(request);

							// resolve the list handler type
							final ListHandlerType lht = listingDef.getListHandlerType();
							if(lht == null) {
								throw new ListingException(listingId, "No list handler type specified.");
							}
							// resolve the sorting to use
							sorting = (sorting == null ? listingDef.getInitialSorting() : sorting);
							if(sorting == null) {
								throw new ListingException(listingId, "No sorting directive specified.");
							}
							IListHandler<SearchResult> listHandler = null;
							try {
								listHandler = ListHandlerFactory.create(criteria, sorting, lht, dataProvider);
							}
							catch(final InvalidCriteriaException e) {
								throw new ListingException(listingId, "Invalid criteria: " + e.getMessage(), e);
							}
							catch(final EmptyListException e) {
								// we proceed to allow client to still show the listing
								status.addMsg(e.getMessage(), MsgLevel.WARN, MsgAttr.STATUS.flag);
							}
							catch(final ListHandlerException e) {
								// shouldn't happen
								throw new IllegalStateException("Unable to instantiate the list handler: " + e.getMessage(), e);
							}
							*/

							// transform to row list handler
							@SuppressWarnings("unchecked")
							IListHandler<R> rowListHandler = (IListHandler<R>) context.getRowListHandlerProvider().getRowListHandler(listingDef);
							handler = new ListingHandler<R>(rowListHandler, listingId, listingDef.getPageSize());
						}
					}

					// do the query related listing op
					if(handler != null && listingOp != null && listingOp.isQuery()) {
						log.debug("Performing : '{}' for '{}'", listingOp.getName(), listingId);
						try {
							handler.query(offset.intValue(), sorting, (listingOp == ListingOp.REFRESH));
							status.addMsg(listingOp.getName() + " for '" + listingId + "' successful.", MsgLevel.INFO,
									MsgAttr.STATUS.flag);
						}
						catch(final EmptyListException e) {
							// we proceed to allow client to still show the listing
							status.addMsg(e.getMessage(), MsgLevel.WARN, MsgAttr.STATUS.flag);
						}
						catch(final ListingException e) {
							throw new ListingException(listingId, "An unexpected error occurred performing listing operation: "
									+ e.getMessage(), e);
						}
					}
				}
				catch(final ListingException e) {
					RpcServlet.exceptionToStatus(e, status);
					context.getExceptionHandler().handleException(e);
				}
				catch(final RuntimeException re) {
					context.getExceptionHandler().handleException(re);
					throw re;
				}

				// do caching
				if(listingOp == ListingOp.CLEAR) {
					// clear
					log.debug("Clearing listing '{}'...", listingId);
					lcache.clearHandler(sessionId, listingId);
					if(!request.getRetainStateOnClear()) {
						lcache.clearState(sessionId, listingId);
					}
					listingStatus = ListingStatus.NOT_CACHED;
					status.addMsg("Cleared listing data for " + listingId, MsgLevel.INFO, MsgAttr.STATUS.flag);
				}
				else if(listingOp == ListingOp.CLEAR_ALL) {
					// clear all
					log.debug("Clearing ALL listings...");
					lcache.clearHandler(sessionId, listingId);
					if(!request.getRetainStateOnClear()) {
						lcache.clearAll(request.getRetainStateOnClear());
					}
					listingStatus = ListingStatus.NOT_CACHED;
					status.addMsg("Cleared ALL listing data", MsgLevel.INFO, MsgAttr.STATUS.flag);
				}
				else if(handler != null && !status.hasErrors()) {
					// cache listing handler
					log.debug("[Re-]Caching listing '{}'...", listingId);
					lcache.storeHandler(sessionId, listingId, handler);
					// cache listing state
					log.debug("[Re-]Caching listing state '{}'...", listingId);
					lcache.storeState(sessionId, listingId, new ListingState(Integer.valueOf(handler.getOffset()), handler
							.getSorting()));
					listingStatus = ListingStatus.CACHED;
				}
			}
		} // !status.hasErrors()

		final ListingPayload<R> p = new ListingPayload<R>(status, listingId, listingStatus);

		// only provide page data when it is needed at the client and there are no
		// errors
		if(handler != null && !status.hasErrors() && (listingOp != null && !listingOp.isClear())) {
			log.debug("Sending page data for '{}'...", listingId);
			p.setPageData(new Page<R>(handler.size(), handler.getPageSize(), handler.getOffset(), handler.getSorting(), handler
					.getElements()));
		}

		return p;
	}
}
