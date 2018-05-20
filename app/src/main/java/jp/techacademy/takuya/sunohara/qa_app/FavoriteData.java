package jp.techacademy.takuya.sunohara.qa_app;

import android.app.Application;

import java.io.Serializable;
import java.util.ArrayList;

public class FavoriteData extends Application implements Serializable {
    private ArrayList<Favorite> mFavoriteArrayList;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ArrayList<Favorite> getFavoriteArrayList() {
        return mFavoriteArrayList;
    }

    public void setFavoriteArrayList(Favorite favorite) {
        mFavoriteArrayList.add(favorite);
    }

    public void removeFavoriteData(String uid) {
        for (Favorite favorite : mFavoriteArrayList) {
            String key = favorite.getQuestionUid();
            int removeIndex = mFavoriteArrayList.indexOf(key);
            mFavoriteArrayList.remove(removeIndex);
        }
    }
}
