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
package com.springsource.greenhouse.profile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.social.greenhouse.api.GreenhouseProfile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.App;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.springsource.greenhouse.AbstractGreenhouseActivity;
import com.springsource.greenhouse.MainApplication;
import com.springsource.greenhouse.R;

/**
 * @author Roy Clarkson
 */
@EActivity(R.layout.profile)
public class ProfileActivity extends AbstractGreenhouseActivity {
	
	protected static final String TAG = ProfileActivity.class.getSimpleName();
	
	private GreenhouseProfile profile;
	
	@App
	MainApplication application;
	
	@ViewById(R.id.profile_textview_member_name)
	TextView textViewMemberName;
	
	@ViewById(R.id.profile_imageview_picture)
	ImageView imageViewPicture;
	
	
	//***************************************
    // Activity methods
    //***************************************
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (profile == null) {
			downloadProfile();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.profile_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
		    case R.id.profile_menu_refresh:
		        downloadProfile();
		        return true;
		    case R.id.profile_menu_sign_out:
		        signOut();
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	//***************************************
    // Private methods
    //***************************************
	private void refreshProfile(GreenhouseProfile profile) {
		if (profile == null) {
			return;
		}
		
		this.profile = profile;
		
		textViewMemberName.setText(profile.getDisplayName());
		downloadProfileImage(profile.getPictureUrl());
	}
	
	@Background
	void downloadProfileImage(String urlString) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bitmap = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "Error retrieving profile image", e);
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        profileImageDownloaded(bitmap);
	}
	
	@UiThread
	void profileImageDownloaded(Bitmap result) {
        imageViewPicture.setImageBitmap(result);
	}
	
	    
    private void downloadProfile() {
        showProgressDialog(); 
		downloadProfileInbackground();
	}
    
    @Background
    void downloadProfileInbackground() {
        try {
            GreenhouseProfile downloadedProfile = application.getPrimaryConnection().getApi().userOperations().getUserProfile();
            downloadProfileDone(downloadedProfile, null);
        } catch(Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
            downloadProfileDone(null, e);
        }
    }
    
    @UiThread
    void downloadProfileDone(GreenhouseProfile result, Exception exception)  {
        dismissProgressDialog();
        processException(exception);
        refreshProfile(result);
    }
    
}
