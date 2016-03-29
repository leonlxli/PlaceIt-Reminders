package com.example.placeits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class CategoricalActivity extends Activity implements
		AdapterView.OnItemSelectedListener {
	public static final String TAG = "CategoricalActivity";
	private String username;
	private Spinner spinner;
	private EditText nameBox;
	private EditText descriptionBox;
	String category;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			username = extras.getString("com.example.placeits.user");
		}
		setContentView(R.layout.activity_categorical);
		spinner = (Spinner) findViewById(R.id.item_spinner);
		ArrayAdapter adapter = ArrayAdapter.createFromResource(this,
				R.array.category_list, android.R.layout.simple_spinner_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		nameBox = (EditText) findViewById(R.id.item_name);
		descriptionBox = (EditText) findViewById(R.id.item_desc);

		 Button btn = (Button) findViewById(R.id.register_item);
		
		 btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				 register(v);
				
			}
		 });
	}

	public void goBack(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("com.example.placeits.user", username);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.categorical, menu);
		return true;
	}

	void register(View view) {
		final String name = nameBox.getText().toString();
		final String description = descriptionBox.getText().toString();

		final ProgressDialog dialog = ProgressDialog.show(this,
				"Posting Data...", "Please wait...", false);
		Thread t = new Thread() {

			public void run() {
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost("http://placeitsserver.appspot.com/placeit");

				try {
					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
					nameValuePairs.add(new BasicNameValuePair("user", username));
					nameValuePairs.add(new BasicNameValuePair("title", name));
						Log.d(TAG, name);
					nameValuePairs.add(new BasicNameValuePair("desc", description));
						Log.d(TAG, description);
					nameValuePairs.add(new BasicNameValuePair("status", String.valueOf(1)));
					nameValuePairs.add(new BasicNameValuePair("lat", "none"));
					nameValuePairs.add(new BasicNameValuePair("lng", "none"));
					nameValuePairs.add(new BasicNameValuePair("category",
							spinner.getSelectedItem().toString()));
					nameValuePairs.add(new BasicNameValuePair("action", "put"));
					Log.d(TAG, spinner.getSelectedItem().toString());
					
					post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = client.execute(post);
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent()));
					String line = ""; 
					while ((line = rd.readLine()) != null) {
						Log.d(TAG, line);
					}

				} catch (IOException e) {
					Log.d(TAG, "IOException while trying to connect");
				}
				dialog.dismiss();
			}
		};

		t.start();
		dialog.show();
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int i,
			long l) {
		TextView myText = (TextView) view;
		category = (String) myText.getText();

		// TODO Auto-generated method stub

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
