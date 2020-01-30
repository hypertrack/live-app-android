package com.hypertrack.live.auth;

import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.ui.MainActivity;
import com.hypertrack.live.utils.HTTextWatcher;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class SignUpFragment extends Fragment implements HTMobileClient.Callback {

    private static final String TAG = "SignInFragment";

    private static final int PAGES_COUNT = 6;
    private static final int PAGE_USER = 0;
    private static final int PAGE_CATEGORY = 1;
    private static final int PAGE_DEVICES = 2;
    private static final int PAGE_STAGE = 3;
    private static final int PAGE_AGGREE = 4;
    private static final String CUSTOM_COMPANY = "custom:company";
    private static final String CUSTOM_USE_CASE = "custom:use_case";

    private ViewPager viewPager;
    private Button next;
    private Button accept;
    private TextView agreeToTerms;

    private LoaderDecorator loader;

    private String company;
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        getActivity().setTitle(R.string.sign_up_a_new_account);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        loader = new LoaderDecorator(getContext());

        viewPager = view.findViewById(R.id.view_pager);
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

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextPage();
            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loader.start();
                HTMobileClient.getInstance(getContext()).signUp(email, password, cognitoUserAttributes, SignUpFragment.this);
            }
        });

        view.findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

    }

    private void nextPage() {

        switch (viewPager.getCurrentItem()) {
            case PAGE_USER:
                View incorrect = getView().findViewById(R.id.incorrect);
                incorrect.setVisibility(View.INVISIBLE);
                if (TextUtils.isEmpty(company) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    incorrect.setVisibility(View.VISIBLE);
                    return;
                }
                cognitoUserAttributes.put(CUSTOM_COMPANY, company);
                break;
            case PAGE_CATEGORY:
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

    @Override
    public void onSuccess(HTMobileClient mobileClient) {
        loader.stop();

        if (getActivity() != null) {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    @Override
    public void onError(String message) {
        loader.stop();
        viewPager.setCurrentItem(PAGE_USER);
    }

    private class MyPagerAdapter extends PagerAdapter {

        @SuppressWarnings("ConstantConditions")
        @NonNull
        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(collection.getContext());
            ViewGroup view = null;
            switch (position) {
                case PAGE_USER:
                    view = (ViewGroup) inflater.inflate(R.layout.view_pager_signup_user, collection, false);
                    EditText companyNameEditText = view.findViewById(R.id.company_name);
                    EditText emailAddressEditText = view.findViewById(R.id.email_address);
                    final EditText passwordEditText = view.findViewById(R.id.password);
                    final View passwordClear = view.findViewById(R.id.password_clear);

                    companyNameEditText.addTextChangedListener(new HTTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            company = editable.toString();
                        }
                    });
                    emailAddressEditText.addTextChangedListener(new HTTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            email = editable.toString();
                        }
                    });
                    passwordEditText.addTextChangedListener(new HTTextWatcher() {
                        @Override
                        public void afterTextChanged(Editable editable) {
                            passwordClear.setVisibility(TextUtils.isEmpty(editable) ? View.INVISIBLE : View.VISIBLE);
                            password = editable.toString();
                        }
                    });
                    passwordClear.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            passwordEditText.setText("");
                        }
                    });
                    passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                                v.clearFocus();
                                nextPage();
                                return true;
                            }
                            return false;
                        }
                    });
                    break;

                case PAGE_CATEGORY:
                    view = (ViewGroup) inflater.inflate(R.layout.view_pager_signup_radiogroup, collection, false);
                    TextView pageTitle = view.findViewById(R.id.page_title);
                    RadioGroup radioGroup = view.findViewById(R.id.radio_group);

                    pageTitle.setText(R.string.my_app_manages);
                    final String[] categories = getResources().getStringArray(R.array.categories);
                    for (String item : categories) {
                        RadioButton radioButton = new RadioButton(view.getContext());
                        radioButton.setText(item);
                        radioGroup.addView(radioButton);
                    }
                    radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup radioGroup, int i) {
                            cognitoUserAttributes.put(CUSTOM_USE_CASE, categories[i]);
                            nextPage();
                        }
                    });
                    break;
                case PAGE_DEVICES:
                    view = (ViewGroup) inflater.inflate(R.layout.view_pager_signup_radiogroup, collection, false);
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
