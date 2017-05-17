package com.yoloo.android.feature.group.taglist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.yoloo.android.R;
import com.yoloo.android.data.db.TagRealm;
import com.yoloo.android.data.repository.group.GroupRepository;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.animator.SlideInItemAnimator;
import com.yoloo.android.util.BundleBuilder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import timber.log.Timber;

public class TagListController extends BaseController implements OnItemClickListener<TagRealm> {

  private static final String KEY_GROUP_ID = "GROUP_ID";

  @BindView(R.id.recycler_view) RecyclerView rvTagList;

  private TagListEpoxyController epoxyController;

  private Disposable disposable;

  public TagListController(Bundle args) {
    super(args);
  }

  public static TagListController create(@NonNull String groupId) {
    Bundle bundle = new BundleBuilder().putString(KEY_GROUP_ID, groupId).build();

    return new TagListController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_tag_list, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupRecyclerview();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    final String groupId = getArgs().getString(KEY_GROUP_ID);

    GroupRepository repository = GroupRepositoryProvider.getRepository();

    disposable = repository
        .listGroupTags(groupId, null, 100)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tagNames -> epoxyController.setData(tagNames.getData()), Timber::e);
  }

  @Override
  protected void onDetach(@NonNull View view) {
    super.onDetach(view);
    if (!disposable.isDisposed()) {
      disposable.dispose();
    }
  }

  @Override public void onItemClick(View v, TagRealm item) {

  }

  private void setupRecyclerview() {
    epoxyController = new TagListEpoxyController();
    epoxyController.setOnItemClickListener(this);

    rvTagList.setHasFixedSize(true);

    FlexboxLayoutManager layoutManager = new FlexboxLayoutManager();
    layoutManager.setFlexWrap(FlexWrap.WRAP);
    layoutManager.setFlexDirection(FlexDirection.ROW);

    rvTagList.setLayoutManager(layoutManager);
    rvTagList.setItemAnimator(new SlideInItemAnimator());
    rvTagList.setAdapter(epoxyController.getAdapter());
  }
}
