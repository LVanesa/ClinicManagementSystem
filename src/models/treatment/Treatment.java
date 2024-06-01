package models.treatment;

public class Treatment {
    private TreatmentCategory category;
    private String name;
    private double price;

    public Treatment(TreatmentCategory category, String name, double price) {
        this.category = category;
        this.name = name;
        this.price = price;
    }

    public TreatmentCategory getCategory() {
        return category;
    }

    public void setCategory(TreatmentCategory category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString(){
        return "Treatment Name: " + getName() +
                "\nCategory: " + getCategory().toString() +
                "\nPrice: " + getPrice() + " $\n";
    }
}
