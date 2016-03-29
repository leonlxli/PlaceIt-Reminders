package com.example.placeits;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	/**
	 * Permanent string values and URIs
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";
	public static final String TAG = "LoginActivity";
	private static final String USERNAME_URI = "http://placeitsserver.appspot.com/username";
	private static final String PASSWORD_URI = "http://placeitsserver.appspot.com/password";
	private static final String LOGIN = "login";
	private static final String REGISTER = "register";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	// Error flag to display the correct message
	private int error;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		error = 0;

		// Set up the login form and UI
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id,
					KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin(LOGIN);
					return true;
				}
				return false;
			}
		});
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		// Registers login button
		findViewById(R.id.login_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin(LOGIN);
					}
				});
		
		// Registers register button
		findViewById(R.id.register_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin(REGISTER);
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin(String status) {
		// Asynctask
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			
			// Login button executes asynctask with login params
			if(status.equals(LOGIN)) {
				mAuthTask.execute(LOGIN, PASSWORD_URI, USERNAME_URI);
			}
			// Register button executes asynctask with register params
			else if(status.equals(REGISTER)) {
				mAuthTask.execute(REGISTER, PASSWORD_URI, USERNAME_URI);
			}
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Boolean> {
		String username;
		
		@Override
		protected Boolean doInBackground(String... str) {
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(str[1]);					// Password requester
			HttpPost postPassword = new HttpPost(str[1]);			// Password poster
			HttpPost postUsername = new HttpPost(str[2]);			// Username poster
			List<String> emailList = new ArrayList<String>();
			List<String> passwordList = new ArrayList<String>();
			String[] emailArray;
			String[] passwordArray;
			
			try {
				HttpResponse requestResponse = client.execute(request);	// Request data from client
				HttpEntity entity = requestResponse.getEntity();		// Creates entities
				String data = EntityUtils.toString(entity);				// Parses to one data string object
				Log.d(TAG, data);
				JSONObject myjson;
			
				try {
					myjson = new JSONObject(data);							
					JSONArray array = myjson.getJSONArray("data");		// New JSON array
					for (int i = 0; i < array.length(); i++) {			// Loop to parse JSON into email and password lists
						JSONObject obj = array.getJSONObject(i);
						emailList.add(obj.get("username").toString());
						passwordList.add(obj.get("name").toString());
					}
					emailArray = new String[emailList.size()];
					passwordArray = new String[passwordList.size()];
					emailList.toArray((String[])emailArray);			// Email list to email array
					passwordList.toArray((String[])passwordArray);		// Password list to password array
					
					/* Case when async executed from login button */
					if(str[0] == LOGIN) {
						/* Compares data from email array to inputed email */
						for(int i = 0; i < emailArray.length; i++) {
							if (emailArray[i].equals(mEmail)) {
								/* If account exists, return true if the password matches */
								if(passwordArray[i].equals(mPassword)) {
									username = mEmail;
									return true;
								}
								else {
									/* Incorrect password, error */
									error = 1;
									return false;
								}
							}
						}
					}
					/* Case when async executed from register button */
					if(str[0] == REGISTER) {
						/* Searches for duplicate emails */
						for(int i = 0; i < emailArray.length; i++) {
							if (emailArray[i].equals(mEmail)) {
								/* Account exists, error */
								error = 2;
								return false;
							}
						}
						try {
							username = mEmail;
							/* Data to be stored into the username entity */
							List<NameValuePair> usernameData = new ArrayList<NameValuePair>();
							usernameData.add(new BasicNameValuePair("name", mEmailView.getText().toString()));
							usernameData.add(new BasicNameValuePair("action", "put"));
						    postUsername.setEntity(new UrlEncodedFormEntity(usernameData));
						    /* Posts username data onto app engine */
						    HttpResponse usernameResponse = client.execute(postUsername);
						    BufferedReader rd = new BufferedReader(new InputStreamReader(usernameResponse.getEntity().getContent()));
						    String line = "";
						    while ((line = rd.readLine()) != null) {
						    	Log.d(TAG, line);
						    }
							
						    /* Data to be stored into the password entity */
							List<NameValuePair> passwordData = new ArrayList<NameValuePair>();
							passwordData.add(new BasicNameValuePair("name", mPasswordView.getText().toString()));
							passwordData.add(new BasicNameValuePair("username", mEmailView.getText().toString()));
							passwordData.add(new BasicNameValuePair("action", "put"));
						    postPassword.setEntity(new UrlEncodedFormEntity(passwordData));
						    /* Posts password data onto app engine */
						    HttpResponse passwordResponse = client.execute(postPassword);
						    rd = new BufferedReader(new InputStreamReader(passwordResponse.getEntity().getContent()));
						    while ((line = rd.readLine()) != null) {
						    	Log.d(TAG, line);
						    }
						    /* Account created, return to application */
						    return true;
						} 
						catch (IOException e) {
							Log.d(TAG, "IOException while trying to conect to GAE");
						}
					}
				}
				catch (JSONException e) {
					Log.d(TAG, "Error in parsing JSON");
				}
			}
			catch (ClientProtocolException e) {
				Log.d(TAG, "ClientProtocolException while trying to connect to GAE");
			} 
			catch (IOException e) {
				Log.d(TAG, "IOException while trying to connect to GAE");
			}
			/* Shouldn't reach here: default error */
			error = 5;
			return false;
		}
			
		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;		// Resets asynctask
			showProgress(false);	// Stops loading bar

			if (success) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("com.example.placeits.user", username);  // put data that you want returned to activity A
				setResult(Activity.RESULT_OK, resultIntent);
				finish();			// Login successful, return to application */
			}  
			else {
				if(error == 1) {
					mPasswordView.setError(getString(R.string.error_incorrect_password));
					mPasswordView.requestFocus();
				}
				else if(error == 2) {
					mEmailView.setError(getString(R.string.error_email_in_use));
					mEmailView.requestFocus();
				}
				else if(error == 5) {
					mEmailView.setError("DEFAULT ERROR");
					mEmailView.requestFocus();
				}
			}
		}


		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
}
