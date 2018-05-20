package jp.techacademy.takuya.sunohara.qa_app;

import java.io.Serializable;

public class Favorite implements Serializable {
    private String mTitle;
    private String mQuestionUid;

    public String getTitle() {
        return mTitle;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }

    public Favorite(String title, String questionUid){
        mTitle = title;
        mQuestionUid = questionUid;
    }
}
