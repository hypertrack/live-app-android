package io.hypertrack.sendeta.presenter;

import io.hypertrack.sendeta.interactor.BusinessProfileInteractor;
import io.hypertrack.sendeta.view.BusinessProfileView;

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfilePresenter implements IBusinessProfilePresenter<BusinessProfileView>{

    private BusinessProfileView view;
    private BusinessProfileInteractor businessProfileInteractor;

    @Override
    public void attachView(BusinessProfileView view) {
        this.view = view;
        businessProfileInteractor = new BusinessProfileInteractor();
    }

    @Override
    public void detachView() {
        view = null;
    }

    @Override
    public void attemptVerifyPendingBusinessProfile() {
        businessProfileInteractor.verifyBusinessProfilePendingInvite();
    }
}
