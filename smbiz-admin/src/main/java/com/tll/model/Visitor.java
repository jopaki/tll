package com.tll.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.tll.model.bk.BusinessKeyDef;
import com.tll.model.bk.BusinessObject;

/**
 * The visitor entity (those people who visit a storefront site).
 * @author jpk
 */
@BusinessObject(businessKeys = @BusinessKeyDef(name = "Account Id, Date Created, Remote Host", properties = {
	"account.id", "dateCreated", "remoteHost" }))
public class Visitor extends TimeStampEntity implements IChildEntity<Account>, IAccountRelatedEntity {

	private static final long serialVersionUID = 3466539674112418212L;

	public static final int MAXLEN_REMOTE_HOST = 64;
	public static final int MAXLEN_REMOTE_ADDR = 64;
	public static final int MAXLEN_REMOTE_USER = 64;
	public static final int MAXLEN_MC = 16;

	private String remoteHost;

	private String remoteAddr;

	private String remoteUser;

	private String mc;

	private Account account;

	@Override
	public Class<? extends IEntity> entityClass() {
		return Visitor.class;
	}

	/**
	 * @return Returns the account.
	 */
	@NotNull
	public Account getAccount() {
		return account;
	}

	/**
	 * @param account The account to set.
	 */
	public void setAccount(final Account account) {
		this.account = account;
	}

	/**
	 * @return Returns the mc.
	 */
	@Length(max = MAXLEN_MC)
	public String getMc() {
		return mc;
	}

	/**
	 * @param mc The mc to set.
	 */
	public void setMc(final String mc) {
		this.mc = mc;
	}

	/**
	 * @return Returns the remoteAddr.
	 */
	@Length(max = MAXLEN_REMOTE_ADDR)
	public String getRemoteAddr() {
		return remoteAddr;
	}

	/**
	 * @param remoteAddr The remoteAddr to set.
	 */
	public void setRemoteAddr(final String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	/**
	 * @return Returns the remoteHost.
	 */
	@NotEmpty
	@Length(max = MAXLEN_REMOTE_HOST)
	public String getRemoteHost() {
		return remoteHost;
	}

	/**
	 * @param remoteHost The remoteHost to set.
	 */
	public void setRemoteHost(final String remoteHost) {
		this.remoteHost = remoteHost;
	}

	/**
	 * @return Returns the remoteUser.
	 */
	@NotEmpty
	@Length(max = MAXLEN_REMOTE_USER)
	public String getRemoteUser() {
		return remoteUser;
	}

	/**
	 * @param remoteUser The remoteUser to set.
	 */
	public void setRemoteUser(final String remoteUser) {
		this.remoteUser = remoteUser;
	}

	@Override
	public Account getParent() {
		return getAccount();
	}

	@Override
	public void setParent(final Account e) {
		setAccount(e);
	}

	@Override
	public Long accountKey() {
		try {
			return getAccount().getId();
		}
		catch(final NullPointerException npe) {
			LOG.warn("Unable to provide related account id due to a NULL nested entity");
			return null;
		}
	}
}