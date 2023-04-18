package red.medusa.watchobj.example.pet;

public abstract class Pet{
    private String name;
    private Integer age;

    public String getName() {
        return name;
    }

    public Pet setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getAge() {
        return age;
    }

    public Pet setAge(Integer age) {
        this.age = age;
        return this;
    }
}
