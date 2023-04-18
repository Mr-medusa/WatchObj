package red.medusa.watchobj.example;


import red.medusa.watchobj.example.pet.Pet;

import java.util.*;

public class User<PETS extends List<? super Pet>> {
    private String watchName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private int age;
    private Date birthday;
    private String[] favorite;
    private String[] favoriteWithNull;
    private Address[] addressArr;
    private PETS pets;
    private Map<String,String> extraInfoMapWithNull;
    private Map<String,Object> extraInfoMap = new HashMap<>();
    private User spouseWithNull;
    private User spouse;

    public User(){
    }


    public String getWatchName() {
        return watchName;
    }

    public User setWatchName(String watchName) {
        this.watchName = watchName;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String[] getFavorite() {
        return favorite;
    }

    public void setFavorite(String[] favorite) {
        this.favorite = favorite;
    }

    public String[] getFavoriteWithNull() {
        return favoriteWithNull;
    }

    public void setFavoriteWithNull(String[] favoriteWithNull) {
        this.favoriteWithNull = favoriteWithNull;
    }

    public Address[] getAddressArr() {
        return addressArr;
    }

    public void setAddressArr(Address[] addressArr) {
        this.addressArr = addressArr;
    }

    public PETS getPets() {
        return pets;
    }

    public void setPets(PETS pets) {
        this.pets = pets;
    }

    public Map<String, String> getExtraInfoMapWithNull() {
        return extraInfoMapWithNull;
    }

    public void setExtraInfoMapWithNull(Map<String, String> extraInfoMapWithNull) {
        this.extraInfoMapWithNull = extraInfoMapWithNull;
    }

    public Map<String, Object> getExtraInfoMap() {
        return extraInfoMap;
    }

    public void setExtraInfoMap(Map<String, Object> extraInfoMap) {
        this.extraInfoMap = extraInfoMap;
    }

    public User getSpouseWithNull() {
        return spouseWithNull;
    }

    public void setSpouseWithNull(User spouseWithNull) {
        this.spouseWithNull = spouseWithNull;
    }

    public User getSpouse() {
        return spouse;
    }

    public void setSpouse(User spouse) {
        this.spouse = spouse;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("watchName='" + watchName + "'")
                .toString();
    }
}








