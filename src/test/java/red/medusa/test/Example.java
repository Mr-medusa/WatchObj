package red.medusa.test;

import red.medusa.watchobj.core.MutableJsonService;
import red.medusa.watchobj.example.Address;
import red.medusa.watchobj.example.AutoUpdateValueBaseTest;
import red.medusa.watchobj.example.User;
import red.medusa.watchobj.example.bean.CollectionBean;
import red.medusa.watchobj.example.bean.ListIntegerTestBean;
import red.medusa.watchobj.example.pet.Cat;
import red.medusa.watchobj.example.pet.Dog;
import red.medusa.watchobj.example.pet.Pet;

import java.util.*;

public class Example extends AutoUpdateValueBaseTest {
    public static Set<Object> cache = new HashSet<>();

    // -javaagent:G:/HWorkspace/WatchObj/target/WatchObj-1.0.jar
    public static void main(String[] args) throws InterruptedException {
        System.setProperty(MutableJsonService.ENABLE_SERVER, "true");
        System.setProperty(MutableJsonService.ENABLE_MUTABLE_JSON_UPDATE_COLLECTION, "true");
        System.setProperty(MutableJsonService.ENABLE_MUTABLE_JSON_SERVER, "true");
        testUserWithAspectJ();
        testCollectionBeanWithLoadTimeWeaving();
        testListIntegerTestBeanWithLoadTimeWeaving();
        Thread.currentThread().join();
    }

    public static void testListIntegerTestBeanWithLoadTimeWeaving() {
        ListIntegerTestBean listIntegerTestBean = new ListIntegerTestBean();
        cache.add(listIntegerTestBean);
    }

    public static void testCollectionBeanWithLoadTimeWeaving() {
        CollectionBean<Integer> a = new CollectionBean<>();
        a.setT(1);
        a.setName("AAA");
        a.getNumbers().add(1);
        a.getNumbers().add(1L);
        a.getNumbers().add(1.25);
        Map<String, Map<String, String>> m1 = new HashMap<>();
        Map<String, String> m2 = new HashMap<>();
        m1.put("M", m2);
        m2.put("CM", "CMV");
        m2.put("2", "CMV");
        a.mapMap = m1;
        cache.add(a);
    }


    public static void testUserWithAspectJ() {
        User<List<? super Pet>> user = new User<>();
        user.setWatchName("Watch-You");
        user.setUsername("李寻欢");
        user.setPassword("9527");
        user.setEmail("ErShouDong@JD.com");
        user.setPhone("110");
        user.setAge(100);
        user.setBirthday(new Date());
        user.setFavorite(new String[]{"吃饭", "睡觉", "打扑克"});
        user.setAddressArr(new Address[]{
                new Address().setCity("Beijing").setCountry("zhonggou"),
                new Address().setCity("LA.").setCountry("United States")
        });
        List<? super Pet> pets = new ArrayList<>();
        pets.add(new Cat().setColor("black").setAge(200).setName("九尾猫妖"));
        pets.add(new Dog().setWeight(300.2).setAge(100).setName("雪山飞狐"));
        user.setPets(pets);
        Map<String, Object> extraInfoMap = new HashMap<>();
        extraInfoMap.put("id", "9527");
        HashMap<String, String> stringStringHashMap = new HashMap<>();
        stringStringHashMap.put("name", "特斯拉");
        stringStringHashMap.put("output volume", "2.4L");
        extraInfoMap.put("cars", stringStringHashMap);
        user.setExtraInfoMap(extraInfoMap);
        user.setSpouse(new User().setWatchName("watch-wife").setUsername("Barbara"));
        cache.add(user);

    }

}
