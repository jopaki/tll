/**
 * The Logic Lab
 * @author jpk
 * @since Sep 21, 2009
 */
package com.tll.dao.db4o;

import com.db4o.config.EmbeddedConfiguration;
import com.google.inject.Scopes;
import com.tll.config.Config;
import com.tll.dao.IPCHDao;
import com.tll.dao.ISiteStatisticsDao;
import com.tll.model.Account;
import com.tll.model.Interface;


/**
 * SmbizDb4oDaoModule
 * @author jpk
 */
public class SmbizDb4oDaoModule extends AbstractDb4oDaoModule {

	/**
	 * Constructor
	 */
	public SmbizDb4oDaoModule() {
		super();
	}

	/**
	 * Constructor
	 * @param config
	 */
	public SmbizDb4oDaoModule(Config config) {
		super(config);
	}

	@Override
	protected void configure() {
		super.configure();
		bind(ISiteStatisticsDao.class).to(SiteStatisticsDao.class).in(Scopes.SINGLETON);
		bind(IPCHDao.class).to(PCHDao.class).in(Scopes.SINGLETON);
	}

	@Override
	protected void configureConfiguration(EmbeddedConfiguration c) {
		c.common().objectClass(Account.class).updateDepth(3);
		c.common().objectClass(Interface.class).updateDepth(4);
		// TODO finish
	}

	@Override
	protected Class<? extends IDb4oNamedQueryTranslator> getNamedQueryTranslatorImpl() {
		return SmbizNamedQueryTranslator.class;
	}

}
