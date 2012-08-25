package com.tll.model;

import com.tll.IMarshalable;
import com.tll.INameValueProvider;

/**
 * Payment Item Status
 * @author jpk
 */
public enum PaymentItemStatus implements INameValueProvider<String>, IMarshalable {
	N("None"),
	A("Authorized"),
	S("Sold"),
	C("Credited"),
	V("Voided");

	private final String name;

	private PaymentItemStatus(final String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return name();
	}

}
