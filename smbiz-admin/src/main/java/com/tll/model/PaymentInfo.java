package com.tll.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.tll.model.bk.BusinessKeyDef;
import com.tll.model.bk.BusinessObject;

/**
 * PaymentInfo - Wraps {@link PaymentData} (for security). A simple flat-file
 * type holder for several types of payment methods.
 * @see PaymentData For the actual field list.
 * @see PaymentType For the list of app supported payment types.
 * @author jpk
 */
@BusinessObject(businessKeys = @BusinessKeyDef(name = "Name", properties = { INamedEntity.NAME }))
public class PaymentInfo extends NamedEntity {

	public static PaymentInfo findPaymentInfo(Long id) {
		return EMF.get().instanceByEntityType(PaymentInfo.class).load(id);
	}
	
	private static final long serialVersionUID = -8237732782824087760L;

	public static final int MAXLEN_NAME = 64;

	private PaymentData paymentData;

	@Override
	public Class<? extends IEntity> entityClass() {
		return PaymentInfo.class;
	}

	/**
	 * Constructor
	 */
	public PaymentInfo() {
		super();
		paymentData = new PaymentData();
	}

	@Override
	@NotEmpty
	@Length(max = MAXLEN_NAME)
	public String getName() {
		return name;
	}

	// @Type(type = "encobj")
	@NotNull
	@Valid
	@Nested
	public PaymentData getPaymentData() {
		return paymentData;
	}

	public void setPaymentData(final PaymentData paymentData) {
		this.paymentData = paymentData;
	}

	public void clearPaymentData() {
		this.paymentData = null;
	}
}
