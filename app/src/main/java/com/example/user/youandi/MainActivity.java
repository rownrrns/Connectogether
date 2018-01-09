package com.example.user.youandi;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    ConstraintLayout l;
    int currentContent;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager;
            FragmentTransaction fragmentTransaction;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (currentContent != Constants.NAVIGATION_HOME) {
                        currentContent = Constants.NAVIGATION_HOME;
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.contents, new Home());
                        fragmentTransaction.commit();
                        fragmentManager.executePendingTransactions();
                    }
                    return true;
                case R.id.navigation_dashboard:
                    if (currentContent != Constants.NAVIGATION_YOUANDI) {
                        currentContent = Constants.NAVIGATION_YOUANDI;
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.contents, new Youandi());
                        fragmentTransaction.commit();
                        fragmentManager.executePendingTransactions();
                    }
                    return true;
                case R.id.navigation_notifications:
                    if (currentContent != Constants.NAVIGATION_PAINT) {
                        currentContent = Constants.NAVIGATION_PAINT;
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.contents, new Paper());
                        fragmentTransaction.commit();
                        fragmentManager.executePendingTransactions();
                    }
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentContent = Constants.NAVIGATION_HOME;
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.contents, new Home());
        fragmentTransaction.commit();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}
