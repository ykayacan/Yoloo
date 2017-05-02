package com.yoloo.android.feature.profile.buybounty;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.OnClick;
import com.yoloo.android.R;
import com.yoloo.android.YolooApp;
import com.yoloo.android.feature.base.BaseController;
import com.yoloo.android.ui.widget.BountyView;
import io.reactivex.disposables.CompositeDisposable;
import javax.annotation.Nonnull;
import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.EmptyRequestListener;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.ProductTypes;
import org.solovyev.android.checkout.Purchase;

public class BuyBountyController extends BaseController {

  private final ActivityCheckout checkout =
      Checkout.forActivity(getActivity(), YolooApp.getInstance().getBilling());

  @BindView(R.id.toolbar) Toolbar toolbar;
  @BindView(R.id.bountyView_1) BountyView bountyView1;
  @BindView(R.id.bountyView_2) BountyView bountyView2;
  @BindView(R.id.bountyView_3) BountyView bountyView3;
  @BindView(R.id.bountyView_4) BountyView bountyView4;
  @BindView(R.id.bountyView_5) BountyView bountyView5;
  @BindView(R.id.bountyView_6) BountyView bountyView6;

  private CompositeDisposable disposable;
  private Inventory inventory;

  @Override
  protected View inflateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container) {
    return inflater.inflate(R.layout.controller_buy_bounty, container, false);
  }

  @Override
  protected void onViewBound(@NonNull View view) {
    super.onViewBound(view);
    setupToolbar();

    checkout.start();

    checkout.createPurchaseFlow(new PurchaseListener());

    inventory = checkout.makeInventory();
    inventory.load(
        Inventory.Request.create().loadAllPurchases().loadSkus(ProductTypes.IN_APP, "sku_01"),
        new InventoryCallback());
  }

  @Override
  protected void onDestroy() {
    checkout.stop();
    super.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    checkout.onActivityResult(requestCode, resultCode, data);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setTitle("Buy Bounty");
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
  }

  @OnClick(R.id.bountyView_1)
  void buyBounty1() {
    checkout.whenReady(new Checkout.EmptyListener() {
      @Override
      public void onReady(@Nonnull BillingRequests requests) {
        requests.purchase(ProductTypes.IN_APP, "sku_01", null, checkout.getPurchaseFlow());
      }
    });
  }

  private static class PurchaseListener extends EmptyRequestListener<Purchase> {

  }

  private static class InventoryCallback implements Inventory.Callback {
    @Override
    public void onLoaded(@NonNull Inventory.Products products) {
      // your code here
    }
  }
}
