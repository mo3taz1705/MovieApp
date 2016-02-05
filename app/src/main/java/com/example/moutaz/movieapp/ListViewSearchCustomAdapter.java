package com.example.moutaz.movieapp;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by moutaz on 2/5/2016.
 */
public class ListViewSearchCustomAdapter extends ArrayAdapter<GridItem>{
    private Context mContext;
    private int layoutResourceID;
    private ArrayList<GridItem> dataArray = new ArrayList<GridItem>();

    public ListViewSearchCustomAdapter(Context context, int layoutResID, ArrayList<GridItem> gData){
        super(context, layoutResID, gData);
        this.mContext = context;
        this.layoutResourceID = layoutResID;
        this.dataArray = gData;
    }

    public void setData(ArrayList<GridItem> itemArrayList){
        this.dataArray = itemArrayList;
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
            holder.imageView = (ImageView) itemView.findViewById(R.id.ivResult);
            holder.textView = (TextView) itemView.findViewById(R.id.tvResult);

            itemView.setTag(holder);
        }else{
            holder = (ViewHolder) itemView.getTag();
        }

        GridItem item = dataArray.get(position);
        String imageLink = "http://image.tmdb.org/t/p/w185/" + item.getImage() ;
        Picasso.with(mContext)
                .load(imageLink)
                .into(holder.imageView);
        holder.textView.setText(item.getOrigTitle());

        return itemView;
    }

    static class ViewHolder{
        ImageView imageView;
        TextView textView;
    }
}
