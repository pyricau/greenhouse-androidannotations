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
package com.springsource.greenhouse.events;

import java.util.List;

import org.springframework.social.greenhouse.api.Event;

import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.UiThread;
import com.springsource.greenhouse.AbstractGreenhouseListActivity;
import com.springsource.greenhouse.MainApplication;
import com.springsource.greenhouse.R;

/**
 * @author Roy Clarkson
 */
@EActivity
public class EventsActivity extends AbstractGreenhouseListActivity {
	
	protected static final String TAG = EventsActivity.class.getSimpleName();
	
	private List<Event> upcomingEvents;
	
	@App
	MainApplication application;
	
	//***************************************
    // Activity methods
    //***************************************

	@Override
	public void onStart() {
		super.onStart();
		
		if (upcomingEvents == null) {
			downloadEvents();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.events_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.events_menu_refresh:
	    		downloadEvents();
	    		return true;
	    	default:
	    		return super.onOptionsItemSelected(item);
	    }
	}

	
    @ItemClick
    void listItemClicked(Event event) {
        application.setSelectedEvent(event);
        startActivity(new Intent(this, EventDetailsActivity_.class));
    }
	
	//***************************************
    // Private methods
    //***************************************
	private void refreshEvents(List<Event> upcomingEvents) {	
		this.upcomingEvents = upcomingEvents;

		if (upcomingEvents == null) {
			return;
		}
		
		setListAdapter(new EventsListAdapter(this, upcomingEvents));
	}
		
	private void downloadEvents() {
	    showProgressDialog();
	    downloadEventsInBackground();
	}
	
	@Background
	void downloadEventsInBackground() {
        try {
            List<Event> downloadedEvents = application.getGreenhouseApi().eventOperations().getUpcomingEvents();
            downloadEventsDone(downloadedEvents, null);
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            downloadEventsDone(null, e);
        }
	}
	
	@UiThread
	void downloadEventsDone(List<Event> result, Exception exception) {
        dismissProgressDialog();
        processException(exception);
        refreshEvents(result);
	}
	
	
}