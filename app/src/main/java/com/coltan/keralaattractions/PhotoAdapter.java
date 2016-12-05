package com.coltan.keralaattractions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Robo on 25-08-2016.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private static final String TAG = "PhotoAdapter";

    private ArrayList<Photo> mDataset;
    private ArrayList<String> mKeyList;
    private Context mContext;
    private Activity mActivity;
    private StorageReference storageRef;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.author);
            mImageView = (ImageView) v.findViewById(R.id.photo);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public PhotoAdapter(ArrayList<Photo> myDataset, ArrayList<String> keyList, Context context, Activity activity) {
        mDataset = myDataset;
        mKeyList = keyList;
        mContext = context;
        mActivity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);

        // Create a storage reference from app
        storageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://keralaattractions-fd4fe.appspot.com");

        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Photo photo = mDataset.get(position);
        final String photoKey = mKeyList.get(position);

        // Create a reference with an initial file path and name
        StorageReference pathReference = storageRef.child(photo.getPhotoRef());

        Glide.with(holder.mImageView.getContext())
                .using(new FirebaseImageLoader())
                .load(pathReference)
                .placeholder(R.color.placeholder)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(holder.mImageView);
        String titleAuthorString = String.format("By %1$s", photo.getAuthorName());
        holder.mTextView.setText(titleAuthorString);

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, DetailActivity.class);
                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(mActivity, holder.mImageView, mContext.getString(R.string.transition_photo));
                i.putExtra("photo", photo);
                i.putExtra("key", photoKey);
                mContext.startActivity(i, options.toBundle());
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
