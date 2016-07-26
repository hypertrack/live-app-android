package io.hypertrack.sendeta.presenter;

import io.hypertrack.sendeta.interactor.BusinessProfileInteractor;
import io.hypertrack.sendeta.model.Membership;
import io.hypertrack.sendeta.store.UserStore;
import io.hypertrack.sendeta.store.callback.UserStoreMembershipCallback;
import io.hypertrack.sendeta.view.BusinessProfileView;

/**
 * Created by piyush on 22/07/16.
 */
public class BusinessProfilePresenter implements IBusinessProfilePresenter<BusinessProfileView> {

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

    public void getMembershipForAccountId(int accountId) {
        if (accountId != -1) {
            UserStore.sharedStore.getMembershipForAccountId(accountId, new UserStoreMembershipCallback() {
                @Override
                public void OnSuccess(Membership membership) {

                    if (view != null)
                        view.showGetMembershipSuccess(membership);
                }

                @Override
                public void OnError() {
                    if (view != null)
                        view.showGetMembershipError();
                }
            });
        }
    }

}
