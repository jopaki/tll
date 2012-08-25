package com.tll.model;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.tll.model.bk.BusinessKeyDef;
import com.tll.model.bk.BusinessObject;

/**
 * General product entity
 * @author jpk
 */
@BusinessObject(businessKeys = @BusinessKeyDef(name = "Titles", properties = {
	"d1", "d2" }))
public class ProductGeneral extends EntityBase {

	private static final long serialVersionUID = 459067490839802092L;

	public static final int MAXLEN_D1 = 255;
	public static final int MAXLEN_D2 = 255;
	public static final int MAXLEN_D3 = 255;
	public static final int MAXLEN_IMAGE1 = 64;
	public static final int MAXLEN_IMAGE2 = 64;

	private String d1;

	private String d2;

	private String d3;

	private String image1;

	private String image2;

	@Override
	public Class<? extends IEntity> entityClass() {
		return ProductGeneral.class;
	}

	/**
	 * @return Returns the d1.
	 */
	@NotEmpty
	@Length(max = MAXLEN_D1)
	public String getD1() {
		return d1;
	}

	/**
	 * @param d1 The d1 to set.
	 */
	public void setD1(final String d1) {
		this.d1 = d1;
	}

	/**
	 * @return Returns the d2.
	 */
	@NotEmpty
	@Length(max = MAXLEN_D2)
	public String getD2() {
		return d2;
	}

	/**
	 * @param d2 The d2 to set.
	 */
	public void setD2(final String d2) {
		this.d2 = d2;
	}

	/**
	 * @return Returns the d3.
	 */
	@NotEmpty
	@Length(max = MAXLEN_D3)
	public String getD3() {
		return d3;
	}

	/**
	 * @param d3 The d3 to set.
	 */
	public void setD3(final String d3) {
		this.d3 = d3;
	}

	/**
	 * @return Returns the image1.
	 */
	@NotEmpty
	@Length(max = MAXLEN_IMAGE1)
	public String getImage1() {
		return image1;
	}

	/**
	 * @param image1 The image1 to set.
	 */
	public void setImage1(final String image1) {
		this.image1 = image1;
	}

	/**
	 * @return Returns the image2.
	 */
	@NotEmpty
	@Length(max = MAXLEN_IMAGE2)
	public String getImage2() {
		return image2;
	}

	/**
	 * @param image2 The image2 to set.
	 */
	public void setImage2(final String image2) {
		this.image2 = image2;
	}
}