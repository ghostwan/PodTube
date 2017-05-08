package com.ghostwan.podtube.feed;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.ghostwan.podtube.R;

/**
 * Created by erwan on 08/05/2017.
 */
public class CViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.name)
    TextView name;

    @BindView(R.id.list_icon)
    ImageView image;

    @BindView(R.id.item_main_layout)
    RelativeLayout layout;

    public CViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
