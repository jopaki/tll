/**
 * The Logic Lab
 * @author jpk
 * @since Apr 28, 2009
 */
package com.tll.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.tll.config.Config;
import com.tll.config.IConfigKey;
import com.tll.mail.NameEmail;

/**
 * EmailExceptionHandlerModule
 * @author jpk
 */
public class EmailExceptionHandlerModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(EmailExceptionHandlerModule.class);

	/**
	 * ConfigKeys - Configuration property keys for the app context.
	 * @author jpk
	 */
	public static enum ConfigKeys implements IConfigKey {

		ONERROR_SEND_EMAIL("server.onerror.ToAddress"),
		ONERROR_SEND_NAME("server.onerror.ToName");

		private final String key;

		/**
		 * Constructor
		 * @param key
		 */
		private ConfigKeys(String key) {
			this.key = key;
		}

		@Override
		public String getKey() {
			return key;
		}
	}

	/**
	 * OnErrorEmail annotation
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target( {
		ElementType.FIELD, ElementType.PARAMETER })
		@BindingAnnotation
		public @interface OnErrorEmail {
	}

	private final Config config;

	/**
	 * Constructor
	 */
	public EmailExceptionHandlerModule() {
		this(null);
	}

	/**
	 * Constructor
	 * @param config
	 */
	public EmailExceptionHandlerModule(Config config) {
		super();
		this.config = config;
	}

	@Override
	protected void configure() {
		if(config == null) throw new IllegalStateException("No config instance set.");
		log.info("Employing exception handler module");

		bind(Key.get(NameEmail.class, OnErrorEmail.class)).toProvider(new Provider<NameEmail>() {

			final String onErrorName = config.getString(ConfigKeys.ONERROR_SEND_NAME.getKey());
			final String onErrorEmail = config.getString(ConfigKeys.ONERROR_SEND_EMAIL.getKey());
			final NameEmail email = new NameEmail(onErrorName, onErrorEmail);

			@Override
			public NameEmail get() {
				return email;
			}
		}).in(Scopes.SINGLETON);

		bind(IExceptionHandler.class).to(EmailExceptionHandler.class).in(Scopes.SINGLETON);
	}

}
