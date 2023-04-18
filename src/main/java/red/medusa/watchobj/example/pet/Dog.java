package red.medusa.watchobj.example.pet;


public class Dog extends Pet{
    private Double weight;

    public Double getWeight() {
        return weight;
    }

    public Dog setWeight(Double weight) {
        this.weight = weight;
        return this;
    }
}
