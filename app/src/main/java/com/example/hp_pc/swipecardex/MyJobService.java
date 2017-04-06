package com.example.hp_pc.swipecardex;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MyJobService extends JobService {
    NotificationCompat.Builder notification;
    int uniqueID = 101;
    private static final String CRIC_API_MATCHES_BASE_URL = "https://newsapi.org/v1/articles";
    private static final String API_KEY = "541743e949524b4c9631d0fa0686e080";
    private static String source;
    private static final String SORT_BY = "top";
    private SharedPreferences spf;
    List<News> news;
    public String title, desc;
    private AsyncTask<String, String, List<News>> bgTask;

    @Override
    public boolean onStartJob(final JobParameters job) {
        spf = PreferenceManager.getDefaultSharedPreferences(this);
        source = spf.getString(getString(R.string.key_news_type), "the-times-of-india");
        bgTask = new AsyncTask<String, String, List<News>>() {
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
            protected void onPostExecute(List<News> newses) {
                super.onPostExecute(newses);
                News singleNews = newses.get(0);
                title = singleNews.getTitle();
                desc = singleNews.getDesc();
                notification = new NotificationCompat.Builder(getApplicationContext());
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                notification.setAutoCancel(true)
                        .setSmallIcon(R.drawable.ic_stat_social_whatshot)
                        .setWhen(System.currentTimeMillis())
                        .setContentTitle("Trending Stories")
                        .setContentText(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(title+" & More Stories to read!!"))
                        .setVibrate(new long[] { 1000, 1000, 1000, 3000, 1000 })
                        .setContentIntent(pendingIntent);

                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                nm.notify(uniqueID, notification.build());
                jobFinished(job, false);
            }
        };
        bgTask.execute(CRIC_API_MATCHES_BASE_URL);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return false;
    }
}