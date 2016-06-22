package io.hypertrack.meta.view;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import io.hypertrack.meta.R;
import io.hypertrack.meta.model.User;
import io.hypertrack.meta.store.UserStore;

public class UserProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.updateTitle();
    }

    public void doneButtonClicked(MenuItem menuItem) {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_profile, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void updateTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }

        User user = UserStore.sharedStore.getUser();
        if (user == null) {
            return;
        }

        actionBar.setTitle(user.getFullName());
    }
}
