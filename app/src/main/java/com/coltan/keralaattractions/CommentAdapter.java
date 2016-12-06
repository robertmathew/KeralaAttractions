package com.coltan.keralaattractions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by robo on 25/11/16.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private ArrayList<Comment> mDataset;
    private Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView mAuthorImageView;
        public TextView mNameTextView;
        public TextView mCommentTextView;
        public TextView mTimeTextView;

        public ViewHolder(View v) {
            super(v);
            mAuthorImageView = (CircleImageView) v.findViewById(R.id.authorImage);
            mNameTextView = (TextView) v.findViewById(R.id.authorName);
            mCommentTextView = (TextView) v.findViewById(R.id.comment);
            mTimeTextView = (TextView) v.findViewById(R.id.time);
        }
    }

    public CommentAdapter(ArrayList<Comment> dataset, Context context) {
        mDataset = dataset;
        mContext = context;
    }

    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(CommentAdapter.ViewHolder holder, int position) {
        Comment comment = mDataset.get(position);
        holder.mNameTextView.setText(comment.getCommenterName());
        holder.mCommentTextView.setText(comment.getComment());
        if (comment.getCommenterPhotoUrl() != null) {
            Glide.with(mContext)
                    .load(comment.getCommenterPhotoUrl())
                    .into(holder.mAuthorImageView);
        }
        holder.mTimeTextView.setText(
                DateUtils.getRelativeTimeSpanString(Long.parseLong(comment.getMillisecond()),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS).toString().toLowerCase()
        );
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
