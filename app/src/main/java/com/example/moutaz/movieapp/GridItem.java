package com.example.moutaz.movieapp;

/**
 * Created by moutaz on 12/16/2015.
 */
public class GridItem {
    private int id;
    private String image;
    private String origTitle;
    private String overview;
    private String voteAvg;
    private String relDate;

    public GridItem(){}

    public GridItem(String i){
        super();
        this.image = i ;
    }

    public void setImage(String i){
        this.image = i;
    }

    public String getImage(){
        return this.image;
    }

    public String getRelDate() {
        return relDate;
    }

    public void setRelDate(String relDate) {
        this.relDate = relDate;
        if(relDate.length() > 3)
            this.relDate = relDate.substring(0, 4);
    }

    public String getVoteAvg() {
        return voteAvg;
    }

    public void setVoteAvg(String voteAvg) {
        if(voteAvg.contains("/10"))
            this.voteAvg = voteAvg;
        else
            this.voteAvg = voteAvg + "/10";
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getOrigTitle() {
        return origTitle;
    }

    public void setOrigTitle(String origTitle) {
        this.origTitle = origTitle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
