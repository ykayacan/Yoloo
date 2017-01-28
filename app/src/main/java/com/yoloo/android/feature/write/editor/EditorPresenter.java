package com.yoloo.android.feature.write.editor;

import com.yoloo.android.data.Response;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.model.TagRealm;
import com.yoloo.android.data.repository.post.PostRepository;
import com.yoloo.android.data.repository.tag.TagRepository;
import com.yoloo.android.feature.base.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.List;

public class EditorPresenter extends MvpPresenter<EditorView> {

  public static final int NAV_BACK = -1;
  public static final int NAV_BOUNTY = 1;
  public static final int NAV_POST = 2;

  private final TagRepository tagRepository;
  private final PostRepository postRepository;

  public EditorPresenter(TagRepository tagRepository, PostRepository postRepository) {
    this.tagRepository = tagRepository;
    this.postRepository = postRepository;
  }

  @Override public void onAttachView(EditorView view) {
    super.onAttachView(view);
    loadDraft();
  }

  public void loadDraft() {
    Disposable d = postRepository.getDraft()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(draft -> getView().onDraftLoaded(draft));

    getDisposable().add(d);
  }

  public void loadRecommendedTags() {
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

    getView().onRecommendedTagsLoaded(list);
  }

  public void loadSuggestedTags(String text) {
    Disposable d = tagRepository.list(text, null, 5)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(this::showSuggestedTags, this::showError);

    getDisposable().add(d);
  }

  public void updateDraft(PostRealm draft, int navigation) {
    Disposable d = postRepository.addDraft(draft)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(() -> getView().onDraftSaved(navigation));

    getDisposable().add(d);
  }

  private void showSuggestedTags(Response<List<TagRealm>> response) {
    getView().onSuggestedTagsLoaded(response.getData());
  }

  private void showError(Throwable throwable) {
    getView().onError(throwable);
  }
}
