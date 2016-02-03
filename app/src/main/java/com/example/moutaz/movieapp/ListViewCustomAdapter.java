package com.example.moutaz.movieapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by moutaz on 1/30/2016.
 */
public class ListViewCustomAdapter extends ArrayAdapter<String>{

    private Context mContext;
    private ArrayList<String> listData = new ArrayList<String>();
    private int  noOfItems;
    private int trailersSize;

    public ListViewCustomAdapter(Context context, int layoutResID, ArrayList<String> lData, int trailers_size, int reviews_size){
        super(context, layoutResID, lData);
        this.mContext = context;
        this.listData = lData;
        this.noOfItems = trailers_size + reviews_size;
        this.trailersSize = trailers_size;
    }

    public void setListData(ArrayList<String> itemArrayList, int trailers_size, int reviews_size){
        this.listData = itemArrayList;
        this.noOfItems = trailers_size + reviews_size;
        this.trailersSize = trailers_size;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

            if(position < trailersSize){  //trailer
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                row = inflater.inflate(R.layout.trailers_layout, parent, false);

                TextView tvName = (TextView)row.findViewById(R.id.tvName);
                TextView tvLink = (TextView)row.findViewById(R.id.tvLink);

                tvName.setText(listData.get(position*2));
                tvLink.setText(listData.get(position*2+1));
            }else{  //review
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                row = inflater.inflate(R.layout.reviews_layout, parent, false);

                TextView tvAuthor = (TextView)row.findViewById(R.id.tvAuthor);
                TextView tvReview = (TextView)row.findViewById(R.id.tvReview);

                tvAuthor.setText(listData.get(position*2));
                tvReview.setText(listData.get(position*2+1));
            }

        return row;
    }

    @Override
    public int getCount(){
        return noOfItems;
    }
}
