package com.elance.coaches;

public class Coach {
    private int inputDataId;
    private String directory;
    private String mongoID;
    private String firstName;
    private String lastName;
    private String fullName;

    private boolean coachFound;
    private String detailsPageUrl;
    private String email;
    private String biography;
    private byte[] image;
    private String imageExtension;
    private String mimeType;

    public int getInputDataId() {
        return inputDataId;
    }

    public void setInputDataId(int inputDataId) {
        this.inputDataId = inputDataId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getMongoID() {
        return mongoID;
    }

    public void setMongoID(String mongoID) {
        this.mongoID = mongoID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFullNameInverse() {
        return getLastName() + " " + getFirstName();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean isCoachFound() {
        return coachFound;
    }

    public void setIsFound(boolean isFound) {
        this.coachFound = isFound;
    }

    public String getDetailsPageUrl() {
        return detailsPageUrl;
    }

    public void setDetailsPageUrl(String detailsPageUrl) {
        this.detailsPageUrl = detailsPageUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBiography() {
        return biography;
    }

    public String getBiographyEncodeUTF8() {
        String out = null;
        if (biography != null) {
            try {
                out = new String(biography.getBytes("UTF-8"), "ISO-8859-1");
            } catch (java.io.UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return out;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getImageExtension() {
        return imageExtension;
    }

    public void setImageExtension(String imageExtension) {
        this.imageExtension = imageExtension;

        switch (imageExtension.toLowerCase()) {
            case ".png":
                this.mimeType = "image/png";
                break;
            case ".jpg":
                this.mimeType = "image/jpeg";
                break;
            case ".gif":
                this.mimeType = "image/gif";
                break;
            case ".bmp":
                this.mimeType = "image/bmp";
                break;
            case ".jpeg":
                this.mimeType = "image/jpeg";
                break;
        }
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public String toString() {
        return "Coach{" +
                "inputDataId=" + inputDataId +
                ", directory='" + directory + '\'' +
                ", mongoID='" + mongoID + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", fullName='" + fullName + '\'' +
                ", coachFound=" + coachFound +
                ", detailsPageUrl='" + detailsPageUrl + '\'' +
                ", email='" + email + '\'' +
                ", biography='" + biography + '\'' +
                ", imageExtension='" + imageExtension + '\'' +
                '}';
    }
}
