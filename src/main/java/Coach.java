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

    public void setBiography(String biography) {
        this.biography = biography;
    }

    @Override
    public String toString() {
        return "Coach{" +
                "coachFound=" + coachFound +
                ", directory='" + directory + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", bio='" + biography + '\'' +
                '}';
    }
}
