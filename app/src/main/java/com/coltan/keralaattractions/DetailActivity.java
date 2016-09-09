package com.coltan.keralaattractions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Photo photo = getIntent().getExtras().getParcelable("photo");

        ImageView imgPhoto = (ImageView) findViewById(R.id.backdrop);
        TextView tvTitle = (TextView) findViewById(R.id.title);
        TextView tvPlace = (TextView) findViewById(R.id.place);
        TextView tvDescription = (TextView) findViewById(R.id.description);
        TextView tvAuthor = (TextView) findViewById(R.id.author);
        ImageView imgAuthor = (ImageView) findViewById(R.id.authorPic);

        Glide.with(this)
                .load(photo.getPhoto())
                .placeholder(R.color.placeholder)
                .centerCrop()
                .into(imgPhoto);

        tvTitle.setText(photo.getTitle());
        tvPlace.setText(photo.getPlace());
        tvDescription.setText(photo.getDescription());
        tvAuthor.setText(photo.getAuthorName());
        Glide.with(this)
                .load(photo.getAuthorPhotoUrl())
                .placeholder(R.color.placeholder)
                .into(imgAuthor);
    }
}
