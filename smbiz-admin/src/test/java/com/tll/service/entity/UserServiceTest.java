/*
 * The Logic Lab
 */
package com.tll.service.entity;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.tll.dao.AbstractDbAwareTest;
import com.tll.dao.IEntityDao;
import com.tll.model.Account;
import com.tll.model.Authority;
import com.tll.model.AuthorityRoles;
import com.tll.model.User;
import com.tll.service.entity.user.IUserService;
import com.tll.service.entity.user.UserService;

/**
 * UserServiceTest
 * @author jpk
 */
@Test(groups = "service.entity")
public class UserServiceTest extends AccountRelatedServiceTest {

	private void stubAuthorities() {
		final IEntityDao dao = getDao();
		getDbTrans().startTrans();
		try {
			for(final AuthorityRoles role : AuthorityRoles.values()) {
				final Authority a = getEntityBeanFactory().getEntityCopy(Authority.class);
				a.setAuthority(role.toString());
				dao.persist(a);
			}
			getDbTrans().setComplete();
		}
		finally {
			getDbTrans().endTrans();
		}
	}

	/**
	 * Tests {@link UserService#encodePassword(String, Object)}.
	 */
	@Test
	public void testEncodePassword() {
		final String[][] args = new String[][] {
			{
				"superuser", "abc123" }, {
				"jopaki@gmail.com", "bleamin" } };
		for(final String[] arg : args) {
			System.out.println(arg[0] + ": " + UserService.encodePassword(arg[1], arg[0]));
		}
	}

	@Test
	public void testUserCreate() throws Exception {
		Account account = null;
		try {
			// stub the related account
			account = stubValidAccount(true);

			// stub the authorities
			stubAuthorities();

			// the service test (should be transactional)
			final IUserService userService = getEntityServiceFactory().instance(IUserService.class);
			final User user = userService.create(account, "name@domain.com", "password");
			Assert.assertNotNull(user);

			getDbTrans().startTrans();
			final User dbUser = AbstractDbAwareTest.getEntityFromDb(getDao(), User.class, user.getId());
			getDbTrans().endTrans();
			Assert.assertEquals(dbUser, user);
		}
		catch(final Throwable t) {
			Assert.fail(t.getMessage(), t);
		}
	}
}
