package com.example.moutaz.movieapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class GridFragment extends Fragment {

    private AlertDialog alertDialog;
    private int mPosition = 0;
    private static final String SELECTED_KEY = "selected_position";

    private GridView gridView;
    private ProgressBar mProgressBar;
    private TextView noFavTV;
    private String lastStatus;
    private GridViewCustomAdapter gridViewAdapter;
    private ArrayList<GridItem> gridItemArrayList;
    private SharedPreferences sharedpreferences;
    private boolean noConnection;

    public interface Callback {
        public void onItemSelected(GridItem gridItem, boolean clicked);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_grid, container, false);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        gridView = (GridView) rootView.findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
        noFavTV = (TextView) rootView.findViewById(R.id.tvNoFav);

        gridItemArrayList = new ArrayList<>();

        getFromPreferences();

        gridViewAdapter = new GridViewCustomAdapter(getActivity(), R.layout.grid_item_layout, gridItemArrayList);
        gridView.setAdapter(gridViewAdapter);

        noConnection = false;
        if(lastStatus.equals("Fav")){
            updateView();
        }else {
            if(isOnline(getActivity())) {
                new FetchData().execute();
                mProgressBar.setVisibility(View.VISIBLE);
            }else {
                showDialogMsg();
            }
        }


        //listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                GridItem gridItem = (GridItem) parent.getItemAtPosition(position);
                mPosition = position;
                ((Callback) getActivity()).onItemSelected(gridItem, true);
            }
        });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.

        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu){
        if(lastStatus.equals(getString(R.string.popular_tag))){
            menu.findItem(R.id.popularity).setChecked(true);
        }else if (lastStatus.equals(getString(R.string.rating_tag))){
            menu.findItem(R.id.rated).setChecked(true);
        }else{
            menu.findItem(R.id.favorites).setChecked(true);
        }
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//        if (mPosition != GridView.INVALID_POSITION) {
//            outState.putInt(SELECTED_KEY, mPosition);
//        }
//        outState.putString("lastStatus", lastStatus);

        saveToPreferences();
    }

    private void saveToPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("state", lastStatus);
        if (mPosition < gridItemArrayList.size()) {
            editor.putInt(SELECTED_KEY, mPosition);
        }
        editor.commit();
    }

    private void getFromPreferences(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        lastStatus = preferences.getString("state", "Popularity");
        mPosition = preferences.getInt(SELECTED_KEY, 0);
    }

    public void updateView() {

        if(lastStatus.equals("Fav")) {
            long size = sharedpreferences.getLong("size", 0);
            if (size == 0) {
                noFavTV.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
            }
            gridItemArrayList.clear();
            for (long i = 1; i <= size; i++) {
                if (sharedpreferences.contains(Long.toString(i) + "ID")) {
                    GridItem gridItem = new GridItem();
                    gridItem.setId(Integer.parseInt(sharedpreferences.getString(Long.toString(i) + "ID", "")));
                    gridItem.setOrigTitle(sharedpreferences.getString(Long.toString(i) + "title", ""));
                    gridItem.setRelDate(sharedpreferences.getString(Long.toString(i) + "year", ""));
                    gridItem.setVoteAvg(sharedpreferences.getString(Long.toString(i) + "rate", ""));
                    gridItem.setOverview(sharedpreferences.getString(Long.toString(i) + "overview", ""));
                    gridItem.setImage(sharedpreferences.getString(Long.toString(i) + "imageLink", ""));
                    gridItemArrayList.add(gridItem);
                }
            }
            if (gridItemArrayList.size() > 0) {
                gridViewAdapter.setGridData(gridItemArrayList);
                if(mPosition >= gridItemArrayList.size()) {
                    mPosition = 0;
                }
                gridView.smoothScrollToPosition(mPosition);
                ((Callback) getActivity()).onItemSelected(gridItemArrayList.get(mPosition), false);
            } else {
                noFavTV.setVisibility(View.VISIBLE);
                gridView.setVisibility(View.GONE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putLong("size", 0);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.popularity:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                if (!(lastStatus.equals(getString(R.string.popular_tag)))) {
                    mPosition = 0;
                    lastStatus = getString(R.string.popular_tag);
                    noConnection = false;
                    if(isOnline(getActivity())) {
                        new FetchData().execute();
                        mProgressBar.setVisibility(View.VISIBLE);
                        noFavTV.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                    }else{
                        showDialogMsg();
                    }
                }
                return true;
            case R.id.rated:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                if(!(lastStatus.equals(getString(R.string.rating_tag)))) {
                    mPosition = 0;
                    lastStatus = getString(R.string.rating_tag);
                    noConnection = false;
                    if(isOnline(getActivity())) {
                        new FetchData().execute();
                        mProgressBar.setVisibility(View.VISIBLE);
                        noFavTV.setVisibility(View.GONE);
                        gridView.setVisibility(View.VISIBLE);
                    }else{
                        showDialogMsg();
                    }
                }
                return true;
            case R.id.favorites:
                if (item.isChecked())
                    item.setChecked(false);
                else
                    item.setChecked(true);
                if(!(lastStatus.equals("Fav"))) {
                    mPosition = 0;
                    lastStatus = "Fav";
                    updateView();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public class FetchData extends AsyncTask<Void, Void, Void> {

        private final String LOG_TAG = FetchData.class.getSimpleName();

        private Void putDataInArrays(String JsonStr)throws JSONException {

            final String ID = "id";
            final String TITLE = "original_title";
            final String OVERVIEW = "overview";
            final String VOTE_AVG = "vote_average";
            final String REL_DATE = "release_date";
            final String IMAGES = "poster_path";

            JSONObject MJson = new JSONObject(JsonStr);
            JSONArray movieArray = MJson.getJSONArray("results");



            gridItemArrayList.clear();
            for(int i = 0; i < movieArray.length(); i++) {


                JSONObject movie = movieArray.getJSONObject(i);

                GridItem gridItem = new GridItem();
                gridItem.setOrigTitle(movie.getString(TITLE));
                gridItem.setOverview(movie.getString(OVERVIEW));
                gridItem.setVoteAvg(movie.getString(VOTE_AVG));
                gridItem.setRelDate(movie.getString(REL_DATE));
                gridItem.setImage(movie.getString(IMAGES));
                gridItem.setId(movie.getInt(ID));

                gridItemArrayList.add(gridItem);

            }

            return null;
        }

        @Override
        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String JsonStr = null;


            try {

                String link;
                if(lastStatus.equals(getString(R.string.popular_tag)))
                    link = getString(R.string.popular_movies) + "&" + getString(R.string.API_KEY);
                else
                    link = getString(R.string.highest_rated_movies) + "&" + getString(R.string.API_KEY);
                URL url = new URL(link);


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    noConnection = true;
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    noConnection = true;
                    return null;
                }
                JsonStr = buffer.toString();

            } catch (IOException e) {
                noConnection = true;
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {

                    }
                }
            }

            try {
                putDataInArrays(JsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            if(noConnection)
                showDialogMsg();
            else if (gridItemArrayList.size() > 0) {
                gridViewAdapter.setGridData(gridItemArrayList);
            }
            mProgressBar.setVisibility(View.GONE);

            if(mPosition >= gridItemArrayList.size()) {
                mPosition = 0;
            }
            if(gridItemArrayList.size() > 0) {
                gridView.smoothScrollToPosition(mPosition);
                ((Callback) getActivity()).onItemSelected(gridItemArrayList.get(mPosition), false);
            }else{
                showDialogMsg();
            }
        }
    }

    private void showDialogMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("No Internet Connection, Please connect to internet firstly");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                getActivity().finish();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        return isConnected;
    }
}
