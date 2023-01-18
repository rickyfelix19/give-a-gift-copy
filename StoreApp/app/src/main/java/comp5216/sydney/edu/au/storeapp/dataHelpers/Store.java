package comp5216.sydney.edu.au.storeapp.dataHelpers;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class Store {
	private String name;
	private String category;
	private String imageUri;
	private GeoPoint location;
	private DocumentReference ref;
	private String userRoll;

	public Store(String name, String category, String imageUri, GeoPoint location) {
		this.name = name;
		this.category = category;
		this.imageUri = imageUri;
		this.location = location;
		ref = null;
		userRoll = null;
	}

	public Store() {
		this.name = null;
		this.category = null;
		this.imageUri = null;
		this.location = null;
		ref = null;
		userRoll = null;
	}

	public Map<String, Object> getStoreForDB() {
		Map<String, Object> aux = new HashMap<>();
		if (name != null)
			aux.put("name", name);
		if (category != null)
			aux.put("category", category);
		if (imageUri != null)
			aux.put("imageUri", imageUri);
		if (location != null)
			aux.put("location", location);
		return aux;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getImageUri() {
		return imageUri;
	}

	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}

	public DocumentReference getRef() {
		return ref;
	}

	public void setRef(DocumentReference ref) {
		this.ref = ref;
	}

	public String getUserRoll() {
		return userRoll;
	}

	public void setUserRoll(String userRoll) {
		this.userRoll = userRoll;
	}
}
