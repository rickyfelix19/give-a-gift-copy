package comp5216.sydney.edu.au.customerapp.dataHelpers;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class Friend {
	private String id;
	private String name;
	private String lastname;
	private String profileImage;
	private Date birthdate;
	private GeoPoint location;
	private Boolean isPending;
	private Double latitude;
	private Double longitude;

	public Friend(DocumentSnapshot snapshot, Boolean isPending) {
		id = snapshot.getId();
		name = snapshot.getString("name");
		lastname = snapshot.getString("lastname");
		profileImage = snapshot.getString("profileImage");
		birthdate = Objects.requireNonNull(snapshot.getTimestamp("birthdate")).toDate();
		location = snapshot.getGeoPoint("location");
		if(snapshot.getGeoPoint("location") != null){
			latitude = snapshot.getGeoPoint("location").getLatitude();
			longitude = snapshot.getGeoPoint("location").getLongitude();
		} else {
			latitude = null;
			longitude = null;
		}

		this.isPending = isPending;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getProfileImage() {
		return profileImage;
	}

	public void setProfileImage(String profileImage) {
		this.profileImage = profileImage;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}


	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public Boolean getPending() {
		return isPending;
	}

	public void setPending(Boolean pending) {
		isPending = pending;
	}
	public Double getLatitude(){
		return latitude;
	}

	public Double getLongitude(){
		return longitude;
	}

	@Override
	public String toString() {
		return (name + " " + lastname);
	}
}
