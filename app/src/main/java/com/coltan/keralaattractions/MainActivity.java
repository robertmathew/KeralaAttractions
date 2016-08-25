package com.coltan.keralaattractions;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.coltan.keralaattractions.ui.GridMarginDecoration;

public class MainActivity extends Activity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private int[] mDataset = {R.drawable.photo1, R.drawable.photo2, R.drawable.photo3,R.drawable.photo4,
            R.drawable.photo5, R.drawable.photo6, R.drawable.photo7, R.drawable.photo8};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.image_grid);
        mProgressBar = (ProgressBar) findViewById(android.R.id.empty);


        setupRecyclerView();
        populateGrid();
    }

    private void setupRecyclerView() {
        GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                /* emulating https://material-design.storage.googleapis.com/publish/material_v_4/material_ext_publish/0B6Okdz75tqQsck9lUkgxNVZza1U/style_imagery_integration_scale1.png */
                switch (position % 6) {
                    case 5:
                        return 3;
                    case 3:
                        return 2;
                    default:
                        return 1;
                }
            }
        });
        mRecyclerView.addItemDecoration(new GridMarginDecoration(
                getResources().getDimensionPixelSize(R.dimen.grid_item_spacing)));
        mRecyclerView.setHasFixedSize(true);

    }

    private void populateGrid() {
        mRecyclerView.setAdapter(new PhotoAdapter(mDataset));
        mProgressBar.setVisibility(View.GONE);
    }
}
