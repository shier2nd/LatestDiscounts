package com.shier2nd.android.latestdiscounts;

import android.net.Uri;

/**
 * Created by Woodinner on 5/30/16.
 */
public class DiscountItem {
    private String mChannelId;
    private String mDiscountId;
    private String mPicUrl;
    private String mTitle;
    private String mPrice;
    private String mDate;
    private String mMall;
    private String mCommentNum;
    private String mWorthyNum;
    private String mUnworthyNum;
    private String mTimeSort;

    public String getChannelId() {
        return mChannelId;
    }

    public void setChannelId(String channelId) {
        mChannelId = channelId;
    }

    public String getDiscountId() {
        return mDiscountId;
    }

    public void setDiscountId(String discountId) {
        mDiscountId = discountId;
    }

    public String getPicUrl() {
        return mPicUrl;
    }

    public void setPicUrl(String picUrl) {
        mPicUrl = picUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getPrice() {
        return mPrice;
    }

    public void setPrice(String price) {
        mPrice = price;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getMall() {
        return mMall;
    }

    public void setMall(String mall) {
        mMall = mall;
    }

    public String getCommentNum() {
        return mCommentNum;
    }

    public void setCommentNum(String commentNum) {
        mCommentNum = commentNum;
    }

    public String getWorthyNum() {
        return mWorthyNum;
    }

    public void setWorthyNum(String worthyNum) {
        mWorthyNum = worthyNum;
    }

    public String getUnworthyNum() {
        return mUnworthyNum;
    }

    public void setUnworthyNum(String unworthyNum) {
        mUnworthyNum = unworthyNum;
    }

    public String getTimeSort() {
        return mTimeSort;
    }

    public void setTimeSort(String timeSort) {
        mTimeSort = timeSort;
    }

    public String getDateAndMall() {
        if (mMall != null && !mMall.equals("")) {
            return mMall + " | " + mDate;
        } else {
            return mDate;
        }
    }

    public String getWorthyRate() {
        int worthyNum = Integer.parseInt(mWorthyNum);
        int unWorthyNum = Integer.parseInt(mUnworthyNum);
        if ((worthyNum + unWorthyNum) != 0) {
            int worthyRate = worthyNum * 100 / (worthyNum + unWorthyNum);
            return String.valueOf(worthyRate) + "%";
        } else {
            return "0";
        }
    }

    public Uri getDiscountUri() {
        return Uri.parse("http://www.smzdm.com/p/")
                .buildUpon()
                .appendPath(mDiscountId)
                .build();
    }
}
