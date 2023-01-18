package comp5216.sydney.edu.au.storeapp.dataHelpers;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserDB implements Parcelable {
	private String id;
	private String name;
	private String lastname;
	private String email;
	private String profileImage;
	private Date birthdate;
	private String currentStoreId;
	private String currentStoreType;

	public UserDB(String id, String name, String lastname, String email, String profileImage,
	              Date birthdate) {
		this.id = id;
		this.name = name;
		this.lastname = lastname;
		this.email = email;
		this.profileImage = profileImage;
		this.birthdate = birthdate;
		currentStoreId = null;
		currentStoreType = null;
	}

	public UserDB(FirebaseUser currentUser, DocumentSnapshot snapshot) {
		this.id = currentUser.getUid();
		this.name = snapshot.get("name", String.class);
		this.lastname = snapshot.get("lastname", String.class);
		this.email = snapshot.get("email", String.class);
		this.profileImage = snapshot.get("profileImage", String.class);
		this.birthdate =
			Objects.requireNonNull(snapshot.get("birthdate", Timestamp.class)).toDate();
		this.currentStoreId =
			Objects.requireNonNull(snapshot.getDocumentReference("currentStore")).getId();
		this.currentStoreType = snapshot.getString("currentStoreType");
	}

	protected UserDB(Parcel in) {
		id = in.readString();
		name = in.readString();
		lastname = in.readString();
		email = in.readString();
		profileImage = in.readString();
		birthdate = new Date(in.readLong());
		currentStoreId = in.readString();
		currentStoreType = in.readString();
	}

	public static final Creator<UserDB> CREATOR = new Creator<UserDB>() {
		@Override
		public UserDB createFromParcel(Parcel in) {
			return new UserDB(in);
		}

		@Override
		public UserDB[] newArray(int size) {
			return new UserDB[size];
		}
	};

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

	public Date getBirthday() {
		return birthdate;
	}

	public void setBirthday(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getBirthdate() {
		return birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
	}

	public String getCurrentStoreId() {
		return currentStoreId;
	}

	public void setCurrentStoreId(String currentStoreId) {
		this.currentStoreId = currentStoreId;
	}

	public String getCurrentStoreType() {
		return currentStoreType;
	}

	public void setCurrentStoreType(String currentStoreType) {
		this.currentStoreType = currentStoreType;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(id);
		parcel.writeString(name);
		parcel.writeString(lastname);
		parcel.writeString(email);
		parcel.writeString(profileImage);
		parcel.writeLong(birthdate.getTime());
		parcel.writeString(currentStoreId);
		parcel.writeString(currentStoreType);
	}

	public Map<String, Object> getUserForDB() {
		Map<String, Object> aux = new HashMap<>();
		if (name != null)
			aux.put("name", name);
		if (lastname != null)
			aux.put("lastname", lastname);
		if (birthdate != null)
			aux.put("birthdate", birthdate);
		return aux;
	}
}
