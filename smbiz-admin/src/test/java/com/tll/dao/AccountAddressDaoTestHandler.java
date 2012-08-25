/*
 * The Logic Lab
 */
package com.tll.dao;

import org.testng.Assert;

import com.tll.model.AccountAddress;
import com.tll.model.Address;
import com.tll.model.AddressType;
import com.tll.model.Asp;
import com.tll.model.Currency;

/**
 * AccountAddressDaoTestHandler
 * @author jpk
 */
public class AccountAddressDaoTestHandler extends AbstractEntityDaoTestHandler<AccountAddress> {

	private Long pkCurrency, pkAsp, pkAddress;

	@Override
	public Class<AccountAddress> entityClass() {
		return AccountAddress.class;
	}

	@Override
	public boolean supportsPaging() {
		return false;
	}

	@Override
	public void doPersistDependentEntities() {
		final Currency currency = createAndPersist(Currency.class, true);
		this.pkCurrency = currency.getId();

		Asp asp = create(Asp.class, true);
		asp.setCurrency(currency);
		asp.setPaymentInfo(null);
		asp.setParent(null);
		asp = persist(asp);
		this.pkAsp = asp.getId();

		final Address address = createAndPersist(Address.class, true);
		this.pkAddress = address.getId();
	}

	@Override
	public void doPurgeDependentEntities() {
		purge(Address.class, pkAddress);
		purge(Asp.class, pkAsp);
		purge(Currency.class, pkCurrency);
	}

	@Override
	public void assembleTestEntity(AccountAddress e) throws Exception {
		e.setAccount(load(Asp.class, pkAsp));
		e.setAddress(load(Address.class, pkAddress));
	}

	@Override
	public void verifyLoadedEntityState(AccountAddress e) throws Exception {
		Assert.assertNotNull(e.getAccount());
		Assert.assertNotNull(e.getAddress());
		Assert.assertNotNull(e.getParent());
	}

	@Override
	public void alterTestEntity(AccountAddress e) {
		super.alterTestEntity(e);
		Assert.assertFalse(AddressType.CONTACT.equals(e.getType()));
		e.setType(AddressType.CONTACT);
	}

	@Override
	public void verifyEntityAlteration(AccountAddress e) throws Exception {
		super.verifyEntityAlteration(e);
		Assert.assertTrue(AddressType.CONTACT.equals(e.getType()));
	}

}
