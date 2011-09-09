package com.springsource.greenhouse.events;

import java.util.List;

import org.springframework.social.greenhouse.api.Event;
import org.springframework.social.greenhouse.api.Tweet;
import org.springframework.social.greenhouse.api.TweetFeed;

import android.util.Log;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.springsource.greenhouse.MainApplication;
import com.springsource.greenhouse.twitter.TweetsListActivity;

@EActivity
public class EventTweetsActivity extends TweetsListActivity {
	
	private static final String TAG = EventTweetsActivity.class.getSimpleName();
	
	@App
	MainApplication application;
	
	@Override
	protected void downloadTweets() {
	    showProgressDialog();
	    downloadTweetsInBackground();
	}
	
	@Background
	void downloadTweetsInBackground() {
        try {
            Event event = getSelectedEvent();
            if (event != null) {
                TweetFeed feed = application.getGreenhouseApi().tweetOperations().getTweetsForEvent(event.getId());
                downloadTweetsDone(feed.getTweets(), null);
            }
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            downloadTweetsDone(null, e);
        } 
	}
	
	@UiThread
	void downloadTweetsDone(List<Tweet> result, Exception exception) {
        dismissProgressDialog();
        processException(exception);
        setTweets(result);
	}

}
