package com.example.placeits;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class InactivePlaceItList extends Activity {
	private ListView listView;
	private ArrayList<String> strArray;
	private ArrayAdapter<String> adapter;
	private String username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_inactive_listview);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    username = extras.getString("com.example.placeits.user");
		}

		strArray = new ArrayList<String>();

		// Get ListView object from xml
		listView = (ListView) findViewById(R.id.lv2);
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, strArray);
		listView.setAdapter(adapter);
		
		retrieveInactivePlaceIts();
	}
	
	public void goBack(View view) {
		Intent intent = new Intent(this, MainActivity.class);
        Log.d("fdkjl",username);
		intent.putExtra("com.example.placeits.user", username);
		startActivity(intent);
	}
	
	private void retrieveInactivePlaceIts() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"Retrieving Inactive PlaceIts...", "Please wait...", false);
		
		Thread t = new Thread() {

			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet("http://placeitsserver.appspot.com/placeit");
 
			    try {
			      HttpResponse response = client.execute(request);	// Request data from client
			      HttpEntity entity = response.getEntity();			// Creates entities
			      String data = EntityUtils.toString(entity);		// Parses to one data string object
			      Log.d("InactivePlaceItList", data);
			      JSONObject myjson;
			      
			      try {
			    	  myjson = new JSONObject(data);							
			    	  JSONArray array = myjson.getJSONArray("data");		// New JSON array
			    	  for (int i = 0; i < array.length(); i++) {			// Loop to parse JSON into email and password lists
			    		  JSONObject obj = array.getJSONObject(i);
			    		  if(obj.get("user").toString().equals(username) && obj.get("status").toString().equals("0")) {
			    			  strArray.add(obj.getString("title"));
			    			  
			    		  }
			    	  }
			      } catch(JSONException e) {
			    	  Log.d("InactivePlaceItList", "Exception while trying to parse JSON");
			      }

			    }
			    catch (IOException e) {
			    	Log.d("InactivePlaceItList", "IOException while trying to conect to GAE");
			    }
				dialog.dismiss();
			}
		};

		t.start();
		dialog.show();
	}
}