package com.example.moutaz.movieapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

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


public class SearchActivity extends ActionBarActivity {

    private EditText etMovieName;
    private ImageView ivSearch;
    private ListView lvResults;
    private ProgressBar mProgressBar;

    private ArrayList<GridItem> listViewArrayList;
    private ListViewSearchCustomAdapter listViewAdapter;
    private Boolean noConnection;
    private AlertDialog alertDialog;
    private AlertDialog alertDialog2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initialize();

        //remove options
        //putDataInArrays
        //onPostExecute
        //adapter
        //add listview click listener

        //image el search tezbit
    }

    private void initialize() {
        etMovieName = (EditText) findViewById(R.id.etSearchField);
        ivSearch = (ImageView) findViewById(R.id.ivSearch);
        lvResults = (ListView) findViewById(R.id.listView2);
        mProgressBar = (ProgressBar) findViewById(R.id.pbLoading);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etMovieName.getText().toString();
                if (name.length() > 0) {
                    searchForResults();
                } else {
                    showDialogBox();
                }
            }
        });

        //setting adapter
        listViewArrayList = new ArrayList<>();
        listViewAdapter = new ListViewSearchCustomAdapter(this, R.layout.list_item_layout, listViewArrayList);
        lvResults.setAdapter(listViewAdapter);

        //add list view on item click listener
        lvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                GridItem gridItem = (GridItem) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), MovieActivity.class);

                intent.putExtra("TITLE", gridItem.getOrigTitle());
                intent.putExtra("OVERVIEW", gridItem.getOverview());
                intent.putExtra("RATE", gridItem.getVoteAvg());
                intent.putExtra("YEAR", gridItem.getRelDate());
                intent.putExtra("IMAGE", gridItem.getImage());
                intent.putExtra("ID", Integer.toString(gridItem.getId()));

                startActivity(intent);
            }
        });
    }

    private void showDialogBox() {
        //showing dialog Box
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Please enter movie name");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog2.dismiss();
            }
        });

        alertDialog2 = alertDialogBuilder.create();
        alertDialog2.show();
    }

    private void searchForResults() {
        //get async task
        noConnection = false;
        new FetchData().execute();
        mProgressBar.setVisibility(View.VISIBLE);
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

            listViewArrayList.clear();

            for(int i = 0; i < movieArray.length(); i++) {
                JSONObject movie = movieArray.getJSONObject(i);

                GridItem gridItem = new GridItem();
                gridItem.setOrigTitle(movie.getString(TITLE));
                gridItem.setOverview(movie.getString(OVERVIEW));
                gridItem.setVoteAvg(movie.getString(VOTE_AVG));
                gridItem.setRelDate(movie.getString(REL_DATE));
                gridItem.setImage(movie.getString(IMAGES));
                gridItem.setId(movie.getInt(ID));

                listViewArrayList.add(gridItem);
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
                link = "http://api.themoviedb.org/3/search/movie?query=" + etMovieName.getText().toString() + "&" + getString(R.string.API_KEY);
                URL url = new URL(link);


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
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
            if(noConnection || listViewArrayList.size()==0)
                showDialogMsg();
            else {
                listViewAdapter.setData(listViewArrayList);
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    private void showDialogMsg() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("No Internet Connection, Please connect to internet firstly");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                alertDialog.dismiss();
                finish();
            }
        });

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_search, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
