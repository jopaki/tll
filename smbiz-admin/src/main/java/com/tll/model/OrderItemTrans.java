package com.tll.model;

import javax.validation.constraints.NotNull;

import com.tll.model.bk.BusinessKeyDef;
import com.tll.model.bk.BusinessObject;

/**
 * Order item transaction entity
 * @author jpk
 */
@BusinessObject(businessKeys = @BusinessKeyDef(name = "Order Item Id and Order Trans Id", properties = {
	"orderItem.id", "orderTrans.id" }))
public class OrderItemTrans extends EntityBase implements IChildEntity<OrderTrans>, IAccountRelatedEntity {

	private static final long serialVersionUID = -2106851598169919247L;

	private OrderItem orderItem;

	private OrderTrans orderTrans;

	private OrderItemTransOp orderItemTransOp;

	private float amount = 0f;

	@Override
	public Class<? extends IEntity> entityClass() {
		return OrderItemTrans.class;
	}

	/**
	 * @return Returns the orderItemTransOp.
	 */
	@NotNull
	public OrderItemTransOp getOrderItemTransOp() {
		return orderItemTransOp;
	}

	/**
	 * @param orderItemTransOp The orderItemTransOp to set.
	 */
	public void setOrderItemTransOp(final OrderItemTransOp orderItemTransOp) {
		this.orderItemTransOp = orderItemTransOp;
	}

	/**
	 * @return Returns the amount.
	 */
	// @Size(min = 0, max = 99999)
	public float getAmount() {
		return amount;
	}

	/**
	 * @param amount The amount to set.
	 */
	public void setAmount(final float amount) {
		this.amount = amount;
	}

	/**
	 * @return Returns the orderItem.
	 */
	@NotNull
	public OrderItem getOrderItem() {
		return orderItem;
	}

	/**
	 * @param orderItem The orderItem to set.
	 */
	public void setOrderItem(final OrderItem orderItem) {
		this.orderItem = orderItem;
	}

	/**
	 * @return Returns the orderTrans.
	 */
	@NotNull
	public OrderTrans getOrderTrans() {
		return orderTrans;
	}

	/**
	 * @param orderTrans The orderTrans to set.
	 */
	public void setOrderTrans(final OrderTrans orderTrans) {
		this.orderTrans = orderTrans;
	}

	@Override
	public OrderTrans getParent() {
		return getOrderTrans();
	}

	@Override
	public void setParent(final OrderTrans e) {
		setOrderTrans(e);
	}

	@Override
	public Long accountKey() {
		try {
			return getOrderItem().getOrder().getAccount().getId();
		}
		catch(final NullPointerException npe) {
			LOG.warn("Unable to provide related account id due to a NULL nested entity");
			return null;
		}
	}

	public Long orderItemId() {
		try {
			return getOrderItem().getId();
		}
		catch(final NullPointerException npe) {
			return null;
		}
	}

	public Long orderTransId() {
		try {
			return getOrderTrans().getId();
		}
		catch(final NullPointerException npe) {
			return null;
		}
	}
}
