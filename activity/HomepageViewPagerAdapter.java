package com.vizoal.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xxx.domain.HomepagePlayer;
import com.xxx.view.HomePageItemView;

public class HomepageViewPagerAdapter extends PagerAdapter {

	private String TAG = "HomepageViewPagerAdapter";
	
	private Context mContext;

	private List<HomepagePlayer> playerList;

	private HashMap<Integer, HomePageItemView> mHashMap;
	
	private double screenWidth;
	
	private ImageLoader imageLoader;
	private DisplayImageOptions options;

	public HomepageViewPagerAdapter(Context context, List<HomepagePlayer> playerList, double screenWidth,
			ImageLoader imageLoader, DisplayImageOptions options) {
		this.mContext = context;
		this.playerList = playerList;
		this.screenWidth = screenWidth;
		mHashMap = new HashMap<Integer, HomePageItemView>();
		
		this.imageLoader = imageLoader;
		this.options = options;
	}
 
	@Override
	public void destroyItem(View container, int position, Object object) {
		Log.i(TAG, "destroyItem..."+position); 
		HomePageItemView itemView = (HomePageItemView) object;
		itemView.recycle();
	}

	@Override
	public void finishUpdate(View view) {

	}

	@Override
	public int getCount() {
		return playerList.size();
	}

	@Override
	public Object instantiateItem(View container, int position) {
//		Log.i(TAG, "instantiateItem..."+position); 
		
		HomePageItemView itemView;
		if (mHashMap.containsKey(position)) {
			itemView = mHashMap.get(position);
			itemView.setData(playerList.get(position),screenWidth,imageLoader, options);
		} else {
			itemView = new HomePageItemView(mContext);
			mHashMap.put(position, itemView);
			((ViewPager)container).addView(itemView);
			itemView.setData(playerList.get(position),screenWidth,imageLoader, options);
		}

		return itemView;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {

	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View view) {

	}

	public HashMap<Integer, HomePageItemView> getmHashMap() {
		return mHashMap;
	}

	public void setmHashMap(HashMap<Integer, HomePageItemView> mHashMap) {
		this.mHashMap = mHashMap;
	}
}

