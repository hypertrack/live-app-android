package io.hypertrack.meta.presenter;

import io.hypertrack.meta.interactor.HomeInteractorListener;
import io.hypertrack.meta.view.HomeView;

/**
 * Created by suhas on 25/02/16.
 */
public class HomePresenter implements Presenter<HomeView>, HomeInteractorListener {

    private static final String TAG = HomePresenter.class.getSimpleName();
    private HomeView homeView;

    @Override
    public void attachView(HomeView view) {
        homeView = view;
    }

    @Override
    public void detachView() {
        homeView = null;
    }
}
