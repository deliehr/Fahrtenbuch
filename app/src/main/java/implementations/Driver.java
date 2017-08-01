package implementations;

public class Driver {
    // region local fields
    private String firstName = "";
    private String lastName = "";
    private long databaseId = -1;
    // endregion

    // region init
    public Driver() {

    }

    public Driver(String firstName, String lastName) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }

    public Driver(long databaseId, String firstName, String lastName) {
        this(firstName, lastName);
        this.setDatabaseId(databaseId);
    }
    // endregion

    // region getter & setter
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

    public long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    // endregion
}