package jp.techacademy.takuya.sunohara.qa_app;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;
    private DatabaseReference mAnswerRef;
    //追記
    private DatabaseReference mFavoriteRef;
    private boolean favoriteFlag;
    private FavoriteData favoriteData;
    private ArrayList<Favorite> mFavoriteArrayList;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    //追記
    private ChildEventListener mFavoriteEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String questionUid = dataSnapshot.getKey();
            String title = (String) map.get("title");

            Favorite favorite = new Favorite(title, questionUid);
            favoriteData.setFavoriteArrayList(favorite);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            HashMap map = (HashMap) dataSnapshot.getValue();
            String questionUid = dataSnapshot.getKey();
            favoriteData.removeFavoriteData(questionUid);

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });


        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }//onCreate/

    //追記
    @Override
    protected void onResume() {
        super.onResume();

        FloatingActionButton fav = (FloatingActionButton) findViewById(R.id.favorite);
        //ログイン済みのユーザーを取得
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // ログインしていない時はお気に入りボタンを隠す
            fav.setVisibility(View.INVISIBLE);
        } else {
            //todo
            //お気に入りデータを保持するクラスからデータを取得して、フラグの変更を行う。
            String uid = mQuestion.getQuestionUid();
            favoriteFlag = false;
            favoriteData = (FavoriteData)this.getApplication();
            mFavoriteArrayList = favoriteData.getFavoriteArrayList();
            if (mFavoriteArrayList != null) {
                for (Favorite favorite : mFavoriteArrayList) {
                    if (favorite.getQuestionUid().equals(uid)) {
                        favoriteFlag = true;
                    }
                };
            }

            DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
            mFavoriteRef = dataBaseReference.child(Const.FavoritePATH).child(user.getUid()).child(mQuestion.getQuestionUid());

            fav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (favoriteFlag == false) {
                        //flagがfalseの時＝お気に入りに登録していない場合は、データを取得して保存
                        Map<String, String> data = new HashMap<String, String>();

                        data.put("title", mQuestion.getTitle());
                        data.put("questionUid", mQuestion.getQuestionUid());

                        mFavoriteRef.setValue(data);
                        setStar();
                        favoriteFlag = true;

                    } else {
                        //flagがtrue＝登録済みの場合は、データを削除
                        mFavoriteRef.removeValue();
                        removeStar();
                    }

                    if (mFavoriteRef != null) {
                        mFavoriteRef.removeEventListener(mFavoriteEventListener);
                    }
                    mFavoriteRef.addChildEventListener(mFavoriteEventListener);
                }
            });
        }
    }//onResume/

    private void setStar() {
        FloatingActionButton fav = (FloatingActionButton)findViewById(R.id.favorite);
        Drawable drawable = getResources().getDrawable(R.drawable.fav_1);
        fav.setImageDrawable(drawable);
    }

    private void removeStar() {
        FloatingActionButton fav = (FloatingActionButton)findViewById(R.id.favorite);
        Drawable drawable = getResources().getDrawable(R.drawable.fav_0);
        fav.setImageDrawable(drawable);
    }
}
