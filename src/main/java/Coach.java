public class Coach {
    private boolean isFound;
    private String directory;
    private String mongoID;
    private String firstName;
    private String lastName;
    private String detailsPageUrl;
    private String email;

    public boolean isFound() {
        return isFound;
    }

    public void setIsFound(boolean isFound) {
        this.isFound = isFound;
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

    @Override
    public String toString() {
        return "Coach{" +
                "isFound=" + isFound +
                ", directory='" + directory + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", detailsPageUrl='" + detailsPageUrl + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
