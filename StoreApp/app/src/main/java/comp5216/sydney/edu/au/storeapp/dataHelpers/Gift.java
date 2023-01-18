package comp5216.sydney.edu.au.storeapp.dataHelpers;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;

public class Gift {

    private String sender;
    private String store;
    private String receiver;
    private DocumentReference ref;
    private String description;
    private Double latitude;
    private Double longitude;

    public Gift(String sender, String store, String receiver,
                DocumentReference ref, String description, Double latitude, Double longitude) {
        this.receiver = receiver;
        this.sender = sender;
        this.store = store;
        this.ref = ref;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Gift() {
        this.receiver = null;
        this.sender = null;
        this.store = null;
        this.ref = null;
        this.description = null;
        this.latitude = null;
        this.longitude = null;
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver () { return receiver;}

    public String getStore () { return store;}

    public void setSender(String sender) { this.sender = sender;}

    public void setReceiver (String receiver) { this.receiver = receiver;}

    public void setStore (String store) { this.store = store;}

    public DocumentReference getRef() {
        return ref;
    }

    public void setRef(DocumentReference ref) {
        this.ref = ref;
    }

    public String getDescription() { return description; }

    public void setDescription (String description) {this.description = description;}

    public Double getLatitude() { return latitude; }

    public void setLatitude (Double latitude) {this.latitude = latitude;}

    public Double getLongitude() { return longitude; }

    public void setLongitude (Double longitude) {this.longitude = longitude;}

    public void setLatLon (GeoPoint location) {
        setLatitude(location.getLatitude());
        setLongitude(location.getLongitude());
    }

}
