package com.example.hp_pc.swipecardex;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.huxq17.swipecardsview.BaseCardAdapter;
import com.squareup.picasso.Picasso;

import java.util.List;


public class CardAdapter extends BaseCardAdapter {

    private List<News> news;
    private Context context;

    public CardAdapter(List<News> news, Context context) {
        this.news = news;
        this.context = context;
    }

    @Override
    public int getCount() {
        return news.size();
    }

    @Override
    public int getCardLayoutId() {
        return R.layout.card_item;
    }

    @Override
    public void onBindData(int position, View itemView) {
        ImageView imageView;
        TextView title, desc, author,wholeStory;
        imageView = (ImageView) itemView.findViewById(R.id.image);
        title = (TextView) itemView.findViewById(R.id.title);
        desc = (TextView) itemView.findViewById(R.id.desc);
        author = (TextView) itemView.findViewById(R.id.textView2);
        wholeStory= (TextView) itemView.findViewById(R.id.whole_story);
        if (news != null && news.size() != 0) {
            final News mNews = news.get(position);
            String a=mNews.getAuthor();
            Picasso.with(context).load(mNews.getImageUrl()).into(imageView);
            title.setText(mNews.getTitle());
            desc.setText(mNews.getDesc());
            itemView.setTag(R.id.whole_story,mNews.getUrl());
//            itemView.setTag(mNews.getUrl());
//            Log.v("nhn",itemView.getTag().toString());
            if (a.contentEquals("null"))
                author.setText("The Times Of India");

            else author.setText(mNews.getAuthor());
        }
    }
}
