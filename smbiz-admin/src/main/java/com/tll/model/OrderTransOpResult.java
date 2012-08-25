package com.tll.model;

import com.tll.IMarshalable;
import com.tll.INameValueProvider;

/**
 * Order Trans Op Result
 * @author jpk
 */
public enum OrderTransOpResult implements INameValueProvider<String>, IMarshalable {
	S("Successful"),
	F("Failed");

	private final String name;

	private OrderTransOpResult(final String name) {
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
