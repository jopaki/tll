/*
 * The Logic Lab
 */
package com.tll.dao;

import java.util.Iterator;
import java.util.Set;

import org.testng.Assert;

import com.tll.model.InterfaceMulti;
import com.tll.model.InterfaceOption;
import com.tll.model.InterfaceOptionParameterDefinition;
import com.tll.model.bk.BusinessKeyFactory;

/**
 * @author jpk
 */
public class InterfaceDaoTestHandler extends AbstractEntityDaoTestHandler<InterfaceMulti> {

	protected InterfaceOption removedOption;
	protected InterfaceOptionParameterDefinition removedParam;

	@Override
	public Class<InterfaceMulti> entityClass() {
		return InterfaceMulti.class;
	}

	/*
	 * Interfaces don't support paging.
	 */
	@Override
	public boolean supportsPaging() {
		return false;
	}

	@Override
	public void assembleTestEntity(InterfaceMulti e) throws Exception {

		final InterfaceOption o1 = create(InterfaceOption.class, false);
		Set<InterfaceOptionParameterDefinition> params =
			getAll(InterfaceOptionParameterDefinition.class);
		o1.addParameters(params);
		e.addOption(o1);

		final InterfaceOption o2 = create(InterfaceOption.class, false);
		params = getAll(InterfaceOptionParameterDefinition.class);
		o2.addParameters(params);
		e.addOption(o2);
	}

	@Override
	public void makeUnique(InterfaceMulti e) {
		super.makeUnique(e);
		for(final InterfaceOption o : e.getOptions()) {
			BusinessKeyFactory.makeBusinessKeyUnique(o);
			for(final InterfaceOptionParameterDefinition param : o.getParameters()) {
				BusinessKeyFactory.makeBusinessKeyUnique(param);
			}
		}
	}

	@Override
	public void verifyLoadedEntityState(InterfaceMulti e) throws Exception {
		super.verifyLoadedEntityState(e);

		Assert.assertNotNull(e.getCode(), "Interface code is null");
		Assert.assertNotNull(e.getOptions(), "Interface options is null");
		Assert.assertTrue(e.getOptions().size() > 0, "Interface options is empty");
		for(final InterfaceOption o : e.getOptions()) {
			if(o.getParameters() != null && o.getParameters().size() > 0) {
				for(final InterfaceOptionParameterDefinition param : o.getParameters()) {
					Assert.assertNotNull("param name is empty", param.getName());
				}
			}
		}
	}

	@Override
	public void alterTestEntity(InterfaceMulti e) {
		final Iterator<InterfaceOption> itr = e.getOptions().iterator();

		final InterfaceOption o = itr.next();
		final InterfaceOption o2 = itr.next();

		e.removeOption(o);
		removedOption = o;

		final Iterator<InterfaceOptionParameterDefinition> itrp = o2.getParameters().iterator();
		final InterfaceOptionParameterDefinition p = itrp.next();
		final InterfaceOptionParameterDefinition p2 = itrp.next();
		o2.removeParameter(p);
		removedParam = p;
		p2.setDescription("updated");
	}

	@Override
	public void verifyEntityAlteration(InterfaceMulti e) throws Exception {
		// TODO fix
		//Assert.assertNull(e.getOption(removedOption.getId()), "Orphaned option still persists");
		final InterfaceOption o = e.getOptions().iterator().next();
		for(final InterfaceOptionParameterDefinition p : o.getParameters()) {
			if(p.equals(removedParam)) {
				Assert.fail("The removed [orphan check] parameter still exists");
			}
		}
	}

	/*
	@Override
	public ISelectNamedQueryDef[] getQueriesToTest() {
		return new ISelectNamedQueryDef[] {
			SelectNamedQueries.INTERFACES, SelectNamedQueries.INTERFACE_SUMMARY_LISTING };
	}

	@Override
	public Sorting getSortingForTestQuery(ISelectNamedQueryDef qdef) {
		if(qdef.getQueryName().equals(SelectNamedQueries.INTERFACE_SUMMARY_LISTING.getQueryName())) {
			return new Sorting("name", "intf");
		}
		else if(qdef.getQueryName().equals(SelectNamedQueries.INTERFACES.getQueryName())) {
			return new Sorting("name");
		}
		return null;
	}
	 */
}
