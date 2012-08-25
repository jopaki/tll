/**
 * The Logic Lab
 * @author jpk
 * Jan 7, 2009
 */
package com.tll.client.ui.field;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.junit.client.GWTTestCase;
import com.tll.IPropertyNameProvider;
import com.tll.client.test.TestFieldGroupProviders;

/**
 * FieldGroupGWTTest
 * @author jpk
 */
public class FieldGroupGWTTest extends GWTTestCase {

	@Override
	public String getModuleName() {
		return "com.tll.FieldTest";
	}

	private static void fillPropNames(IField f, Collection<String> propNames) {
		if(f instanceof FieldGroup) {
			for(final IField c : (FieldGroup) f) {
				fillPropNames(c, propNames);
			}
		}
		else {
			propNames.add(((IPropertyNameProvider) f).getPropertyName());
		}
	}

	private Collection<String> getPropNames(FieldGroup fg) {
		final Set<String> set = new HashSet<String>();
		fillPropNames(fg, set);
		return set;
	}

	/**
	 * Tests {@link FieldGroup#getFieldWidgetByProperty(String)}.
	 */
	public void testGetField() {
		final FieldGroup fg = (new TestFieldGroupProviders.AddressFieldsProvider()).getFieldGroup();
		final Collection<String> propNames = getPropNames(fg);
		assert propNames != null && propNames.size() > 0;
		for(final String prop : propNames) {
			final IFieldWidget<?> f = fg.getFieldWidgetByProperty(prop);
			assert f != null;
			assert f.getPropertyName() != null && f.getPropertyName().equals(prop);
		}
	}
}
