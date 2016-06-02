package com.shier2nd.android.latestdiscounts;

import android.content.Context;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Woodinner on 5/30/16.
 */
public class DiscountAdapter extends RecyclerView.Adapter {
    private final int VIEW_PROGRESS = 0;
    private final int VIEW_ITEM = 1;

    private List<DiscountItem> mDiscountItems;
    private Context mContext;

    public DiscountAdapter(List<DiscountItem> discountItems) {
        mDiscountItems = discountItems;
    }

    @Override
    public int getItemViewType(int position) {
        return mDiscountItems.get(position) != null ? VIEW_ITEM : VIEW_PROGRESS;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();

        View itemView;
        RecyclerView.ViewHolder viewHolder;

        switch (viewType) {
            case VIEW_PROGRESS:
                itemView = LayoutInflater
                        .from(mContext)
                        .inflate(R.layout.progressbar_load_more, parent, false);
                viewHolder = new ProgressViewHolder(itemView);
                break;
            case VIEW_ITEM:
                itemView = LayoutInflater
                        .from(mContext)
                        .inflate(R.layout.card_discounts_item, parent, false);
                viewHolder = new DiscountViewHolder(itemView,
                        new DiscountViewHolder.ClickResponseListener() {
                            @Override
                            public void onWholeClick(int position) {
                                browse(mContext, position);
                            }

                            @Override
                            public void onOverflowClick(View v, int position) {
                                final int pos = position;
                                PopupMenu popup = new PopupMenu(mContext, v);
                                MenuInflater inflater = popup.getMenuInflater();
                                inflater.inflate(R.menu.contextual_discount_list, popup.getMenu());
                                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        switch (item.getItemId()) {
                                            case R.id.action_share_url:
                                                share(mContext, pos);
                                                break;
                                        }
                                        return true;
                                    }
                                });
                                popup.show();
                            }
                        });
                break;
            default:
                viewHolder = null;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DiscountViewHolder) {
            DiscountItem discountItem = mDiscountItems.get(position);
            ((DiscountViewHolder) holder).bindDiscountItem(discountItem, mContext);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return mDiscountItems.size();
    }

    private void browse(Context context, int position) {}

    private void share(Context context, int position) {}

    public static class DiscountViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public ImageView productImage;
        public TextView productTitle;
        public TextView productPrice;
        public TextView productDateAndMall;
        public TextView productCommentNum;
        public TextView productWorthyRate;
        public ImageView overflow;

        private DiscountItem mDiscountItem;

        private ClickResponseListener mClickResponseListener;

        public DiscountViewHolder(View itemView, ClickResponseListener clickResponseListener) {
            super(itemView);

            mClickResponseListener = clickResponseListener;

            productImage = (ImageView) itemView.findViewById(R.id.thumbnail_image);
            productTitle = (TextView) itemView.findViewById(R.id.product_title);
            productPrice = (TextView) itemView.findViewById(R.id.product_price);
            productDateAndMall = (TextView) itemView.findViewById(R.id.product_mall_and_date);
            productCommentNum = (TextView) itemView.findViewById(R.id.product_comments);
            productWorthyRate = (TextView) itemView.findViewById(R.id.product_worthy);
            overflow = (ImageView) itemView.findViewById(R.id.card_share_overflow);

            itemView.setOnClickListener(this);
            overflow.setOnClickListener(this);
        }

        public void bindDiscountItem(DiscountItem discountItem, Context context) {
            mDiscountItem = discountItem;

            Picasso.with(context)
                    .load(mDiscountItem.getPicUrl())
                    .into(productImage);

            productTitle.setText(mDiscountItem.getTitle());
            productPrice.setText(mDiscountItem.getPrice());

            productDateAndMall.setText(mDiscountItem.getDateAndMall());

            productCommentNum.setText(mDiscountItem.getCommentNum());
            productWorthyRate.setText(mDiscountItem.getWorthyRate());
        }

        @Override
        public void onClick(View v) {
            if (v == overflow) {
                mClickResponseListener.onOverflowClick(v, getAdapterPosition());
            } else {
                mClickResponseListener.onWholeClick(getAdapterPosition());
            }
        }

        public interface ClickResponseListener {
            void onWholeClick(int position);

            void onOverflowClick(View v, int position);
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progress_bar_load_more);
        }
    }
}
