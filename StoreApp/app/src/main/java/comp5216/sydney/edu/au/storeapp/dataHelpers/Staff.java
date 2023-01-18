package comp5216.sydney.edu.au.storeapp.dataHelpers;

public class Staff {
    private String name;
    private String lastname;
    private String email;
    private String profileImage;
    private String documentId;
    private String userStoresId;

    public Staff(String name, String lastname, String email, String profileImage) {
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.profileImage = profileImage;
        this.userStoresId = null;
        this.documentId = null;
    }

    public Staff() {
    }

    public String getUserStoresId() {
        return userStoresId;
    }

    public void setUserStoresId(String userStoresId) {
        this.userStoresId = userStoresId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getLastName() {
        return lastname;
    }

    public void setLastName(String lastname) {
        this.lastname = lastname;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
