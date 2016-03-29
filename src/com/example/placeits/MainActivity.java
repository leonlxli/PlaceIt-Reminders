package com.example.placeits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity implements OnMapClickListener,
		ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
		AdapterView.OnItemSelectedListener {

	private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
	// Milliseconds per second
	private static final int MILLISECONDS_PER_SECOND = 1000;
	// Update frequency in seconds
	public static final int UPDATE_INTERVAL_IN_SECONDS = 1;
	// Update frequency in milliseconds
	private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND
			* UPDATE_INTERVAL_IN_SECONDS;
	// The fastest update frequency, in seconds
	private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
	// A fast frequency ceiling in milliseconds
	private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND
			* FASTEST_INTERVAL_IN_SECONDS;

	public static final String ITEM_URI = "http://placeitsserver.appspot.com/placeit";
	
	public static final int REQUEST_CODE_LOGIN = 1;
	Spinner spinner;

	LocationRequest mLocationRequest;
	boolean mUpdatesRequested;
	private SharedPreferences.Editor mEditor;
	private SharedPreferences mPrefs;
	private LocationClient mLocationClient;
	private GoogleMap mMap;
	private TextView mLatLong;
	protected PlaceIt placeIt;
	public String username;
	private List<NameValuePair> placeItData;

	protected List<String> titleList = new ArrayList<String>();
	protected List<String> descList = new ArrayList<String>();
	protected List<String> statusList = new ArrayList<String>();
	protected List<String> latList = new ArrayList<String>();
	protected List<String> lngList = new ArrayList<String>();
	protected List<String> categoryList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			username = extras.getString("com.example.placeits.user");
		}
		// username =
		// PreferenceManager.getDefaultSharedPreferences(this).getString("User",
		// username);
		if (username == null) {
			goToLogin();
		} else {
			Log.d("DKFJSLKFJDLFKDLFJSKL", username);
		}

		setUpMapIfNeeded();
		retrievePlaceIts();
		mMap.setOnMapClickListener(this);
		mMap.setMyLocationEnabled(true);

		/* LOCATION UPDATES */
		mLatLong = (TextView) findViewById(R.id.latLong);
		// Create the LocationRequest object
		mLocationRequest = LocationRequest.create();
		// Use high accuracy
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		// Set the update interval to 5 seconds
		mLocationRequest.setInterval(UPDATE_INTERVAL);
		// Set the fastest update interval to 1 second
		mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

		// Open the shared preferences
		mPrefs = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
		// Get a SharedPreferences editor
		mEditor = mPrefs.edit();

		// Start with updates turned on
		mUpdatesRequested = true;

		/*
		 * Create a new location client, using the enclosing class to handle
		 * callbacks.
		 */
		mLocationClient = new LocationClient(this, this, this);
		/* END LOCATION UPDATES */
		spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.spinner_list, android.R.layout.simple_spinner_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		// Save the current setting for updates
		mEditor.putBoolean("KEY_UPDATES_ON", mUpdatesRequested);
		mEditor.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mLocationClient.connect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		/*
		 * Get any previous setting for location updates Gets "false" if an
		 * error occurs
		 */
		if (mPrefs.contains("KEY_UPDATES_ON")) {
			mUpdatesRequested = mPrefs.getBoolean("KEY_UPDATES_ON", false);
		}
		// Otherwise, turn off location updates
		else {
			mEditor.putBoolean("KEY_UPDATES_ON", false);
			mEditor.commit();
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i,
			long l) {
		Log.d("AAAAAAA", " entered on item selected ");
		TextView myText = (TextView) view;
		String selection = (String) myText.getText();
		Log.d("AAAAAAAAAAAAAAAAAAAAAAAAAAAAA", selection);
		if (selection.equals("Logout")) {
			goToLogin();
		} else if (selection.equals("Active Place-Its")) {
			listPlaceIt(view);
		} else if (selection.equals("Inactive Place-its")) {
			listInactivePlaceIt(view);
		} else if (selection.equals("Create Categorical PlaceIts")) {
			CreateCategoricalActivity(view);
		}

	}

	private void CreateCategoricalActivity(View view) {
		Intent intent = new Intent(this, CategoricalActivity.class);
		intent.putExtra("com.example.placeits.user", username);
		startActivity(intent);
	}

	@Override
	public void onMapClick(LatLng position) {
		final LatLng pos = position;
		final double lat = position.latitude;
		final double lng = position.longitude;
		final String category = "none";

		// Prompt user to input Place-it information
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("New Place-It");
		alert.setMessage("Please enter a Place-It Title:");

		// Set layout for entering new Place-it data
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);

		// Set an EditText view to edit Place-it title
		final EditText title = new EditText(this);
		title.setHint("Title");
		layout.addView(title);

		// Set an EditText view to edit Place-it description
		final EditText description = new EditText(this);
		description.setHint("Description");
		layout.addView(description);

		alert.setView(layout);

		// Create button to confirm Place-it creation
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {

				String checkTitle = title.getText().toString();

				// Do not add place-it if empty title
				if (checkTitle.equals("") || checkTitle.equals(null)) {
					Toast.makeText(MainActivity.this,
							"Place-It not added, please enter a title!",
							Toast.LENGTH_SHORT).show();
					return;
				}

				// Create new Place-it, add to array of place-its
				int activeStatus = 1;
				String titleString = title.getText().toString();
				String desString = description.getText().toString();
				placeIt = new PlaceIt(mMap, pos, titleString, desString,
						activeStatus);

				// Stores the created placeit's data to a NameValuePair array
				// for posting
				placeItData = new ArrayList<NameValuePair>();
				placeItData.add(new BasicNameValuePair("user", username));
				placeItData.add(new BasicNameValuePair("title", titleString));
				placeItData.add(new BasicNameValuePair("desc", desString));
				placeItData.add(new BasicNameValuePair("status", String
						.valueOf(activeStatus)));
				placeItData.add(new BasicNameValuePair("lat", String
						.valueOf(lat)));
				placeItData.add(new BasicNameValuePair("lng", String
						.valueOf(lng)));
				placeItData.add(new BasicNameValuePair("category", category));
				placeItData.add(new BasicNameValuePair("action", "put"));
				// Call to posting thread
				postdata();
				Toast.makeText(MainActivity.this, username, Toast.LENGTH_SHORT)
						.show();

				Toast.makeText(MainActivity.this, "Place-it added!",
						Toast.LENGTH_SHORT).show();
			}

		});

		// Create a cancel button
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Toast.makeText(MainActivity.this, "Canceled",
								Toast.LENGTH_SHORT).show();
					}
				});
		alert.show();

	}

	@Override
	public void onLocationChanged(Location location) {
		// Report to the UI that the location was updated
		mLatLong.setText("Lat: " + location.getLatitude() + " Long: "
				+ location.getLongitude());
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
				location.getLatitude(), location.getLongitude()), 18));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_LOGIN:
			// you just got back from LoginActivity - deal with resultCode
			// use data.getExtra(...) to retrieve the returned data
			username = data.getStringExtra("com.example.placeits.user");
			// PreferenceManager.getDefaultSharedPreferences(this).edit().putString("User",
			// username).commit();
			retrievePlaceIts();
			Toast.makeText(this, "Logged in as " + username, Toast.LENGTH_LONG)
					.show();
			// this.recreate();
			break;
		}
	}

	/*
	 * Called by Location Services when the request to connect the client
	 * finishes successfully. At this point, you can request the current
	 * location or start periodic updates
	 */
	@Override
	public void onConnected(Bundle dataBundle) {
		// Display the connection status
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
		// If already requested, start periodic updates
		if (mUpdatesRequested) {
			mLocationClient.requestLocationUpdates(mLocationRequest, this);
		}
	}

	/*
	 * Called by Location Services if the connection to the location client
	 * drops because of an error.
	 */
	@Override
	public void onDisconnected() {
		// Display the connection status
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		/*
		 * Google Play services can resolve some errors it detects. If the error
		 * has a resolution, try sending an Intent to start a Google Play
		 * services activity that can resolve error.
		 */
		if (connectionResult.hasResolution()) {
			try {
				// Start an Activity that tries to resolve the error
				connectionResult.startResolutionForResult(this,
						CONNECTION_FAILURE_RESOLUTION_REQUEST);
				/*
				 * Thrown if Google Play services canceled the original
				 * PendingIntent
				 */
			} catch (IntentSender.SendIntentException e) {
				// Log the error
				e.printStackTrace();
			}
		} else {
			/*
			 * If no resolution is available, display a dialog to the user with
			 * the error.
			 */
			Toast.makeText(this, "FAILURE!", Toast.LENGTH_LONG).show();
		}
	}

	/* EXTERNAL METHODS */

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			mMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
				// The Map is verified. It is now safe to manipulate the map.
			}
		}
	}

	public void goToLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivityForResult(intent, REQUEST_CODE_LOGIN);
	}

	public void listPlaceIt(View view) {
		Intent intent = new Intent(this, PlaceItList.class);
		intent.putExtra("com.example.placeits.user", username);
		startActivity(intent);
	}

	public void listInactivePlaceIt(View view) {
		Intent intent = new Intent(this, InactivePlaceItList.class);
		intent.putExtra("com.example.placeits.user", username);
		startActivity(intent);
	}

	private void postdata() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"Posting Data...", "Please wait...", false);
		Thread t = new Thread() {

			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(
						"http://placeitsserver.appspot.com/placeit");

				try {
					post.setEntity(new UrlEncodedFormEntity(placeItData));
					HttpResponse response = client.execute(post);

					BufferedReader rd = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					String line = "";
					while ((line = rd.readLine()) != null) {
						Log.d("MainActivity", line);
					}

				} catch (IOException e) {
					Log.d("MainActivty",
							"IOException while trying to conect to GAE");
				}
				dialog.dismiss();
			}
		};

		t.start();
		dialog.show();
	}

	private void retrievePlaceIts() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"Retrieving PlaceIts...", "Please wait...", false);

		Thread t = new Thread() {

			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(
						"http://placeitsserver.appspot.com/placeit");

				try {
					HttpResponse response = client.execute(request); // Request
																		// data
																		// from
																		// client
					HttpEntity entity = response.getEntity(); // Creates
																// entities
					String data = EntityUtils.toString(entity); // Parses to one
																// data string
																// object
					Log.d("MainActivity", data);
					JSONObject myjson;

					try {
						myjson = new JSONObject(data);
						JSONArray array = myjson.getJSONArray("data"); // New
																		// JSON
																		// array
						for (int i = 0; i < array.length(); i++) { // Loop to
																	// parse
																	// JSON into
																	// email and
																	// password
																	// lists
							JSONObject obj = array.getJSONObject(i);
							if (obj.get("user").toString().equals(username)) {
								Log.d("Add PlaceIt", "Create place at index "
										+ i);
								titleList.add(obj.getString("title"));
								descList.add(obj.getString("desc"));
								statusList.add(obj.getString("status"));
								latList.add(obj.getString("lat"));
								lngList.add(obj.getString("lng"));
								categoryList.add(obj.getString("category"));

							}
						}
					} catch (JSONException e) {
						Log.d("MainActivty",
								"Exception while trying to parse JSON");
					}

				} catch (IOException e) {
					Log.d("MainActivty",
							"IOException while trying to conect to GAE");
				}
				dialog.dismiss();
			}
		};

		try {
			t.start();
			dialog.show();
			t.join();

			for (int i = 0; i < titleList.size(); i++) {
				placeIt = new PlaceIt(mMap, new LatLng(
						Double.parseDouble(latList.get(i)),
						Double.parseDouble(lngList.get(i))), titleList.get(i),
						descList.get(i), Integer.parseInt(statusList.get(i)));
			}
		} catch (InterruptedException e) {
			Log.d("MainActivity", "Thread was interrupted");
		}
	}

	private void getAllPlaceIts() {
		final ProgressDialog dialog = ProgressDialog.show(this,
				"Retrieving All PlaceIts...", "Please wait...", false);

		Thread t = new Thread() {

			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(
						"http://placeitsserver.appspot.com/placeit");

				try {
					HttpResponse response = client.execute(request); // Request
																		// data
																		// from
																		// client
					HttpEntity entity = response.getEntity(); // Creates
																// entities
					String data = EntityUtils.toString(entity); // Parses to one
																// data string
																// object
					Log.d("MainActivity", data);
					JSONObject myjson;

					try {
						myjson = new JSONObject(data);
						JSONArray array = myjson.getJSONArray("data"); // New
																		// JSON
																		// array
						for (int i = 0; i < array.length(); i++) { // Loop to
																	// parse
																	// JSON into
																	// email and
																	// password
																	// lists
							JSONObject obj = array.getJSONObject(i);
							titleList.add(obj.getString("title"));
							descList.add(obj.getString("desc"));
							statusList.add(obj.getString("status"));
							latList.add(obj.getString("lat"));
							lngList.add(obj.getString("lng"));
							categoryList.add(obj.getString("category"));
						}
					} catch (JSONException e) {
						Log.d("MainActivty",
								"Exception while trying to parse JSON");
					}

				} catch (IOException e) {
					Log.d("MainActivty",
							"IOException while trying to conect to GAE");
				}
				dialog.dismiss();
			}
		};

		try {
			t.start();
			dialog.show();
			t.join();

			for (int i = 0; i < titleList.size(); i++) {
				placeIt = new PlaceIt(mMap, new LatLng(
						Double.parseDouble(latList.get(i)),
						Double.parseDouble(lngList.get(i))), titleList.get(i),
						descList.get(i), Integer.parseInt(statusList.get(i)));
			}
		} catch (InterruptedException e) {
			Log.d("MainActivity", "Thread was interrupted");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}
}
