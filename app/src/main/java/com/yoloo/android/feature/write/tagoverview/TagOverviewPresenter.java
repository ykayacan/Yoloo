package com.yoloo.android.feature.write.tagoverview;

import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;
import timber.log.Timber;

public class TagOverviewPresenter extends MvpPresenter<TagOverviewView> {

  @Override public void onAttachView(TagOverviewView view) {
    super.onAttachView(view);
    loadRecommendedTags();
  }

  private void loadRecommendedTags() {
    Disposable d = Observable.just(tags())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tags -> getView().onRecommendedTagsLoaded(tags), Timber::e);

    getDisposable().add(d);
  }

  public void recommendTags(String query) {
    Disposable d = Observable.just(tags())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tags -> getView().onNewRecommendedTagsLoaded(tags), Timber::e);

    getDisposable().add(d);
  }

  private static List<TagRealm> tags() {
    TagRealm t1 = new TagRealm();
    t1.setName("Accommodation");

    TagRealm t2 = new TagRealm();
    t2.setName("Culture");

    TagRealm t3 = new TagRealm();
    t3.setName("Food");

    TagRealm t4 = new TagRealm();
    t4.setName("Budget");

    TagRealm t5 = new TagRealm();
    t5.setName("Transportation");

    TagRealm t6 = new TagRealm();
    t6.setName("Passport");

    TagRealm t7 = new TagRealm();
    t7.setName("Internet");

    TagRealm t8 = new TagRealm();
    t8.setName("Sightseeing");

    TagRealm t9 = new TagRealm();
    t9.setName("Safety");

    TagRealm t10 = new TagRealm();
    t10.setName("Events");

    TagRealm t11 = new TagRealm();
    t11.setName("Nightlife");

    TagRealm t12 = new TagRealm();
    t12.setName("Travelmate");

    List<TagRealm> list = new ArrayList<>();
    list.add(t1);
    list.add(t2);
    list.add(t3);
    list.add(t4);
    list.add(t5);
    list.add(t6);
    list.add(t7);
    list.add(t8);
    list.add(t9);
    list.add(t10);
    list.add(t11);
    list.add(t12);

    return list;
  }
}
