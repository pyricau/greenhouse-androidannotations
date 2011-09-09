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
package com.springsource.greenhouse.events.sessions;

import org.springframework.social.greenhouse.api.Event;
import org.springframework.social.greenhouse.api.EventSession;

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
import com.springsource.greenhouse.twitter.PostTweetActivity_;

/**
 * @author Roy Clarkson
 */
@EActivity(R.layout.event_session_details)
public class EventSessionDetailsActivity extends AbstractGreenhouseActivity {
	
	private static final String TAG = EventSessionDetailsActivity.class.getSimpleName();
	
	private Event event;
	
	private EventSession session;
	
	@ViewById(R.id.event_session_details_menu)
	ListView listView;
	
	@StringArrayRes(R.array.event_session_details_options_array)
	String[] menu_items;

    private ArrayAdapter<String> arrayAdapter;
    
    @App
    MainApplication application;
	
    @AfterViews
    void initListView() {
        arrayAdapter = new ArrayAdapter<String>(this, R.layout.menu_list_item, menu_items);
        listView.setAdapter(arrayAdapter);
    }
    
    @ItemClick(R.id.event_session_details_menu)
    void listItemClicked(String selectedItem) {
        int position = arrayAdapter.getPosition(selectedItem);
        switch(position) {
        case 0:
            showProgressDialog("Updating favorite ...");
            updateFavorite();
            break;
        case 1:
            startActivity(new Intent(this, EventSessionRatingActivity.class));
            break;
        case 2:
            startActivity(new Intent(this, PostTweetActivity_.class));
            break;
        case 3:
            startActivity(new Intent(this, EventSessionTweetsActivity.class));
            break;
        default:
            break;
    }
    }
    
	//***************************************
	// Activity methods
	//***************************************

    @Override
	public void onStart() {
		super.onStart();
		event = application.getSelectedEvent();
		session = application.getSelectedSession();		
		refreshEventDetails(); 
	}
	
	
	//***************************************
	// Private methods
	//***************************************
	private void refreshEventDetails() {		
		if (session == null) {
			return;
		}
		
		TextView t = (TextView) findViewById(R.id.event_session_details_name);
		t.setText(session.getTitle());
		
		t = (TextView) findViewById(R.id.event_session_details_leaders);
		t.setText(session.getJoinedLeaders(", "));
		
		t = (TextView) findViewById(R.id.event_session_details_time_and_room);
		t.setText(session.getFormattedTimeSpan() + " in " + session.getRoom().getLabel());
		
		t = (TextView) findViewById(R.id.event_session_details_rating);
		if (session.getRating() == 0) {
			t.setText("No Ratings");
		} else {
			t.setText(session.getRating() + " Stars");
		}
		
		t = (TextView) findViewById(R.id.event_session_details_description);
		t.setText(session.getDescription());
		
		setFavoriteStatus(session.isFavorite());
	}
	
	private void setFavoriteStatus(boolean status) {
		final TextView textViewSessionFavorite = (TextView) findViewById(R.id.event_session_details_favorite);
		String text = status ? "Favorite: \u2713" : "Not a Favorite";
		textViewSessionFavorite.setText(text);
	}
	
	@Background
	void updateFavorite() {
        try {
            if (event == null || session == null) {
                updateFavoriteDone(false, null);
            } else {
               boolean status = application.getGreenhouseApi().sessionOperations().updateFavoriteSession(event.getId(), session.getId());
               updateFavoriteDone(status, null);
            }
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            updateFavoriteDone(false, e);
        } 
	}
	
	@UiThread
	void updateFavoriteDone(boolean result, Exception exception) {
	       dismissProgressDialog();
           processException(exception);
           setFavoriteStatus(result);
	}
	
}
