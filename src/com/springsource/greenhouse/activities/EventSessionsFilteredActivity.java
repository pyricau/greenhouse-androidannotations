package com.springsource.greenhouse.activities;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.springsource.greenhouse.R;
import com.springsource.greenhouse.controllers.NavigationManager;

public class EventSessionsFilteredActivity extends ListActivity {
	
	private static final String TAG = "EventSessionsFilteredActivity";
	
	//***************************************
	// Activity methods
	//***************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		String[] menu_items = getResources().getStringArray(R.array.event_filtered_sessions_options_array);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.menu_list_item, menu_items); 
		setListAdapter(arrayAdapter);
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	//***************************************
    // ListActivity methods
    //***************************************
	@Override
	protected void  onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		switch(position) {
			case 0:
				Log.d(TAG, "show current sessions");
				NavigationManager.startActivity(v.getContext(), EventSessionsCurrentActivity.class);
				break;
			case 1:
				Log.d(TAG, "show upcoming sessions");
				NavigationManager.startActivity(v.getContext(), EventSessionsUpcomingActivity.class);
				break;
			case 2:
				Log.d(TAG, "show favorite sessions");
				NavigationManager.startActivity(v.getContext(), EventSessionsMyFavoritesActivity.class);
				break;
			case 3:
				Log.d(TAG, "show conference favorite sessions");
				NavigationManager.startActivity(v.getContext(), EventSessionsConferenceFavoritesActivity.class);
				break;
			default:
				break;
		}
	}

}