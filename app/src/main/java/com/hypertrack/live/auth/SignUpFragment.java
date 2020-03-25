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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException;
import com.hypertrack.live.App;
import com.hypertrack.live.HTMobileClient;
import com.hypertrack.live.R;
import com.hypertrack.live.ui.LoaderDecorator;
import com.hypertrack.live.utils.HTTextWatcher;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ConstantConditions")
public class SignUpFragment extends Fragment implements HTMobileClient.Callback {

    private static final String TAG = App.TAG + "SignInFragment";

    private static final int PAGES_COUNT = 2;
    private static final int PAGE_USER = 0;
    private static final int PAGE_INFO = 1;
    private static final String CUSTOM_COMPANY = "custom:company";
    private static final String CUSTOM_USE_CASE = "custom:use_case";
    private static final String CUSTOM_SCALE = "custom:scale";
    public static final String CUSTOM_STATE = "custom:state";

    private ViewPager viewPager;
    private TextView incorrect;
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
                if (!cognitoUserAttributes.keySet().containsAll(Arrays.asList(CUSTOM_USE_CASE, CUSTOM_SCALE, CUSTOM_STATE))) {
                    showError(getString(R.string.all_fields_required));
                    return;
                }
                loader.start();
                HTMobileClient.getInstance(getContext()).signUp(email, password, cognitoUserAttributes, SignUpFragment.this);
            }
        });

        view.findViewById(R.id.sign_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

    }

    private void nextPage() {

        incorrect.setVisibility(View.INVISIBLE);
        switch (viewPager.getCurrentItem()) {
            case PAGE_USER:
                if (TextUtils.isEmpty(company) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    showError(getString(R.string.all_fields_required));
                    return;
                }
                cognitoUserAttributes.put(CUSTOM_COMPANY, company);
                break;
            case PAGE_INFO:
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
    public void onSuccess(HTMobileClient mobileClient) {
        if (getActivity() != null) {
            loader.stop();
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_frame, new ConfirmFragment(), ConfirmFragment.class.getSimpleName())
                        .commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void onError(String message, Exception e) {
        if (getActivity() != null) {
            loader.stop();

            viewPager.setCurrentItem(PAGE_USER);
            if (e instanceof UsernameExistsException) {
                incorrect.setText(R.string.an_account_exists);
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
            ViewGroup view = null;
            switch (position) {
                case PAGE_USER:
                    view = (ViewGroup) inflater.inflate(R.layout.view_pager_signup_user, collection, false);
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
                    view = (ViewGroup) inflater.inflate(R.layout.view_pager_signup_info, collection, false);
                    Spinner categoriesSpinner = view.findViewById(R.id.categories);
                    Spinner devicesSpinner = view.findViewById(R.id.devices);
                    Spinner stageSpinner = view.findViewById(R.id.stage);
                    final List<String> categories = Arrays.asList(getResources().getStringArray(R.array.categories));
                    final List<String> scale = Arrays.asList("", "<100", "100", "1000", ">10000");
                    final List<String> stage = Arrays.asList(getResources().getStringArray(R.array.stage));

                    categoriesSpinner.setSelection(categories.indexOf(cognitoUserAttributes.get(CUSTOM_USE_CASE)));
                    categoriesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            incorrect.setVisibility(View.INVISIBLE);
                            if (i == 0) {
                                cognitoUserAttributes.remove(CUSTOM_USE_CASE);
                            } else {
                                cognitoUserAttributes.put(CUSTOM_USE_CASE, categories.get(i));
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                    devicesSpinner.setSelection(scale.indexOf(cognitoUserAttributes.get(CUSTOM_SCALE)));
                    devicesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            incorrect.setVisibility(View.INVISIBLE);
                            if (i == 0) {
                                cognitoUserAttributes.remove(CUSTOM_SCALE);
                            } else {
                                cognitoUserAttributes.put(CUSTOM_SCALE, scale.get(i));
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
                    stageSpinner.setSelection(stage.indexOf(cognitoUserAttributes.get(CUSTOM_STATE)));
                    stageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            incorrect.setVisibility(View.INVISIBLE);
                            if (i == 0) {
                                cognitoUserAttributes.remove(CUSTOM_STATE);
                            } else {
                                cognitoUserAttributes.put(CUSTOM_STATE, stage.get(i));
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                        }
                    });
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
