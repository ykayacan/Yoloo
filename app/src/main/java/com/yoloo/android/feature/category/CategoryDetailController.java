package com.yoloo.android.feature.category;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.VerticalChangeHandler;
import com.yoloo.android.R;
import com.yoloo.android.data.model.CategoryRealm;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.feature.feed.global.FeedGlobalController;

public class CategoryDetailController extends BaseController implements
    CategoryController.OnCategoryClickListener {

  @BindView(R.id.toolbar_catalog) Toolbar toolbar;
  @BindView(R.id.category_container) ViewGroup childContainer;

  public CategoryDetailController() {
    setRetainViewMode(RetainViewMode.RETAIN_DETACH);
    setHasOptionsMenu(true);
  }

  public static CategoryDetailController create() {
    return new CategoryDetailController();
  }

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_category_detail, container, false);
  }

  @Override protected void onViewBound(@NonNull View view) {
    setupToolbar();

    final Router childRouter = getChildRouter(childContainer);

    if (!childRouter.hasRootController()) {
      CategoryController controller = CategoryController.create(0);
      controller.setOnCategoryClickListener(this);
      childRouter.setRoot(RouterTransaction.with(controller));
    }
  }

  @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == android.R.id.home) {
      getRouter().handleBack();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override public void onCategoryClick(View v, CategoryRealm category) {
    getRouter().pushController(
        RouterTransaction.with(FeedGlobalController.ofCategory(category.getName()))
            .pushChangeHandler(new VerticalChangeHandler())
            .popChangeHandler(new VerticalChangeHandler()));
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);

    // addPostToBeginning back arrow to toolbar
    final ActionBar ab = getSupportActionBar();
    if (ab != null) {
      ab.setTitle(R.string.label_main_catalog_toolbar_title);
      ab.setDisplayHomeAsUpEnabled(true);
      ab.setDisplayShowHomeEnabled(true);
    }
  }
}
