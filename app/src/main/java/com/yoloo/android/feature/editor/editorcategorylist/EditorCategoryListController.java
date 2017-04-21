package com.yoloo.android.feature.editor.editorcategorylist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.HorizontalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.GroupRealm;
import com.yoloo.android.data.model.PostRealm;
import com.yoloo.android.data.repository.post.PostRepositoryProvider;
import com.yoloo.android.data.repository.user.UserRepositoryProvider;
import com.yoloo.android.feature.category.GroupOverviewController;
import com.yoloo.android.feature.editor.EditorType;
import com.yoloo.android.feature.editor.editor.EditorController;
import com.yoloo.android.framework.MvpController;
import com.yoloo.android.util.BundleBuilder;
import com.yoloo.android.util.ControllerUtil;
import java.util.Collections;
import java.util.List;

public class EditorCategoryListController
    extends MvpController<EditorCategoryListView, EditorCategoryListPresenter>
    implements EditorCategoryListView, GroupOverviewController.SelectedCategoriesListener {

  private static final String KEY_EDITOR_TYPE = "EDITOR_TYPE";

  @BindView(R.id.category_container) ViewGroup childContainer;
  @BindView(R.id.toolbar_catalog) Toolbar toolbar;

  private PostRealm draft;

  private int editorType;

  private List<GroupRealm> selectedCategories = Collections.emptyList();

  public EditorCategoryListController(@Nullable Bundle args) {
    super(args);
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setHasOptionsMenu(true);
  }

  public static EditorCategoryListController create(@EditorType int editorType) {
    final Bundle bundle = new BundleBuilder().putInt(KEY_EDITOR_TYPE, editorType).build();

    return new EditorCategoryListController(bundle);
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_category_detail, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);

    editorType = getArgs().getInt(KEY_EDITOR_TYPE);

    setupToolbar();

    ControllerUtil.preventDefaultBackPressAction(view, this::showDiscardDraftDialog);

    final Router childRouter = getChildRouter(childContainer);

    if (!childRouter.hasRootController()) {
      GroupOverviewController controller = GroupOverviewController.create(1);
      controller.setSelectedCategoriesListener(this);
      childRouter.setRoot(RouterTransaction.with(controller));
    }
  }

  @Override
  protected void onChangeStarted(@NonNull ControllerChangeHandler changeHandler,
      @NonNull ControllerChangeType changeType) {
    super.onChangeStarted(changeHandler, changeType);
    if (changeType.equals(ControllerChangeType.POP_ENTER)) {
      getPresenter().loadDraft();
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.menu_catalog, menu);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    switch (itemId) {
      case android.R.id.home:
        showDiscardDraftDialog();
        return false;
      case R.id.action_next:
        updateDraft();
        return false;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @NonNull
  @Override
  public EditorCategoryListPresenter createPresenter() {
    return new EditorCategoryListPresenter(PostRepositoryProvider.getRepository(),
        UserRepositoryProvider.getRepository());
  }

  @Override
  public void onDraftLoaded(PostRealm draft) {
    this.draft = draft;
  }

  @Override
  public void onDraftSaved() {
    getRouter().pushController(RouterTransaction
        .with(EditorController.create(editorType))
        .pushChangeHandler(new HorizontalChangeHandler())
        .popChangeHandler(new HorizontalChangeHandler()));
  }

  @Override
  public void selectedCategories(List<GroupRealm> selected) {
    selectedCategories = selected;
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_catalog_toolbar_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setHomeAsUpIndicator(
          AppCompatResources.getDrawable(getActivity(), R.drawable.ic_close_white_24dp));
      ab.setDisplayShowHomeEnabled(true);
    }
  }

  private void showDiscardDraftDialog() {
    new AlertDialog.Builder(getActivity())
        .setTitle(R.string.action_catalog_discard_draft)
        .setCancelable(false)
        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
          getPresenter().deleteDraft();
          getRouter().popToRoot();
        })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void updateDraft() {
    if (selectedCategories.isEmpty()) {
      Snackbar
          .make(getView(), R.string.error_catalog_select_category, Snackbar.LENGTH_SHORT)
          .show();
    } else {
      /*Stream.of(selectedCategories).forEach(category -> draft.addGroup(category));
      getPresenter().updateDraft(draft);*/
    }
  }
}
