package com.yoloo.android.feature.editor.selectgroup;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.OnTextChanged;
import com.airbnb.epoxy.EpoxyModel;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.editor.editor.EditorController2;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.KeyboardUtil;
import io.reactivex.subjects.PublishSubject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class SelectGroupController extends MvpController<SelectGroupView, SelectGroupPresenter>
    implements SelectGroupView, OnItemClickListener<GroupRealm> {

  private final PublishSubject<String> querySubject = PublishSubject.create();

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView rvSelectGroup;

  private SelectGroupAdapter adapter;

  public static SelectGroupController create() {
    return new SelectGroupController();
  }

  @NonNull
  @Override
  public SelectGroupPresenter createPresenter() {
    return new SelectGroupPresenter(UserRepositoryProvider.getRepository(),
        GroupRepositoryProvider.getRepository());
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();
    setupRecyclerview();
  }

  @Override
  protected void onAttach(@NonNull View view) {
    super.onAttach(view);
    getPresenter().loadSubscribedGroups();

    querySubject
        .filter(query -> !query.isEmpty())
        .filter(query -> query.length() > 3)
        .debounce(400, TimeUnit.MILLISECONDS)
        .subscribe(query -> getPresenter().searchGroups(query));
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_select_group, container, false);
  }

  @Override
  public void onLoading(boolean pullToRefresh) {

  }

  @Override
  public void onLoaded(List<GroupRealm> value) {
    adapter.addGroups(value, true);
  }

  @Override
  public void onError(Throwable e) {
    Timber.e(e);
  }

  @Override
  public void onEmpty() {

  }

  @Override
  public boolean handleBack() {
    KeyboardUtil.hideKeyboard(getView());
    return super.handleBack();
  }

  @OnTextChanged(R.id.et_select_group_search)
  void onSearchQuery(CharSequence query) {
    Timber.d("Text: %s", query);
    querySubject.onNext(query.toString());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void onItemClick(View v, EpoxyModel<?> model, GroupRealm item) {
    ((EditorController2) getTargetController()).onGroupSelected(item);

    getRouter().handleBack();
  }

  private void setupRecyclerview() {
    adapter = new SelectGroupAdapter(Glide.with(getActivity()), getActivity());
    adapter.setOnItemClickListener(this);

    rvSelectGroup.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvSelectGroup.setItemAnimator(new DefaultItemAnimator());
    rvSelectGroup.setHasFixedSize(true);
    rvSelectGroup.addItemDecoration(
        new SpaceItemDecoration(DisplayUtil.dpToPx(8), SpaceItemDecoration.VERTICAL));
    rvSelectGroup.setAdapter(adapter);
  }
}
