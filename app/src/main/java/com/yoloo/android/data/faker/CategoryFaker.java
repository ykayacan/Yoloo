package com.yoloo.android.data.faker;

import com.yoloo.android.data.model.CategoryRealm;
import io.realm.Realm;
import java.util.HashSet;
import java.util.Set;

public class CategoryFaker {

  private static final String THEME = "THEME";
  private static final String DESTINATION = "DESTINATION";

  public static void generate() {
    Realm realm = Realm.getDefaultInstance();

    realm.executeTransactionAsync(tx -> {
      Set<CategoryRealm> list = new HashSet<>();

      CategoryRealm c1 = new CategoryRealm()
          .setId("cat1")
          .setName("Activities")
          .setRank(13.2)
          .setBackgroundUrl(getBackgroundUrl("activities"));

      CategoryRealm c2 = new CategoryRealm()
          .setId("cat2")
          .setName("Adventure")
          .setRank(11.25)
          .setBackgroundUrl(getBackgroundUrl("adventure"));

      CategoryRealm c3 = new CategoryRealm()
          .setId("cat3")
          .setName("Camping")
          .setRank(13.2)
          .setBackgroundUrl(getBackgroundUrl("camping"));

      CategoryRealm c4 = new CategoryRealm()
          .setId("cat4")
          .setName("Culture")
          .setRank(17.232)
          .setBackgroundUrl(getBackgroundUrl("culture"));

      CategoryRealm c5 = new CategoryRealm()
          .setId("cat5")
          .setName("Events")
          .setRank(7.232)
          .setBackgroundUrl(getBackgroundUrl("events"));

      CategoryRealm c6 = new CategoryRealm()
          .setId("cat6")
          .setName("Food & Drink")
          .setRank(7.232)
          .setBackgroundUrl(getBackgroundUrl("food-drink"));

      CategoryRealm c7 = new CategoryRealm()
          .setId("cat7")
          .setName("Nightlife")
          .setRank(7.232)
          .setBackgroundUrl(getBackgroundUrl("nightlife"));

      CategoryRealm c8 = new CategoryRealm()
          .setId("cat8")
          .setName("Solo Travel")
          .setRank(7.232)
          .setBackgroundUrl(getBackgroundUrl("solo-travel"));

      CategoryRealm c9 = new CategoryRealm()
          .setId("cat9")
          .setName("Solo Travel")
          .setRank(7.232)
          .setBackgroundUrl(getBackgroundUrl("solo-travel"));

      CategoryRealm c10 = new CategoryRealm()
          .setId("cat10")
          .setName("Study Abroad")
          .setRank(7.232)
          .setBackgroundUrl(getBackgroundUrl("study-abroad"));

      CategoryRealm c11 = new CategoryRealm()
          .setId("cat11")
          .setName("Tours")
          .setRank(7.232)
          .setBackgroundUrl(getBackgroundUrl("tours"));

      CategoryRealm c12 = new CategoryRealm()
          .setId("cat12")
          .setName("Africa")
          .setRank(12.4)
          .setBackgroundUrl(getBackgroundUrl("africa"));

      CategoryRealm c13 = new CategoryRealm()
          .setId("cat13")
          .setName("America")
          .setRank(10.4877)
          .setBackgroundUrl(getBackgroundUrl("america"));

      CategoryRealm c14 = new CategoryRealm()
          .setId("cat14")
          .setName("Asia")
          .setRank(10.9877)
          .setBackgroundUrl(getBackgroundUrl("asia"));

      CategoryRealm c15 = new CategoryRealm()
          .setId("cat15")
          .setName("Canada")
          .setRank(15.9877)
          .setBackgroundUrl(getBackgroundUrl("canada"));

      CategoryRealm c16 = new CategoryRealm()
          .setId("cat16")
          .setName("Europe")
          .setRank(5.9877)
          .setBackgroundUrl(getBackgroundUrl("europe"));

      CategoryRealm c17 = new CategoryRealm()
          .setId("cat17")
          .setName("Middle East")
          .setRank(5.9877)
          .setBackgroundUrl(getBackgroundUrl("middle-east"));

      CategoryRealm c18 = new CategoryRealm()
          .setId("cat18")
          .setName("South Pacific")
          .setRank(5.9877)
          .setBackgroundUrl(getBackgroundUrl("south-pacific"));

      CategoryRealm c19 = new CategoryRealm()
          .setId("cat19")
          .setName("United Kingdom")
          .setRank(5.9877)
          .setBackgroundUrl(getBackgroundUrl("united-kingdom"));

      list.add(c1);
      list.add(c2);
      list.add(c3);
      list.add(c4);
      list.add(c5);
      list.add(c6);
      list.add(c7);
      list.add(c8);
      list.add(c9);
      list.add(c10);
      list.add(c11);
      list.add(c12);
      list.add(c13);
      list.add(c14);
      list.add(c15);
      list.add(c16);
      list.add(c17);
      list.add(c18);
      list.add(c19);

      tx.insertOrUpdate(list);
    });

    realm.close();
  }

  private static String getBackgroundUrl(String name) {
    return "https://storage.googleapis.com/yoloo-151719.appspot.com/categories/" + name + ".webp";
  }
}