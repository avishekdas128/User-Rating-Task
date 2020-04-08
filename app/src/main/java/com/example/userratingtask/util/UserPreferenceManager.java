package com.example.userratingtask.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.userratingtask.models.RatingModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class UserPreferenceManager {

    private static SharedPreferences mSharedPreferences;
    private static final String PREF_NAME = "USERRATINGTASK";

    public static String RATINGS = "ratings";

    private static void init(Context mContext) {
        mSharedPreferences = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    static public void setPreferenceObjectRatings(Context mContext, List<RatingModel> object) {
        if(mSharedPreferences==null){
            init(mContext);
        }
        SharedPreferences.Editor mShEditor=mSharedPreferences.edit();
        Gson gson = new Gson();
        String jsonObject = gson.toJson(object);
        mShEditor.putString(RATINGS, jsonObject);
        mShEditor.commit();
    }

    static public List<RatingModel> getPreferenceObjectRatings(Context mContext) {
        if(mSharedPreferences==null){
            init(mContext);
        }
        Type listType = new TypeToken<List<RatingModel>>() {}.getType();
        String json = mSharedPreferences.getString(RATINGS, "");
        Gson gson=new Gson();
        return gson.fromJson(json, listType);
    }

}
