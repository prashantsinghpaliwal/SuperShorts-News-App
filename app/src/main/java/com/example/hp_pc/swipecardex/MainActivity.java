package com.example.hp_pc.swipecardex;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.RetryStrategy;
import com.firebase.jobdispatcher.Trigger;
import com.huxq17.swipecardsview.SwipeCardsView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String CRIC_API_MATCHES_BASE_URL = "https://newsapi.org/v1/articles";
    private static final String API_KEY = "541743e949524b4c9631d0fa0686e080";
    private static String source;
    private static final String SORT_BY = "top";
    SwipeCardsView swipeCardsView;
    private ProgressBar progressBar;
    private SharedPreferences spf;
    List<News> news;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeCardsView = (SwipeCardsView) findViewById(R.id.swipCardsView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        spf = PreferenceManager.getDefaultSharedPreferences(this);
        source = spf.getString(getString(R.string.key_news_type), "the-times-of-india");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        progressBar.setVisibility(View.VISIBLE);
        swipeCardsView.retainLastCard(true);
        swipeCardsView.enableSwipe(true);
        dispatchJob();
        new MyTask().execute(CRIC_API_MATCHES_BASE_URL);
    }

    private void dispatchJob() {
        Driver driver = new GooglePlayDriver(this);
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(driver);
        Job notifyJob = jobDispatcher.newJobBuilder()
                .setService(MyJobService.class)
                .setTag("notify-tag")
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setLifetime(Lifetime.FOREVER)
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(3600 * 4, 3600 * 4 + 100))
                .setReplaceCurrent(true)
                .setRetryStrategy(RetryStrategy.DEFAULT_EXPONENTIAL)
                .build();
        jobDispatcher.schedule(notifyJob);
    }

    @Override
    protected void onResume() {
        super.onResume();
        spf = PreferenceManager.getDefaultSharedPreferences(this);
        source = spf.getString(getString(R.string.key_news_type), "the-times-of-india");
        progressBar.setVisibility(View.VISIBLE);
        new MyTask().execute(CRIC_API_MATCHES_BASE_URL);
    }

    private class MyTask extends AsyncTask<String, String, List<News>> {
        @Override
        protected List<News> doInBackground(String... params) {
            Uri baseUri = Uri.parse(params[0]);
            Uri.Builder uriBuilder = baseUri.buildUpon();
            uriBuilder.appendQueryParameter("apiKey", API_KEY);
            uriBuilder.appendQueryParameter("source", source);
            uriBuilder.appendQueryParameter("sortBy", SORT_BY);
            URL url = null;
            try {

                url = new URL(uriBuilder.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            assert url != null;
            Log.v("url is:", url.toString());
            news = JSONFromServer.fetchEarthquakeData(url.toString());
            return news;
        }

        @Override
        protected void onPostExecute(List<News> news) {
            super.onPostExecute(news);
            progressBar.setVisibility(View.INVISIBLE);
            CardAdapter cardAdapter = new CardAdapter(news, MainActivity.this);
            swipeCardsView.setAdapter(cardAdapter);
            swipeCardsView.setCardsSlideListener(new SwipeCardsView.CardsSlideListener() {
                @Override
                public void onShow(int index) {

                }

                @Override
                public void onCardVanish(int index, SwipeCardsView.SlideType type) {

                }

                @Override
                public void onItemClick(View cardImageView, int index) {
                    String url = String.valueOf(cardImageView.getTag(R.id.whole_story));
                    Log.v("lll", url);
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    Bundle bndlanimation = ActivityOptions.makeCustomAnimation(getApplicationContext(),
                            R.anim.animation, R.anim.animation2).toBundle();
                    startActivity(webIntent, bndlanimation);
                }
            });

            progressBar.setVisibility(View.INVISIBLE);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                Intent settings = new Intent(this, SettingsActivity.class);
                startActivity(settings);
        }
        return super.onOptionsItemSelected(item);
    }
}
