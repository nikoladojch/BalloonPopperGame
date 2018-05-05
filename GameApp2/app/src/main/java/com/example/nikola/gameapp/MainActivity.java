package com.example.nikola.gameapp;

import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nikola.gameapp.utils.HighScoreHelper;
import com.example.nikola.gameapp.utils.SimpleAlertDialog;
import com.example.nikola.gameapp.utils.SoundHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

public class MainActivity extends AppCompatActivity
        implements Balloon.BalloonListener{
    private static final int MIN_ANIMATION_DELAY = 500;
    private static final int MAX_ANIMATION_DELAY = 1500;
    private static final int MIN_ANIMATION_DURATION = 2000;
    private static final int MAX_ANIMATION_DURATION = 6000;
    private static final int NUMBER_OF_LIVES = 5;
    private static final int BALLOONS_PER_LEVEL = 3 ;
    private static final String TAG = "nesho";

    private ViewGroup mContentView;
    private ImageView playButton;
    private int[]  mBalloonColors = new int[7];
    private int mNextColor, mScreenWidth, mScreenHeight;
    private int mLevel, mScore, mHighScore, mLivesUsed;
    TextView mScoreDisplay, mLevelDisplay, mHighScoreDisplay;
    private List<ImageView> mLivesImages = new ArrayList<>();
    private List<Balloon> mBalloons = new ArrayList<>();
    private Button mGoButton;
    private boolean mPlaying;
    private boolean mGameStopped = false;
    private boolean mGamePaused = false;
    private int mBalloonsPopped;
    private SoundHelper mSoundHelper;
    private int mBalloonsPerLevel = BALLOONS_PER_LEVEL;
    private boolean isGameOver= false;
    private ImageView soundButton;
    private boolean music=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBalloonColors[0] = Color.argb(255,255,0,0);
        mBalloonColors[1] = Color.argb(255,0,255,0);
        mBalloonColors[2] = Color.argb(255,0,0,255);
        mBalloonColors[3] = Color.argb(255,0,255,255);
        mBalloonColors[4] = Color.argb(255,255,0,255);
        mBalloonColors[5] = Color.argb(255,255,255,0);
        mBalloonColors[6] = Color.argb(255,0,0,0);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        mContentView = findViewById(R.id.activity_main);
        setToFullScreen();

        playButton = findViewById(R.id.imageView2);
        soundButton = findViewById(R.id.musicSound);

        ViewTreeObserver viewTreeObserver = mContentView.getViewTreeObserver();
        if(viewTreeObserver.isAlive()){
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mContentView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mScreenWidth = mContentView.getWidth();
                    mScreenHeight = mContentView.getHeight();

                }
            });
        }

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setToFullScreen();
            }
        });

        mScoreDisplay = (TextView) findViewById(R.id.score_display);
        mLevelDisplay = (TextView) findViewById(R.id.level_display);
        mHighScoreDisplay =(TextView) findViewById(R.id.high_score_display);
        mLivesImages.add((ImageView) findViewById(R.id.person1));
        mLivesImages.add((ImageView) findViewById(R.id.person2));
        mLivesImages.add((ImageView) findViewById(R.id.person3));
        mLivesImages.add((ImageView) findViewById(R.id.person4));
        mLivesImages.add((ImageView) findViewById(R.id.person5));


        mGoButton = (Button) findViewById(R.id.go_button);

        updateDisplay();
        mSoundHelper = new SoundHelper(this);
        mSoundHelper.prepareMusicPlayer(this);


        Toast.makeText(this, "Pop the balloons before they explode!", Toast.LENGTH_LONG).show();

    }

    private void setToFullScreen(){
        ViewGroup rootLayout = (ViewGroup) findViewById(R.id.activity_main);
        rootLayout .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        setToFullScreen();
        mSoundHelper.playMusic();

    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseGame();
        mSoundHelper.pauseMusic();
    }

    private void pauseGame(){
        playButton.setVisibility(View.VISIBLE);
        for (final Balloon balloon: mBalloons){
            //mContentView.removeView(balloon);
            mContentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mContentView.removeView(balloon);
                }
            },100);
            balloon.setPopped(true);
        }
        mPlaying = false;
        mGameStopped = false;
        mGamePaused = true;
        if(mLivesUsed < 5){
            mGoButton.setText("Resume game");
        }
        else{
            mGoButton.setText("Start game");
        }

    }

    private void startGame(){
        setToFullScreen();
        playButton.setVisibility(View.GONE);
        mBalloonsPerLevel = BALLOONS_PER_LEVEL;
        mScore = 0;
        mLevel = 0;
        mLivesUsed = 0;
        for (ImageView life:
                mLivesImages) {
            life.setImageResource(R.drawable.ic_person_black_24dp);
        }
        mGameStopped = false;
        mGamePaused = false;
        startLevel();
    }

    private void startLevel(){
        mLevel++;
        updateDisplay();
        BalloonLauncher launcher = new BalloonLauncher();
        launcher.execute(mLevel);
        mPlaying = true;
        mBalloonsPopped = 0;
        mGoButton.setText("Stop game");

    }

    private void finishLevel(){
        mPlaying = false;
    }

    public void goButtonClickHandler(View view) {
        if(mPlaying){
            gameOver(true  );
        }
        //Igrata e pauzirana
        else if(mGamePaused){
            resumeGame();
        }
        //Igrata e zavrshena
        else if(mGameStopped){
            startGame();
        }
        //Nikogash ne e zapochnata igrata
        else{
            startGame();
        }
    }

    private void resumeGame() {
        if(mLivesUsed < 5) {
            playButton.setVisibility(View.GONE);
            BalloonLauncher launcher = new BalloonLauncher();
            launcher.execute(mLevel);
            mPlaying = true;
            mGameStopped = false;
            mGamePaused = false;
            mGoButton.setText("Stop game");
        }
        else{
            startGame();
        }
    }

    @Override
    public void popBalloon(Balloon balloon, boolean userTouch) {
        mBalloonsPopped++;
        mSoundHelper.playSound();
        mContentView.removeView(balloon);
        mBalloons.remove(balloon);
        if(balloon.getBallonColor() == Color.BLACK) {
            if (userTouch) {
                mLivesUsed++;
                if (mLivesUsed <= mLivesImages.size()) {
                    mLivesImages.get(mLivesUsed - 1)
                            .setImageResource
                                    (R.drawable.ic_person_outline_black_24dp);
                }
                if (mLivesUsed == NUMBER_OF_LIVES - 4) {
                    Toast.makeText(this, "Do not pop the black balloons",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            if (userTouch) {
                mScore++;
            } else {
                mLivesUsed++;
                if (mLivesUsed <= mLivesImages.size()) {
                    mLivesImages.get(mLivesUsed - 1)
                            .setImageResource
                                    (R.drawable.ic_person_outline_black_24dp);
                }

                if (mLivesUsed == NUMBER_OF_LIVES - 4) {
                    Toast.makeText(this, "Missed that one",
                            Toast.LENGTH_SHORT).show();
                }

            }
        }

        if (mLivesUsed == NUMBER_OF_LIVES) {
            gameOver(true);
            return;
        }


        updateDisplay();

        if (mBalloonsPopped == mBalloonsPerLevel) {
            mBalloonsPerLevel++;
            finishLevel();
            startLevel();
        }
    }

    private void gameOver(boolean allLivesUsed) {
        Toast.makeText(this,"Game over!", Toast.LENGTH_SHORT).show();
        for (final Balloon balloon: mBalloons){
            //mContentView.removeView(balloon);
            mContentView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mContentView.removeView(balloon);
                }
            },1);
            balloon.setPopped(true);
        }
        mBalloons.clear();
        mPlaying = false;
        mGameStopped = true;
        mGamePaused = false;
        mGoButton.setText("Start game");
        playButton.setVisibility(View.VISIBLE);

        if(allLivesUsed){
            if(HighScoreHelper.isTopScore(this, mScore)){
                HighScoreHelper.setTopScore(this, mScore);
                SimpleAlertDialog dialog = SimpleAlertDialog.newInstance("New High Score",
                        String.format("Your new high score is: %d", mScore));
                mHighScore = mScore;
                dialog.show(getSupportFragmentManager(), null);
                updateDisplay();
            }
        }
    }

    private void updateDisplay() {
        mScoreDisplay.setText(String.valueOf(mScore));
        mLevelDisplay.setText(String.valueOf(mLevel));
        mHighScoreDisplay.setText(String.valueOf(HighScoreHelper.getTopScore(this)));
    }

    public void playGame(View view) {
        startGame();
    }

    public void turnOffMusic(View view) {
        if(music){
            soundButton.setImageResource(R.drawable.ic_volume_off_black_24dp);
            mSoundHelper.pauseMusic();
            music = false;
        }
        else{
            soundButton.setImageResource(R.drawable.ic_volume_up_black_24dp);
            mSoundHelper.playMusic();
            music = true;
        }
    }




    private class BalloonLauncher extends AsyncTask<Integer, Integer, Void> {
        @Override
        protected Void doInBackground(Integer... integers) {
            if(integers.length != 1){
                throw new AssertionError("Expected 1 integer for current error");
            }
            int level = integers[0];
            int maxDelay = Math.max(MIN_ANIMATION_DELAY,
                    (MAX_ANIMATION_DELAY - ((level - 1)* 500)));
            int minDelay = maxDelay / 2;

            int balloonsLaunched = 0;

            while(mPlaying && balloonsLaunched < mBalloonsPerLevel){
                Random random = new Random(new Date().getTime());
                int xPosition = random.nextInt(mScreenWidth - 200);
                publishProgress(xPosition);
                balloonsLaunched++;

                int delay = random.nextInt(minDelay) + minDelay;
                try{
                    Thread.sleep(delay);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int xPosition = values[0];
            launchBalloon(xPosition);
        }


    }

    private void launchBalloon(int x) {
        Balloon balloon;
        Random rand = new Random();
        int n = rand.nextInt(100) + 1;

        Random rand2 = new Random();
        mNextColor = rand2.nextInt(7);
        if(n%2 == 0){
            balloon = new Balloon(this, mBalloonColors[mNextColor], 135);
        }
        else{
            balloon = new Balloon(this, mBalloonColors[mNextColor], 170, true);
        }

        mBalloons.add(balloon);


        balloon.setX(x);
        balloon.setY(mScreenHeight + balloon.getHeight());
        mContentView.addView(balloon);

        int duration = Math.max(MIN_ANIMATION_DURATION, MAX_ANIMATION_DURATION - (mLevel * 1000));
        balloon.releaseBalloon(mScreenHeight,duration, 0f);

    }
}
