package red.medusa.watchobj.example.pet;


public class Cat extends Pet{
    private String color;

    public String getColor() {
        return color;
    }

    public Cat setColor(String color) {
        this.color = color;
        return this;
    }
}
