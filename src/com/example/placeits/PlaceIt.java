package com.example.placeits;

import com.example.placeits.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class PlaceIt {
	private LatLng location;
	private Marker mMarker;
	private int status;
	
	
	/** PlaceIt constructor **/
	public PlaceIt(GoogleMap map, LatLng position, String title, String description, int stat) {
		location = position;
		status = stat;
		
		//Instantiate marker along with Place-it creation
		mMarker = map.addMarker(new MarkerOptions()
			.position(position)
			.title(title)
			.snippet(description)
			.icon(BitmapDescriptorFactory.fromResource(R.drawable.postit)));
	}
	
	/** Set the Place it click listener **/
	public boolean setOnPlaceItClickListener (GoogleMap.OnMarkerClickListener listener, 
			GoogleMap map) {
		
		if (listener == null || map == null) 
			return false;
		
		map.setOnMarkerClickListener(listener);
		return true;
		
	}
	
	
	/** Retrieve location of Place-it **/
	public LatLng getLocation() {
		return this.location;
	}
	
	/** Retrieve Place-it description **/
	public String getDescription() {
		return this.mMarker.getSnippet();
	}
	
	/** Retrieve underlying Marker **/
	public Marker getMarker() {
		return this.mMarker;
	}
	
	/** Retrieve title of Place-it **/
	public String getTitle() {
		return this.mMarker.getTitle();
	}
	

	/** Retrieve PlaceIt status */
	public int getStatus(){
		return this.status;
	}
	
	/** Set Title **/
	public void setTitle(String title) {
		this.mMarker.setTitle(title);
	}
	
	/** Set Status **/
	public void setStatus(int stat) {
		this.status = 0;
	}
	public void showInfoWindow() {
		this.mMarker.showInfoWindow();
	}
	
}
