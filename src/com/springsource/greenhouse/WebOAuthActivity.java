/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.springsource.greenhouse;

import org.springframework.http.HttpStatus;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.DuplicateConnectionException;
import org.springframework.social.greenhouse.api.Greenhouse;
import org.springframework.social.greenhouse.connect.GreenhouseConnectionFactory;
import org.springframework.social.oauth1.AuthorizedRequestToken;
import org.springframework.social.oauth1.OAuth1Parameters;
import org.springframework.social.oauth1.OAuthToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.StringRes;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

/**
 * @author Roy Clarkson
 */
@EActivity(R.layout.web_oauth)
public class WebOAuthActivity extends AbstractGreenhouseActivity {
	
	protected static final String TAG = WebOAuthActivity.class.getSimpleName();
	
	@ViewById
	WebView webView;
	
	private ConnectionRepository connectionRepository;
	
	private GreenhouseConnectionFactory connectionFactory;
	
	@StringRes
	String oauth_callback_url;
	
	@Pref
	GreenhouseConnectPreferences_ prefs;
	
	@App
	MainApplication application;

	//***************************************
    // Activity methods
    //***************************************
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
		
		connectionRepository = application.getConnectionRepository();
		connectionFactory = application.getConnectionFactory();
	}
	
	@AfterViews
	void initWebView() {
        webView.setWebViewClient(new OAuthWebViewClient());
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setSavePassword(false);
        
        webView.setWebChromeClient(
                new WebChromeClient() {
                    public void onProgressChanged(WebView view, int progress) {
                        setTitle("Loading...");
                        setProgress(progress * 100);
                        
                        if (progress == 100) {
                            setTitle(R.string.app_name);
                        }
                    }
                }
        );
	}
	
	@Override
	public void onStart() {
		super.onStart();
		greenhousePreConnect();
	}
		
	
	//***************************************
    // Private methods
    //***************************************
	private void displayGreenhouseAuthorization(OAuthToken requestToken) {
		
		if (requestToken == null) {
			return;
		}
				
		// save for later use
		saveRequestToken(requestToken);
				
		// Generate the Greenhouse authorization URL to be used in the browser or web view
		String authUrl = connectionFactory.getOAuthOperations().buildAuthorizeUrl(requestToken.getValue(), OAuth1Parameters.NONE);
		
		// display the Greenhouse authorization screen
		webView.loadUrl(authUrl);
	}
	
	private void displayGreenhouseOptions() {
		Intent intent = new Intent(this, MainActivity.class);
	    startActivity(intent);
    	finish();
	}
	
	private void saveRequestToken(OAuthToken requestToken) {
		prefs.edit() //
				.request_token().put(requestToken.getValue()) //
				.request_token_secret().put(requestToken.getSecret()) //
				.apply();
	}
	
	private OAuthToken retrieveRequestToken() {
		String token = prefs.request_token().get(null);
		String secret = prefs.request_token_secret().get(null);
		return new OAuthToken(token, secret);
	}
	
	private void deleteRequestToken() {
		prefs.clear();
	}
	
	private void displayAppAuthorizationError(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
		     	signOut();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	void greenhousePreConnect() {
		showProgressDialog("Initializing OAuth Connection...");
		greenhouseRemotePreConnect();
	}
	
	@Background
	void greenhouseRemotePreConnect() {
		try {
			// Fetch a one time use Request Token from Greenhouse
			OAuthToken token = connectionFactory.getOAuthOperations().fetchRequestToken(oauth_callback_url, null);
			greenhousePreConnectSuccess(token);
		} catch(Exception e) {
			greenhousePreConnectFailed(e);
		}
	}
	
	@UiThread
	void greenhousePreConnectSuccess(OAuthToken requestToken) {
		dismissProgressDialog();
		displayGreenhouseAuthorization(requestToken);
	}
	
	@UiThread
	void greenhousePreConnectFailed(Exception exception) {
		dismissProgressDialog();
		if (exception instanceof HttpClientErrorException) {
			if (((HttpClientErrorException) exception).getStatusCode() == HttpStatus.UNAUTHORIZED) {
				displayAppAuthorizationError("This application is not authorized to connect to Greenhouse");
			}
		}
	}
	
	void greenhousePostConnect(String verifier) {
		showProgressDialog("Finalizing OAuth Connection...");
		greenhouseRemotePostConnect(verifier);
	}
	
	@Background
	void greenhouseRemotePostConnect(String verifier) {
		OAuthToken requestToken = retrieveRequestToken();

		// Authorize the Request Token
		AuthorizedRequestToken authorizedRequestToken = new AuthorizedRequestToken(requestToken, verifier);
		
		OAuthToken accessToken = null;
		try {
			// Exchange the Authorized Request Token for the Access Token
			accessToken = connectionFactory.getOAuthOperations().exchangeForAccessToken(authorizedRequestToken, null);
		} catch(Exception e) {
			greenhousePostConnectFailed(e);
			return;
		}
		
		deleteRequestToken();
		
		// Persist the connection and Access Token to the repository 
		Connection<Greenhouse> connection = connectionFactory.createConnection(accessToken);
		
		try {
			connectionRepository.addConnection(connection);
		} catch (DuplicateConnectionException e) {
			Log.i(TAG, "attempting to add duplicate connection", e);
		}
		
		greenhousePostConnectSuccess();
	}
	
	@UiThread
	void greenhousePostConnectSuccess() {
		dismissProgressDialog();
		displayGreenhouseOptions();
	}
	
	@UiThread
	void greenhousePostConnectFailed(Exception exception) {
		dismissProgressDialog();
		if (exception instanceof HttpClientErrorException) {
			if (((HttpServerErrorException)exception).getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
				displayAppAuthorizationError("You are already connected with another Android device. Please remove the connection at Greenhouse and try again.");
			}
		}
	}
	
	private class OAuthWebViewClient extends WebViewClient {
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri  = Uri.parse(url);

			if (uri.getScheme().equals("x-com-springsource-greenhouse") && uri.getHost().equals("oauth-response")) {
				String oauthVerifier = uri.getQueryParameter("oauth_verifier");
				
				if (oauthVerifier != null) {
					Log.d(TAG, "oauth_verifier: " + oauthVerifier);
					greenhousePostConnect(oauthVerifier);
					return true;
				}
			}
			
			return false;
		}
	}
}
