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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.social.greenhouse.api.Event;

import android.content.Intent;
import android.widget.ArrayAdapter;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ItemClick;
import com.springsource.greenhouse.AbstractGreenhouseListActivity;
import com.springsource.greenhouse.MainApplication;
import com.springsource.greenhouse.R;

/**
 * @author Roy Clarkson
 */
@EActivity
public class EventSessionsScheduleActivity extends AbstractGreenhouseListActivity {
	
	@SuppressWarnings("unused")
	private static final String TAG = EventSessionsScheduleActivity.class.getSimpleName();
	
	private Event event;
	
	private List<Date> conferenceDates;
	
	@App
	MainApplication application;

    private ArrayAdapter<String> adapter;
	

	//***************************************
	// Activity methods
	//***************************************
	
	@Override
	public void onStart() {
		super.onStart();
		event = application.getSelectedEvent();
		application.setSelectedDay(null);
		refreshScheduleDays();
	}
	
	@ItemClick
	void listItemClicked(String dayString) {
	    int position = adapter.getPosition(dayString);
	    Date day = conferenceDates.get(position);
        application.setSelectedDay(day);
        startActivity(new Intent(this, EventSessionsByDayActivity_.class));
	}
	
	//***************************************
	// Private methods
	//***************************************
	private void refreshScheduleDays() {
		if (event == null) {
			return;
		}
		
		conferenceDates = new ArrayList<Date>();
		List<String> conferenceDays = new ArrayList<String>();
		Date day = (Date) event.getStartTime().clone();

		while (day.before(event.getEndTime())) {
			conferenceDates.add((Date) day.clone());
			conferenceDays.add(new SimpleDateFormat("EEEE, MMM d").format(day));
			// Common Roy, that's dirty ;-) !
			day.setDate(day.getDate() + 1);
		}
		
		adapter = new ArrayAdapter<String>(this, R.layout.menu_list_item, conferenceDays);
        setListAdapter(adapter);
	}
}
