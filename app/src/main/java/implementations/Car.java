package implementations;

public class Car {
    private String model = "";
    private double mileage = 0.0;
    private long databaseId = -1;

    public Car() {

    }

    public Car(String model, double mileage) {
        this.setModel(model);
        this.setMileage(mileage);
    }

    public Car(long databaseId, String model, double mileage) {
        this(model, mileage);
        this.setDatabaseId(databaseId);
    }

    // region getter & setter

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getMileage() {
        return mileage;
    }

    public void setMileage(double mileage) {
        this.mileage = mileage;
    }

    public long getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(long databaseId) {
        this.databaseId = databaseId;
    }

    // endregion
}