package com.tll.dao.db4o;

import java.util.List;

import org.testng.annotations.Test;

import com.google.inject.Module;
import com.tll.dao.IEntityDaoTestHandler;
import com.tll.dao.db4o.test.TestDb4oDaoModule;
import com.tll.dao.test.TestEntityDaoTestHandler;
import com.tll.model.egraph.EGraphModule;
import com.tll.model.test.TestPersistenceUnitEntityGraphBuilder;

/**
 * Db4oEntityDaoTest
 * @author jpk
 */
@Test(groups = {
	"dao", "db4o"
})
public class Db4oEntityDaoTest extends AbstractDb4oEntityDaoTest {

	@Override
	protected IEntityDaoTestHandler<?>[] getDaoTestHandlers() {
		return new IEntityDaoTestHandler[] {
			new TestEntityDaoTestHandler()
		};
	}

	@Override
	protected void addModules(List<Module> modules) {
		super.addModules(modules);
		modules.add(new TestDb4oDaoModule(getConfig()));
		modules.add(new EGraphModule(TestPersistenceUnitEntityGraphBuilder.class));
	}

}
