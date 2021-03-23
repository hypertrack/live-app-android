package com.hypertrack.live.auth;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;
import com.hypertrack.live.CognitoClient;
import com.hypertrack.live.LaunchActivity;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.utils.HTTextWatcher;
import com.hypertrack.live.views.SignupInfoPage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class SignUpFragment extends Fragment implements CognitoClient.Callback {


    private static final int PAGES_COUNT = 2;
    private static final int PAGE_USER = 0;
    private static final int PAGE_INFO = 1;


    private ViewPager viewPager;
    private TextView incorrect;
    private Button next;
    private Button accept;
    private TextView agreeToTerms;

    private LoaderDecorator loader;

    private String company = "";
    private String email;
    private String password;
    private Map<String, String> cognitoUserAttributes = new HashMap<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().setTitle(R.string.sign_up_a_new_account);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                onBackPressed();
//            }
//        });

        loader = new LoaderDecorator(getContext());

        viewPager = view.findViewById(R.id.view_pager);
        incorrect = view.findViewById(R.id.incorrect);
        next = view.findViewById(R.id.next);
        accept = view.findViewById(R.id.accept);
        agreeToTerms = view.findViewById(R.id.agree_to_terms_of_service);
        agreeToTerms.setText(Html.fromHtml(getString(R.string.i_agree_to_terms_of_service)));
        agreeToTerms.setMovementMethod(LinkMovementMethod.getInstance());

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK && viewPager.getCurrentItem() > 0) {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
                    return true;
                }
                return false;
            }
        });

        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                viewPager.setVisibility(View.VISIBLE);
                incorrect.setVisibility(View.INVISIBLE);
                switch (viewPager.getCurrentItem()) {
                    case PAGE_USER:
                        accept.setVisibility(View.INVISIBLE);
                        agreeToTerms.setVisibility(View.INVISIBLE);
                        next.setVisibility(View.VISIBLE);
                        break;
                    case PAGE_INFO:
                        accept.setVisibility(View.VISIBLE);
                        agreeToTerms.setVisibility(View.VISIBLE);
                        next.setVisibility(View.INVISIBLE);
                        viewPager.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!cognitoUserAttributes.keySet().containsAll(Arrays.asList(SignupInfoPage.CUSTOM_USE_CASE, SignupInfoPage.CUSTOM_STATE))) {
                    showError(getString(R.string.all_fields_required));
                    return;
                }
                loader.start();
                CognitoClient.getInstance(getContext()).signUp(email, password, cognitoUserAttributes, SignUpFragment.this);
            }
        });

        view.findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((LaunchActivity)getActivity()).addSignInFragment();
            }
        });

    }

    private void nextPage() {

        switch (viewPager.getCurrentItem()) {
            case PAGE_USER:
                if (TextUtils.isEmpty(email)
                        || TextUtils.isEmpty(password)
                        || TextUtils.isEmpty(company)
                ) {
                    showError(getString(R.string.all_fields_required));
                    return;
                }
                cognitoUserAttributes.put(SignupInfoPage.CUSTOM_COMPANY, company);
                break;
            case PAGE_INFO:
                break;
        }
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    private void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
        } else {
            getActivity().onBackPressed();
        }
    }

    private void showError(String msg) {
        incorrect.setText(msg);
        incorrect.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSuccess(CognitoClient mobileClient) {
        if (getActivity() != null) {
            loader.stop();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_frame, new ConfirmFragment(), ConfirmFragment.class.getSimpleName())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onError(String message, Exception e) {
        if (getActivity() != null) {
            loader.stop();

            viewPager.setCurrentItem(PAGE_USER);
            if (e instanceof UsernameExistsException) {
                showError(getString(R.string.an_account_exists));
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private class MyPagerAdapter extends PagerAdapter {

        @NonNull
        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(collection.getContext());
            View view = null;
            switch (position) {
                case PAGE_USER:
                    view = inflater.inflate(R.layout.view_pager_signup_user, collection, false);
                    EditText companyNameEditText = view.findViewById(R.id.company_name);
                    EditText emailAddressEditText = view.findViewById(R.id.email_address);
                    final EditText passwordEditText = view.findViewById(R.id.password);
                    final View passwordClear = view.findViewById(R.id.password_clear);

                    companyNameEditText.setText(company);
                    companyNameEditText.addTextChangedListener(new HTTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            company = editable.toString();
                        }
                    });
                    emailAddressEditText.setText(email);
                    emailAddressEditText.addTextChangedListener(new HTTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            email = editable.toString().toLowerCase();
                        }
                    });
                    passwordEditText.setText(password);
                    passwordEditText.addTextChangedListener(new HTTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            passwordClear.setVisibility(TextUtils.isEmpty(editable) ? View.INVISIBLE : View.VISIBLE);
                            password = editable.toString();
                        }
                    });
                    passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                nextPage();
                                v.clearFocus();
                                return true;
                            }
                            return false;
                        }
                    });
                    passwordClear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            passwordEditText.setText("");
                        }
                    });
                    break;

                case PAGE_INFO:
                    view = SignupInfoPage.getSignupInfoPageView(inflater, collection, getResources(), cognitoUserAttributes, incorrect);
                    break;
            }
            collection.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public int getCount() {
            return PAGES_COUNT;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
