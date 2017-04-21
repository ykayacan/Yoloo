package com.yoloo.android.feature.auth.selecttype;

import com.yoloo.android.data.repository.travelertype.TravelerTypeRepository;
import com.yoloo.android.framework.MvpPresenter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class SelectTypePresenter extends MvpPresenter<SelectTypeView> {

  private final TravelerTypeRepository travelerTypeRepository;

  public SelectTypePresenter(TravelerTypeRepository travelerTypeRepository) {
    this.travelerTypeRepository = travelerTypeRepository;
  }

  @Override
  public void onAttachView(SelectTypeView view) {
    super.onAttachView(view);
    loadTravelerTypes();
  }

  private void loadTravelerTypes() {
    Disposable d = travelerTypeRepository
        .listTravelerTypes()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(travelerTypes -> getView().onLoaded(travelerTypes),
            throwable -> getView().onError(throwable));

    getDisposable().add(d);
  }
}
