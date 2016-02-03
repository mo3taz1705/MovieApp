package com.example.moutaz.movieapp;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class MovieActivity extends ActionBarActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);



        if (savedInstanceState == null) {
            //putting data from intent
            Bundle bundle = new Bundle();
            Intent intent = getIntent();
            bundle.putString("ID", intent.getStringExtra("ID"));
            bundle.putString("TITLE", intent.getStringExtra("TITLE"));
            bundle.putString("YEAR", intent.getStringExtra("YEAR"));
            bundle.putString("RATE", intent.getStringExtra("RATE"));
            bundle.putString("OVERVIEW", intent.getStringExtra("OVERVIEW"));
            bundle.putString("IMAGE", intent.getStringExtra("IMAGE"));

            //open fragment
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MovieFragment fragment = new MovieFragment();
            fragment.setArguments(bundle);
            fragmentTransaction.add(R.id.detail_container, fragment, "");
            fragmentTransaction.commit();
        }



    }



}
