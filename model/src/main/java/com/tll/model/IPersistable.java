package com.tll.model;

import java.io.Serializable;

/**
 * Indicates the ability for an <code>Object</code> to be persisted.
 * @author jpk
 */
public interface IPersistable extends Serializable {

	/**
	 * @return true if the object has been persisted, false otherwise
	 */
	boolean isNew();
}
