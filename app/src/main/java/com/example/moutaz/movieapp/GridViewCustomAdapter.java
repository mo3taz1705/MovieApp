package com.example.moutaz.movieapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by moutaz on 12/16/2015.
 */
public class GridViewCustomAdapter extends ArrayAdapter<GridItem> {
    private Context mContext;
    private int layoutResourceID;
    private ArrayList<GridItem> gridData = new ArrayList<GridItem>();

    public GridViewCustomAdapter(Context context, int layoutResID, ArrayList<GridItem> gData){
        super(context, layoutResID, gData);
        this.mContext = context;
        this.layoutResourceID = layoutResID;
        this.gridData = gData;
    }

    public void setGridData(ArrayList<GridItem> itemArrayList){
        this.gridData = itemArrayList;
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View v, ViewGroup parent){
        View itemView = v;
        ViewHolder holder;

        if(itemView == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            itemView = inflater.inflate(layoutResourceID, parent, false);

            holder = new ViewHolder();
            holder.imageView = (ImageView)itemView.findViewById(R.id.ivItem);

            itemView.setTag(holder);
        }else{
            holder = (ViewHolder) itemView.getTag();
        }

        GridItem item = gridData.get(position);
        String imageLink = "http://image.tmdb.org/t/p/w185/" + item.getImage() ;
        Picasso.with(mContext)
                .load(imageLink)
                .into(holder.imageView);

        return itemView;
    }

    static class ViewHolder{
        ImageView imageView;
    }
}
