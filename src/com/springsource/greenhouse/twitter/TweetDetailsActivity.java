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

import java.text.SimpleDateFormat;

import org.springframework.http.HttpStatus;
import org.springframework.social.greenhouse.api.Event;
import org.springframework.social.greenhouse.api.EventSession;
import org.springframework.social.greenhouse.api.Tweet;
import org.springframework.web.client.HttpClientErrorException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.StringArrayRes;
import com.springsource.greenhouse.AbstractGreenhouseActivity;
import com.springsource.greenhouse.MainApplication;
import com.springsource.greenhouse.R;

/**
 * @author Roy Clarkson
 */
@EActivity(R.layout.tweet_details)
public class TweetDetailsActivity extends AbstractGreenhouseActivity {
	
	protected static final String TAG = TweetDetailsActivity.class.getSimpleName();
	
	private Event event;
	
	private EventSession session;
	
	private Tweet tweet;
	
	@ViewById(R.id.tweet_details_menu)
	ListView listView;
	
	@StringArrayRes(R.array.tweet_details_options_array)
	String[] menu_items;
	
	@App
	MainApplication application;

    private ArrayAdapter<String> arrayAdapter;
    
    @ViewById(R.id.tweet_details_fromuser)
    TextView fromUserTextView;
    
    @ViewById(R.id.tweet_details_time)
    TextView timeTextView;
    
    @ViewById(R.id.tweet_details_text)
    TextView textTextView;
    
	@AfterViews
	public void initListView() {
		arrayAdapter = new ArrayAdapter<String>(this, R.layout.menu_list_item, menu_items);
		listView.setAdapter(arrayAdapter);
	}
	
	@Override
    public void onStart() {
        super.onStart();
        event = application.getSelectedEvent();
        session = application.getSelectedSession();
        tweet = application.getSelectedTweet();
        refreshTweetDetails();
    }
	
    @ItemClick(R.id.tweet_details_menu)
    void listItemClicked(String selectedItem) {
        int position = arrayAdapter.getPosition(selectedItem);
        switch(position) {
            case 0:
                Intent replyIntent = new Intent(this, PostTweetActivity_.class);
                replyIntent.putExtra("reply", tweet.getFromUser());
                startActivity(replyIntent);
                break;
            case 1:
                showRetweetDialog();
                break;
            case 2:
                Intent quoteIntent = new Intent(this, PostTweetActivity_.class);
                String quote = new StringBuilder().append("\"@").append(tweet.getFromUser()).append(" ").append(tweet.getText()).append("\"").toString();
                quoteIntent.putExtra("quote", quote);
                startActivity(quoteIntent);
                break;
        }
    }
	

	
	//***************************************
	// Private methods
	//***************************************
	private void refreshTweetDetails() {
		if (tweet == null) {
			return;
		}
		
		fromUserTextView.setText(tweet.getFromUser());
		timeTextView.setText(new SimpleDateFormat("MMM d h:mm a").format(tweet.getCreatedAt()));
		textTextView.setText(tweet.getText());		
	}
	
	private void showRetweetDialog() {
		new AlertDialog.Builder(this) //
		       .setMessage("Are you sure you want to Retweet?") //
		       .setCancelable(false) //
		       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		    	   public void onClick(DialogInterface dialog, int id) {
		        	   retweet();
		           }
		       }) //
		       .setNegativeButton("No", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       }) //
		       .create() //
		       .show();
	}
	
	private void retweet() {
	    showProgressDialog("Retweeting...");
	    retweetInBackground();
	}
	
	@Background
	void retweetInBackground() {
        try {
            if (session != null) {
                application.getGreenhouseApi().tweetOperations().retweetForEventSession(event.getId(), session.getId(), tweet.getId());
            } else {
                application.getGreenhouseApi().tweetOperations().retweetForEvent(event.getId(), tweet.getId());
            }
            retweetDone("Thank you for tweeting about this event!", null);
        } catch(HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.PRECONDITION_FAILED) {
                retweetDone("Your account is not connected to Twitter. Please sign in to greenhouse.springsource.org to connect.", e);
            } else {
                Log.e(TAG, e.getLocalizedMessage(), e);
                retweetDone("A problem occurred while posting to Twitter. Please verify your account is connected at greenhouse.springsource.org.", e);
            }
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            retweetDone("A problem occurred while posting to Twitter. Please verify your account is connected at greenhouse.springsource.org.", e);
        }
	}
	
	@UiThread
	void retweetDone(String result, Exception exception) {
        dismissProgressDialog();
        processException(exception);
        showResult(result);
	}
	
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
