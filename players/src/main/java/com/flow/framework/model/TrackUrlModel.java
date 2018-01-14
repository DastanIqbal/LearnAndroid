package com.flow.framework.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import java.util.List;

public class TrackUrlModel extends Model {
    @Column(name = "limitNum")
    private int limitNum;
    @Column(name = "trackUrl")
    private String trackUrl;

    public TrackUrlModel(String result_str) {
        setTrackUrl(result_str);
    }

    public String getTrackUrl() {
        return this.trackUrl;
    }

    public void setTrackUrl(String trackUrl) {
        this.trackUrl = trackUrl;
    }

    public int getLimitNum() {
        return this.limitNum;
    }

    public void setLimitNum(int limitNum) {
        this.limitNum = limitNum;
    }

    public static boolean checkExistTrackUrl(String trackUrl) {
        List<TrackUrlModel> trackUrlModels = new Select().from(TrackUrlModel.class).where("trackUrl = ?", trackUrl).execute();
        if (trackUrlModels == null || trackUrlModels.size() <= 0) {
            return false;
        }
        return true;
    }

    public static TrackUrlModel getActiveTrackUrl() {
        return (TrackUrlModel) new Select().from(TrackUrlModel.class).where("limitNum > 0").orderBy("limitNum ASC").executeSingle();
    }

    public static void updateTrackUrlModel(TrackUrlModel trackUrlModel) {
        Object[] objArr = new Object[]{trackUrlModel.getTrackUrl()};
        new Update(TrackUrlModel.class).set("limitNum=?", Integer.valueOf(trackUrlModel.getLimitNum())).where("trackUrl=?", objArr).execute();
    }

    public static void deleteActiveTrackUrl(String trackUrl) {
        new Delete().from(TrackUrlModel.class).where("trackUrl = ?", trackUrl).execute();
    }

    public String toString() {
        return "TrackUrlModel{trackUrl='" + this.trackUrl + '\'' + ", limitNum='" + this.limitNum + '\'' + '}';
    }
}
