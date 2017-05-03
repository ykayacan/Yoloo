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
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.OnTextChanged;
import com.airbnb.epoxy.EpoxyModel;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bumptech.glide.Glide;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.repository.group.GroupRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.ui.recyclerview.OnItemClickListener;
import com.yoloo.android.ui.recyclerview.decoration.SpaceItemDecoration;
import com.yoloo.android.util.DisplayUtil;
import com.yoloo.android.util.KeyboardUtil;
import com.yoloo.android.util.ViewUtils;
import io.reactivex.subjects.PublishSubject;
import java.util.List;
import java.util.concurrent.TimeUnit;
import timber.log.Timber;

public class SelectGroupController extends MvpController<SelectGroupView, SelectGroupPresenter>
    implements SelectGroupView, OnItemClickListener<GroupRealm> {

  private final PublishSubject<String> querySubject = PublishSubject.create();

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.recycler_view) RecyclerView rvSelectGroup;

  @BindColor(R.color.primary_dark) int colorPrimaryDark;

  private SelectGroupEpoxyController epoxyController;

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

    querySubject.debounce(400, TimeUnit.MILLISECONDS).subscribe(query -> {
      if (query.length() > 2) {
        getPresenter().searchGroups(query);
      } else {
        getPresenter().loadSubscribedGroups();
      }
    });
  }

  @Override
  protected void onChangeEnded(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeEnded(changeHandler, changeType);
    ViewUtils.setStatusBarColor(getActivity(), colorPrimaryDark);
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
    epoxyController.setData(value);
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
    querySubject.onNext(query.toString());
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    ab.setDisplayHomeAsUpEnabled(true);
  }

  @Override
  public void onItemClick(View v, EpoxyModel<?> model, GroupRealm item) {
    ((Groupable) getTargetController()).onGroupSelected(item);

    getRouter().handleBack();
  }

  private void setupRecyclerview() {
    epoxyController = new SelectGroupEpoxyController(Glide.with(getActivity()));
    epoxyController.setOnItemClickListener(this);

    rvSelectGroup.setLayoutManager(new LinearLayoutManager(getActivity()));
    rvSelectGroup.setItemAnimator(new DefaultItemAnimator());
    rvSelectGroup.setHasFixedSize(true);
    rvSelectGroup.addItemDecoration(
        new SpaceItemDecoration(DisplayUtil.dpToPx(8), SpaceItemDecoration.VERTICAL));
    rvSelectGroup.setAdapter(epoxyController.getAdapter());
  }

  public interface Groupable {
    void onGroupSelected(GroupRealm group);
  }
}
