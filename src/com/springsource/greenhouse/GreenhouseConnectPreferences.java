package com.springsource.greenhouse;

import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

@SharedPref(Scope.UNIQUE)
public interface GreenhouseConnectPreferences {

	String request_token();
	
	String request_token_secret();
	
}
