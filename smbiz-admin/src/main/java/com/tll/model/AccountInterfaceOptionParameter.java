/**
 * The Logic Lab
 * @author jpk
 * @since Sep 6, 2009
 */
package com.tll.model;

import javax.validation.constraints.NotNull;

import com.tll.model.bk.BusinessKeyDef;
import com.tll.model.bk.BusinessObject;

/**
 * AccountInterfaceOptionParameter - Pseudo-entity to facilitate ui interaction.
 * @author jpk
 */
@BusinessObject(businessKeys = @BusinessKeyDef(name = "Code", properties = { "code" }))
public class AccountInterfaceOptionParameter extends InterfaceOptionBase {

	private static final long serialVersionUID = 7012050681612778695L;

	private String value;

	@Override
	public Class<? extends IEntity> entityClass() {
		return AccountInterfaceOptionParameter.class;
	}

	@NotNull
	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

}
