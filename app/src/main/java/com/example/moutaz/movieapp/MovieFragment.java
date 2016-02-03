package com.example.moutaz.movieapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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


public class MovieFragment extends Fragment {

    private String ID;
    private ImageView poster;
    private TextView title;
    private TextView year;
    private TextView rate;
    private TextView overview;
    private ListView listView;
    private Button favBtn;

    private int trailersSize;
    private int reviewsSize;
    private ArrayList<String> listData;
    private ListViewCustomAdapter listViewAdapter;

    private String imageLink;
    private boolean trailersDone;
    private SharedPreferences sharedpreferences;
    private boolean noConnection;
    private String className;
    private Activity currActivity;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        currActivity = activity;
        className = activity.getClass().getSimpleName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_movie, container, false);

        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        initialize(rootView);

        noConnection = false;
        new getData().execute("trailer");



        Bundle arguments = getArguments();
        if (arguments == null) {
            title.setText("null");
            year.setText("null");
            rate.setText("null");
            overview.setText("null");
        }else {
            ID = arguments.getString("ID");
            if (sharedpreferences.contains(ID)) {
                favBtn.setText("remove from Favorites");
            }
            title.setText(arguments.getString("TITLE"));
            year.setText(arguments.getString("YEAR"));
            rate.setText(arguments.getString("RATE"));
            overview.setText(arguments.getString("OVERVIEW"));

            imageLink = "http://image.tmdb.org/t/p/w185/" + arguments.getString("IMAGE");
            Picasso.with(getActivity())
                    .load(imageLink)
                    .into(poster);
        }

        // Item Click Listener for the listview
        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View container, int position, long id) {
                if (position < trailersSize) {
                    String link = ((TextView) container.findViewById(R.id.tvLink)).getText().toString();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/v/" + link)));
                }
            }
        };

        // Setting the item click listener for the listview
        listView.setOnItemClickListener(itemClickListener);

        favBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                long size;
                SharedPreferences.Editor editor = sharedpreferences.edit();

                if(sharedpreferences.contains(ID)){ //remove from fav


                    size = sharedpreferences.getLong("size", 0);
                    String tempSize = sharedpreferences.getString(ID, "");
                    editor.remove(ID);
                    editor.remove(tempSize + "ID");
                    editor.remove(tempSize + "title");
                    editor.remove(tempSize + "year");
                    editor.remove(tempSize + "rate");
                    editor.remove(tempSize + "overview");
                    editor.remove(tempSize + "imageLink");
                    editor.putLong("size", size);
                    editor.commit();
                    favBtn.setText("add to Favorites");
                }else {
                    size = sharedpreferences.getLong("size", 0);
                    size++;
                    editor.putLong("size", size);
                    editor.putString(ID, Long.toString(size));
                    editor.putString(Long.toString(size) + "ID", ID);
                    editor.putString(Long.toString(size) + "title", title.getText().toString());
                    editor.putString(Long.toString(size) + "year", year.getText().toString());
                    editor.putString(Long.toString(size) + "rate", rate.getText().toString());
                    editor.putString(Long.toString(size) + "overview", overview.getText().toString());
                    editor.putString(Long.toString(size) + "imageLink", imageLink);
                    editor.commit();
                    favBtn.setText("remove from Favorites");
                }

                if(className.equals("MainActivity")){
                    ((MainActivity)currActivity).updateView();
                }
            }

        });


        return rootView;
    }

    private void initialize(View view) {
        poster = (ImageView) view.findViewById(R.id.ivPoster);
        title = (TextView) view.findViewById(R.id.tvTitle);
        year = (TextView) view.findViewById(R.id.tvYear);
        rate = (TextView) view.findViewById(R.id.tvRate);
        overview = (TextView) view.findViewById(R.id.tvOverview);
        overview.setMovementMethod(new ScrollingMovementMethod());
        listView = (ListView) view.findViewById(R.id.listView);
        favBtn = (Button) view.findViewById(R.id.bFav);


        trailersDone = false;
        trailersSize = reviewsSize = 0;
        listData = new ArrayList<>();
        listViewAdapter = new ListViewCustomAdapter(getActivity(), R.layout.reviews_layout, listData, trailersSize, reviewsSize);
        listView.setAdapter(listViewAdapter);
    }

    public class getData extends AsyncTask<String, Void, Void> {

        private String param;

        private Void putDataInArrays(String JsonStr)throws JSONException {

            final String KEY = "key";
            final String NAME = "name";
            final String AUTHOR = "author";
            final String CONTENT = "content";

            JSONObject MJson = new JSONObject(JsonStr);
            JSONArray movieArray = MJson.getJSONArray("results");

            if(param.equals("trailer")){
                trailersSize = movieArray.length();
                listData.clear();
            }else {
                reviewsSize = movieArray.length();
            }

            for(int i = 0; i < movieArray.length(); i++) {

                JSONObject movie = movieArray.getJSONObject(i);

                if(param.equals("trailer")) {
                    listData.add(movie.getString(NAME));
                    listData.add(movie.getString(KEY));
                }else{
                    listData.add(movie.getString(AUTHOR));
                    listData.add(movie.getString(CONTENT));
                }

            }

            return null;
        }

        @Override
        protected Void doInBackground(String... params) {

            param = params[0];
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JsonStr = null;

            try {

                String link;
                if(param.equals("trailer"))
                    link = "http://api.themoviedb.org/3/movie/"+ ID +"/videos?" + getString(R.string.API_KEY);
                else
                    link = "http://api.themoviedb.org/3/movie/"+ ID +"/reviews?" + getString(R.string.API_KEY);
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
            if(noConnection){
                //showDialogMsg();
            }
            else if (listData.size() > 0) {
                if (param.equals("trailer")) {
                    trailersDone = true;
                    noConnection = false;
                    new getData().execute("review");
                }
                else
                    listViewAdapter.setListData(listData, trailersSize, reviewsSize);
            }
        }
    }

    private void showDialogMsg() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setMessage("No Internet Connection, Please connect to internet firstly");

        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                getActivity().finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
