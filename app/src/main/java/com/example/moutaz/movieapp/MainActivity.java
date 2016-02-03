package com.example.moutaz.movieapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class MainActivity extends ActionBarActivity implements GridFragment.Callback{


    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        if (findViewById(R.id.detail_container) != null) {

            mTwoPane = true;

            Log.e("mTwoPane", "true");
            if (savedInstanceState == null) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                MovieFragment fragment = new MovieFragment();
                fragmentTransaction.add(R.id.detail_container, fragment, DETAILFRAGMENT_TAG);
                fragmentTransaction.commit();
            }

        } else {
            Log.e("mTwoPane", "false");
            mTwoPane = false;
        }
    }

    @Override
    public void onItemSelected(GridItem gridItem) {
        if (mTwoPane) {

            //putting data from intent
            Bundle bundle = new Bundle();
            bundle.putString("ID", Integer.toString(gridItem.getId()));
            bundle.putString("TITLE", gridItem.getOrigTitle());
            bundle.putString("YEAR", gridItem.getRelDate());
            bundle.putString("RATE", gridItem.getVoteAvg());
            bundle.putString("OVERVIEW", gridItem.getOverview());
            bundle.putString("IMAGE", gridItem.getImage());

            //open fragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MovieFragment fragment = new MovieFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.replace(R.id.detail_container, fragment, DETAILFRAGMENT_TAG);
            fragmentTransaction.commit();
        } else {
            Intent intent = new Intent(getApplicationContext(), MovieActivity.class);

            intent.putExtra("TITLE", gridItem.getOrigTitle());
            intent.putExtra("OVERVIEW", gridItem.getOverview());
            intent.putExtra("RATE", gridItem.getVoteAvg());
            intent.putExtra("YEAR", gridItem.getRelDate());
            intent.putExtra("IMAGE", gridItem.getImage());
            intent.putExtra("ID", Integer.toString(gridItem.getId()));

            startActivity(intent);
        }
    }




}
