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
import java.util.Date;
import java.util.List;

import org.springframework.social.greenhouse.api.Event;
import org.springframework.social.greenhouse.api.EventSession;

import android.util.Log;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.springsource.greenhouse.MainApplication;

/**
 * @author Roy Clarkson
 */
@EActivity
public class EventSessionsByDayActivity extends EventSessionsListActivity {
	
	private static final String TAG = EventSessionsByDayActivity.class.getSimpleName();
	
	private Date day;
	
	@App
	MainApplication application;
	
	//***************************************
	// Activity methods
	//***************************************
	@Override
	public void onStart() {
		day = application.getSelectedDay();
		super.onStart();
		if (day != null) {
			String title = new SimpleDateFormat("EEEE, MMM d").format(day);
			setTitle(title);
		}
	}
	
	//***************************************
    // Protected methods
    //***************************************
	@Override
	protected void downloadSessions() {
	    showProgressDialog();
	    downloadSessionsInBackground();
	}
	
	@Background
	void downloadSessionsInBackground() {
        try {
            Event event = getSelectedEvent();
            if (event == null || day == null) {
                downloadSessionsDone(null, null);
            } else {
                List<EventSession> sessionsOnDay = application.getGreenhouseApi().sessionOperations().getSessionsOnDay(event.getId(), day);
                downloadSessionsDone(sessionsOnDay, null);
            }
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            downloadSessionsDone(null, e);
        } 
	}
	
	@UiThread
    void downloadSessionsDone(List<EventSession> result, Exception exception) {
        dismissProgressDialog();
        processException(exception);
        setSessions(result);
    }
	
}
