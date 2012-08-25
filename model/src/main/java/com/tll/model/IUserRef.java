package com.tll.model;

/**
 * UserRef - Simple def to hold needed user data for resetting the password.
 * @author jpk
 */
public interface IUserRef {

	Long getId();

	String getUsername();

	String getEmailAddress();
}