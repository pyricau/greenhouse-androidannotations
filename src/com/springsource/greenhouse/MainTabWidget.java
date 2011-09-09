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

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.widget.TabHost;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.res.DrawableRes;
import com.springsource.greenhouse.events.EventsActivity_;
import com.springsource.greenhouse.profile.ProfileActivity_;

/**
 * @author Roy Clarkson
 */
@EActivity(R.layout.main)
public class MainTabWidget extends TabActivity {
	
	@DrawableRes
	Drawable ic_tab_events;
	
	@DrawableRes
	Drawable ic_tab_profile;
	
	@DrawableRes
	Drawable ic_tab_info;
	
	@AfterViews
	void fillTabs() {
		TabHost tabHost = getTabHost();
		TabHost.TabSpec tabSpec;
		Intent intent;
				
		// add events tab
		intent = new Intent(this, EventsActivity_.class);
		
		tabSpec = tabHost.newTabSpec("events");
		tabSpec.setIndicator("Events", ic_tab_events);
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);
		
		// add profile tab
		intent = new Intent(this, ProfileActivity_.class);
		
		tabSpec = tabHost.newTabSpec("profile");
		tabSpec.setIndicator("Profile", ic_tab_profile);
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);
		
		// add info tab
		intent = new Intent(this, InfoActivity_.class);
		
		tabSpec = tabHost.newTabSpec("info");
		tabSpec.setIndicator("Info", ic_tab_info);
		tabSpec.setContent(intent);
		tabHost.addTab(tabSpec);
	}
}
