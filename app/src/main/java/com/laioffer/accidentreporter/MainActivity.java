package com.laioffer.accidentreporter;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.laioffer.accidentreporter.Fragment.AccidentsFragment;
import com.laioffer.accidentreporter.Fragment.MainFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
  private static final String TAG = MainActivity.class.getSimpleName();
  private DrawerLayout mDrawerLayout;
  //private LocationTracker mLocationTracker;
  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private LocationTracker mLocationTracker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
    //adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
    vpPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    vpPager.setCurrentItem(1);

    ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.
            WRITE_EXTERNAL_STORAGE},1);

    mLocationTracker = new LocationTracker(this);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionbar = getSupportActionBar();
    actionbar.setDisplayHomeAsUpEnabled(true);
    actionbar.setHomeAsUpIndicator(R.drawable.menu);


    mDrawerLayout = findViewById(R.id.drawer_layout);

    NavigationView navigationView = findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
              @Override
              public boolean onNavigationItemSelected(MenuItem menuItem) {
                // set item as selected to persist highlight
                menuItem.setChecked(true);
                // close drawer when item is tapped
                mDrawerLayout.closeDrawers();
                // Add code here to update the UI based on the item selected
                // For example, swap UI fragments here
                return true;
              }
            });


    mDrawerLayout.addDrawerListener(
            new DrawerLayout.DrawerListener() {
              @Override
              public void onDrawerSlide(View drawerView, float slideOffset) {
                // Respond when the drawer's position changes
              }

              @Override
              public void onDrawerOpened(View drawerView) {
                // Respond when the drawer is opened
                TextView textview = (TextView)drawerView.findViewById(R.id.user_name);
                final TextView locationText = (TextView) drawerView.findViewById(R.id.user_location);
                if (Config.username == null) {
                  textview.setText("");
                } else {
                  textview.setText(Config.username);
                }
                mLocationTracker.getLocation();
                final double longitude = mLocationTracker.getLongitude();
                final double latitude = mLocationTracker.getLatitude();
                if (Config.username == null) {
                  locationText.setText("");
                } else {
                  new AsyncTask<Void, Void, Void>() {
                    private List<String> mAddressList = new ArrayList<String>();

                    @Override
                    protected Void doInBackground(Void... urls) {
                      mAddressList = mLocationTracker.getCurrentLocationViaJSON(latitude,longitude);
                      return null;
                    }

                    @Override
                    protected void onPostExecute(Void input) {
                      if (mAddressList.size() > 3) {
                        locationText.setText(mAddressList.get(0) + ", " + mAddressList.get(1));
                      }
                    }
                  }.execute();
                }

              }

              @Override
              public void onDrawerClosed(View drawerView) {
                // Respond when the drawer is closed
              }

              @Override
              public void onDrawerStateChanged(int newState) {
                // Respond when the drawer motion state changes
              }
            }
    );


    mAuth = FirebaseAuth.getInstance();

    //Add listener to check sign in status
    mAuthListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
          Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
        } else {
          Log.d(TAG, "onAuthStateChanged:signed_out");
        }
      }
    };

    //sign in anonymously
    mAuth.signInAnonymously().addOnCompleteListener(this,  new OnCompleteListener<AuthResult>() {
      @Override
      public void onComplete(@NonNull Task<AuthResult> task) {
        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());
        if (!task.isSuccessful()) {
          Log.w(TAG, "signInAnonymously", task.getException());
        }
      }
    });
  }

  /**
   * Fragment pager allows us to choose fragments
   */
  public static class MyPagerAdapter extends FragmentPagerAdapter {
    private static int NUM_ITEMS = 2;

    public MyPagerAdapter(FragmentManager fragmentManager) {
      super(fragmentManager);
    }

    // Returns total number of pages
    @Override
    public int getCount() {
      return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return AccidentsFragment.newInstance();
        case 1:
          return MainFragment.newInstance();
        default:
          return null;
      }
    }

    @Override
    public CharSequence getPageTitle(int position) {
      switch (position) {
        case 0:
          return "Account";
        case 1:
          return "Map";
      }

      return null;
    }

  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        mDrawerLayout.openDrawer(GravityCompat.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override // android recommended class to handle permissions
  public void onRequestPermissionsResult(int requestCode,
                                         String permissions[], int[] grantResults) {
    switch (requestCode) {
      case 1: {

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

          Log.d("permission", "granted");
        } else {
          onDestroy();
        }
        return;
      }
    }
  }

  //Add authentification listener when activity starts
  @Override
  public void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthListener);
  }

  //Remove authentification listener when activity starts
  @Override
  public void onStop() {
    super.onStop();
    if (mAuthListener != null) {
      mAuth.removeAuthStateListener(mAuthListener);
    }
  }


}
