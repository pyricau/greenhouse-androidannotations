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

import org.springframework.social.greenhouse.api.Event;

import android.app.Activity;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.res.StringArrayRes;
import com.springsource.greenhouse.AbstractGreenhouseActivity;
import com.springsource.greenhouse.MainApplication;
import com.springsource.greenhouse.R;
import com.springsource.greenhouse.events.sessions.EventSessionsFilteredActivity;
import com.springsource.greenhouse.events.sessions.EventSessionsScheduleActivity_;
import com.springsource.greenhouse.twitter.PostTweetActivity_;

/**
 * @author Roy Clarkson
 */
@EActivity(R.layout.event_details)
public class EventDetailsActivity extends AbstractGreenhouseActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = EventDetailsActivity.class.getSimpleName();
	
	private Event event;
	
	
	@ViewById(R.id.event_details_menu)
	ListView listView;
	
	@ViewById(R.id.event_details_name)
	TextView nameTextView;
	
	@ViewById(R.id.event_details_date)
    TextView dateTextView;
   
    @ViewById(R.id.event_details_location)
    TextView locationTextView;
    
    @ViewById(R.id.event_details_description)
    TextView descriptionTextView;
	
	@StringArrayRes(R.array.event_details_options_array)
	String[] menu_items;
	
	@App
	MainApplication application;
	

    private ArrayAdapter<String> arrayAdapter;

	//***************************************
	// Activity methods
	//***************************************
	
	@AfterViews
	void prepareListView() {
	    arrayAdapter = new ArrayAdapter<String>(this, R.layout.menu_list_item, menu_items);
        listView.setAdapter(arrayAdapter);
	}
	
    @ItemClick(R.id.event_details_menu)
    void listItemClicked(String selectedItem) {
        
        int position = arrayAdapter.getPosition(selectedItem);
        
        Class<? extends Activity> activityClass;
        switch(position) {
            case 0:
                activityClass = EventSessionsFilteredActivity.class;
                break;
            case 1:
                activityClass = EventSessionsScheduleActivity_.class;
                break;
            case 2:
                activityClass = PostTweetActivity_.class;
                break;
            case 3:
                activityClass = EventTweetsActivity_.class;
                break;
            default:
                    return;
        }
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }
    
	@Override
	public void onStart() {
		super.onStart();
		event = application.getSelectedEvent();		
		refreshEventDetails();
	}
	
	
	//***************************************
	// Private methods
	//***************************************
	private void refreshEventDetails() {
		if (event == null) {
			return;
		}
		
		nameTextView.setText(event.getTitle());
		dateTextView.setText(event.getFormattedTimeSpan());
		locationTextView.setText(event.getLocation());
		descriptionTextView.setText(event.getDescription());		
	}
	
}
