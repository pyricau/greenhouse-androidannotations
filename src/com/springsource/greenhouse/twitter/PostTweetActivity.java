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
package com.springsource.greenhouse.twitter;

import org.springframework.http.HttpStatus;
import org.springframework.social.greenhouse.api.Event;
import org.springframework.social.greenhouse.api.EventSession;
import org.springframework.web.client.HttpClientErrorException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Extra;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.springsource.greenhouse.AbstractGreenhouseActivity;
import com.springsource.greenhouse.AbstractTextWatcher;
import com.springsource.greenhouse.MainApplication;
import com.springsource.greenhouse.R;

/**
 * @author Roy Clarkson
 */
@EActivity(R.layout.post_tweet)
public class PostTweetActivity extends AbstractGreenhouseActivity {

	protected static final String TAG = PostTweetActivity.class.getSimpleName();
	
	private static final int MAX_TWEET_LENGTH = 140;
	
	
	@ViewById(R.id.post_tweet_count)
	TextView textViewCount;
	
	@ViewById(R.id.post_tweet_text)
	EditText editText;

	private Event event;
	
	private EventSession session;
	
	@Extra("reply")
	String replyExtra;
	
	@Extra("quote")
    String quoteExtra;
	
	@App
	MainApplication application;
	
	@AfterViews
	void watchEditText() {
        editText.addTextChangedListener(new AbstractTextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textViewCount.setText(String.valueOf(MAX_TWEET_LENGTH - s.length()));
             }
        });
	}
	
	@Click
    void post_tweet_button() {
        // hide the soft keypad
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        
        showProgressDialog("Posting Tweet...");
        String status = editText.getText().toString();
        postTweet(status);
    }
	
	@Background
	void postTweet(String status) {
	    String message;
        try {
            if (session != null) {
                application.getGreenhouseApi().tweetOperations().postTweetForEventSession(event.getId(), session.getId(), status);
                message = "Thank you for tweeting about this session!";
            } else {
                application.getGreenhouseApi().tweetOperations().postTweetForEvent(event.getId(), status);
                message = "Thank you for tweeting about this event!";
            }
        } catch(HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
                message = "Your account is not connected to Twitter. Please sign in to greenhouse.springsource.org to connect.";
            } else {
                Log.e(TAG, e.getLocalizedMessage(), e);
                message = "A problem occurred while posting to Twitter. Please verify your account is connected at greenhouse.springsource.org.";
            }
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            message = "A problem occurred while posting to Twitter. Please verify your account is connected at greenhouse.springsource.org.";
        }
        postTweetDone(message);
	}
	
	@UiThread
	void postTweetDone(String result) {
        dismissProgressDialog();
        showResult(result);
	}
	    
	
	//***************************************
    // Activity methods
    //***************************************
	
	@Override
	public void onStart() {
		super.onStart();
		event = application.getSelectedEvent();
		session = application.getSelectedSession();
		
		if (event == null) {
			return;
		}
		
		String tweetText = null;
		String hashtag = session == null ? event.getHashtag() : event.getHashtag() + " " + session.getHashtag();
		
		if (replyExtra !=null) {
			tweetText = new StringBuilder().append("@").append(replyExtra).append(" ").append(hashtag).toString();
		} else if (quoteExtra != null) {
			tweetText = quoteExtra;
		} else {
			tweetText = hashtag;
		}
		
		editText.setText(tweetText);
	}
	
	
	//***************************************
    // Private methods
    //***************************************
	private void showResult(String result) {
		new AlertDialog.Builder(this) //
		    .setMessage(result) //
		    .setCancelable(false) //
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int id) {
    		     	dialog.cancel();
    			}
		    }) //
		    .create() //
		    .show();
	}

}
