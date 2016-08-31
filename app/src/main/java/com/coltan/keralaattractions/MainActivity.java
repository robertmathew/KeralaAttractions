package com.coltan.keralaattractions;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ActionMenuView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coltan.keralaattractions.ui.GridMarginDecoration;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private int[] mDataset = {R.drawable.photo1, R.drawable.photo2, R.drawable.photo3, R.drawable.photo4,
            R.drawable.photo5, R.drawable.photo6, R.drawable.photo7, R.drawable.photo8};
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState == null) {
            animateToolbar();
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.upload:
                Intent intent = new Intent(this, UploadPhotoActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void animateToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            // this is gross but toolbar doesn't expose it's children to animate them :(
            View t = toolbar.getChildAt(0);
            if (t != null && t instanceof TextView) {
                TextView title = (TextView) t;

                // fade in and space out the title.  Animating the letterSpacing performs horribly so
                // fake it by setting the desired letterSpacing then animating the scaleX ¯\_(ツ)_/¯
                title.setAlpha(0f);
                title.setScaleX(0.8f);

                title.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .setStartDelay(300)
                        .setDuration(900)
                        .setInterpolator(AnimUtils.getFastOutSlowInInterpolator(this));
            }
            View amv = toolbar.getChildAt(1);
            if (amv != null & amv instanceof ActionMenuView) {
                ActionMenuView actions = (ActionMenuView) amv;
                popAnim(actions.getChildAt(0), 500, 200); // filter
                popAnim(actions.getChildAt(1), 700, 200); // overflow
            }
        }
    }

    private void popAnim(View v, int startDelay, int duration) {
        if (v != null) {
            v.setAlpha(0f);
            v.setScaleX(0f);
            v.setScaleY(0f);

            v.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setStartDelay(startDelay)
                    .setDuration(duration)
                    .setInterpolator(AnimationUtils.loadInterpolator(this,
                            android.R.interpolator.overshoot));
        }
    }
}
