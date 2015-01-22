package com.xxx.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xxx.adapter.TitleNavigationAdapter;
import com.xxx.adapter.VideoListAdapter;
import com.xxx.common.constant.CommonConstant;
import com.xxx.common.constant.ServiceEndpointConstant;
import com.xxx.domain.League;
import com.xxx.domain.Video;
import com.xxx.fragment.LeagueStandingFragment;
import com.xxx.fragment.MatchListFragment;
import com.xxx.fragment.TopAssistFragment;
import com.xxx.fragment.TopScorerFragment;
import com.xxx.util.DisplayImageOptionUtil;
import com.xxx.util.Util;
import com.xxx.view.PageViewerNoSwipe;
import com.xxx.vo.VideoListResponseVO;

public class ClubActivity extends BaseActivity implements ActionBar.OnNavigationListener{

	private String TAG = "ClubActivity";
	private PageViewerNoSwipe mPager; 
	private List<View> listViews;  
	private RelativeLayout t1, t2, t3;
	private String leagueId = "";
	private String leagueName = "";
	private int currentRound,maxRound;
	private int leagueIndex;
	static float density;
	final private Context context = this;
	private Typeface typeface_regular,typeface_light;
	
	private TextView fixture_textview,statistics_textview, video_textview;
	
	private SharedPreferences preferences;
	
	private List<Video> videoList;
	
	private DisplayImageOptions leagueOptions = DisplayImageOptionUtil.createEmptyImageOption();
	
	String[] leagueIdArray;
    String[] leagueNameArray;
    String[] leagueFMIDArray;
    String[] leagueCurrentRoundArray;
    String[] leagueMaxRoundArray;
    
    boolean firstTimeFlag = true;
    
    String matchDayDisplay;
    String[] matchDayDisplayArray;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.club_main);

		density = getResources().getDisplayMetrics().density;
		density = Util.normalizeDensity(density);
		
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		Intent intent=this.getIntent();
		
		leagueId = intent.getStringExtra("leagueId");
		
		leagueName = intent.getStringExtra("leagueName");
		
		currentRound = Integer.parseInt(intent.getStringExtra("currentRound"));
		
		maxRound = Integer.parseInt(intent.getStringExtra("maxRound"));
		
		leagueIndex = Integer.parseInt(intent.getStringExtra("leagueIndex"));
		
		matchDayDisplay = intent.getStringExtra("matchDayDisplay");
		
		if(matchDayDisplay!=null && !"".equals(matchDayDisplay)){
			matchDayDisplayArray = matchDayDisplay.split(",");
		}
		
		ActionBar actionBar = getSupportActionBar();
		getSupportActionBar().setTitle(leagueName); 
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
		typeface_regular = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeue.ttf");
		typeface_light = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLight.ttf");
		
        
        preferences = this.getSharedPreferences("com.xxx", Context.MODE_PRIVATE);
        String leagueIds = preferences.getString(CommonConstant.KEY_TOP_LEAGUE_IDS,"");
        String leagueNames = preferences.getString(CommonConstant.KEY_TOP_LEAGUE_NAMES,"");
        String leagueFMIDs = preferences.getString(CommonConstant.KEY_TOP_LEAGUE_FMIDS,"");
        String leagueCurrentRoundIDs = preferences.getString(CommonConstant.KEY_TOP_LEAGUE_CURRENTROUND,"");
        String leagueMaxRounds = preferences.getString(CommonConstant.KEY_TOP_LEAGUE_MAXROUND,"");
        
        leagueIdArray = leagueIds.split(",");
        leagueNameArray = leagueNames.split(",");
        leagueFMIDArray = leagueFMIDs.split(",");
        leagueCurrentRoundArray = leagueCurrentRoundIDs.split(",");
        leagueMaxRoundArray = leagueMaxRounds.split(",");
        
        List<League> leagueList = new ArrayList<League>();
        for(int i=0;i<leagueIdArray.length;i++){
        	League league = new League();
        	league.setLeagueId(Integer.parseInt(leagueIdArray[i]));
        	league.setName(leagueNameArray[i]);
        	league.setFmId(Integer.parseInt(leagueFMIDArray[i]));
        	leagueList.add(league);
        }
        
        fixture_textview = (TextView) findViewById(R.id.fixture_textview);
        statistics_textview = (TextView) findViewById(R.id.statistics_textview);
        video_textview = (TextView) findViewById(R.id.video_textview);
        
        fixture_textview.setTypeface(typeface_regular);
        statistics_textview.setTypeface(typeface_regular);
        video_textview.setTypeface(typeface_regular);
         
        // title drop down adapter
        TitleNavigationAdapter adapter = new TitleNavigationAdapter(getApplicationContext(), leagueList, imageLoader, leagueOptions);
        
        // assigning the spinner navigation    
        actionBar.setListNavigationCallbacks(adapter, this);
        actionBar.setSelectedNavigationItem(leagueIndex);
        
        initTextView();
		initViewPager();
        
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);   
	}

	@Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);   
	}
	
	private void initTextView() {
		t1 = (RelativeLayout) findViewById(R.id.tab1);
		t2 = (RelativeLayout) findViewById(R.id.tab2);
		t3 = (RelativeLayout) findViewById(R.id.tab3);
		
		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
	}

	private void initViewPager() {
		mPager = (PageViewerNoSwipe) findViewById(R.id.vPager);
		mPager.setPagingEnabled(false);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		listViews.add(mInflater.inflate(R.layout.match_list_collection, null));
		listViews.add(mInflater.inflate(R.layout.statistics_collection, null));
		listViews.add(mInflater.inflate(R.layout.league_video_list, null));
		mPager.setAdapter(new MyPagerAdapter(listViews));
		mPager.setCurrentItem(0);
		mPager.setOffscreenPageLimit(2);
		mPager.setOnPageChangeListener(new TabPageChangeListener());
	}
	
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}
		@Override
		public void finishUpdate(View arg0) {
		}
		@Override
		public int getCount() {
			return mListViews.size();
		}
		public int getItemPosition(Object object) {  
		    return POSITION_NONE;  
		} 

		@Override
		public Object instantiateItem(View viewPager, int pageIndex) {
			if (pageIndex < 4) {
				((ViewPager) viewPager).addView(mListViews.get(pageIndex % 4), 0);
			}
			if (pageIndex == 0) {
				loadMatchList();
			}
			if (pageIndex == 1) {
				loadStatistics();
			}
			if (pageIndex == 2) {
				String url = ServiceEndpointConstant.VIDEO_LIST_ENDPOINT+"leagueId="+leagueId+"&offset=0&limit=20";
				AsyncHttpClient client = new AsyncHttpClient();
				client.get(url, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
//						loadingProgressBar.setVisibility(View.INVISIBLE);
						Gson gson = new Gson();
						VideoListResponseVO res = gson.fromJson(
								response, VideoListResponseVO.class);
						if(res!=null && res.getVideoList()!=null){
							videoList = res.getVideoList();
							filloutVideoData();
						}
					}

					@Override
					public void onFailure(java.lang.Throwable t) {
//						loadingProgressBar.setVisibility(View.INVISIBLE);
						String errorMSG = "Failed to get player list from server.";
						Log.e(TAG, errorMSG + " Error: " + t.getMessage(), t);
						Toast.makeText(getBaseContext(), errorMSG, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
			if(pageIndex == 3){ 
				
			}
			return mListViews.get(pageIndex % 4);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.i(TAG, "onPause...");
		resumedFlag = true;
	}
	
	boolean resumedFlag = false;
	
	@Override
	public void onResume(){
		super.onResume();
		Log.i(TAG, "onResume...");
		if(resumedFlag){
			resumedFlag = false;
		}
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    setIntent(intent); //now getIntent() should always return the last received intent
	}
	
	public class TabPageChangeListener implements OnPageChangeListener {
//		View tab1_bottom = (View) findViewById(R.id.tab1_bottom);
//		View tab2_bottom = (View) findViewById(R.id.tab2_bottom);
		
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				fixture_textview.setTextColor(Color.parseColor("#2f9658"));
		        statistics_textview.setTextColor(Color.parseColor("#ffffff"));
		        video_textview.setTextColor(Color.parseColor("#ffffff"));
		        
				break;
			case 1:
				statistics_textview.setTextColor(Color.parseColor("#2f9658"));
				fixture_textview.setTextColor(Color.parseColor("#ffffff"));
				video_textview.setTextColor(Color.parseColor("#ffffff"));
				break;
			case 2:
				statistics_textview.setTextColor(Color.parseColor("#ffffff"));
				fixture_textview.setTextColor(Color.parseColor("#ffffff"));
				video_textview.setTextColor(Color.parseColor("#2f9658"));
				break;
			}
		}
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		public void onPageScrollStateChanged(int arg0) {
		}
	}
	
	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;
		public MyOnClickListener(int i) {
			index = i;
		}
		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	};
	
	
	String selectedRadio = "";
	
	String match_selected_radio_round = "";
	
	MatchListFragment lastFragment;
	MatchListFragment currentFragment;
	MatchListFragment nextFragment;
	
	private void loadMatchList(){
		ViewPager mViewPager = (ViewPager) findViewById(R.id.match_pager);
		
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.match_pager_tab_strip);
//		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.blue_overlay));
		
		for (int i = 0; i < pagerTabStrip.getChildCount(); ++i) {
		    View nextChild = pagerTabStrip.getChildAt(i);
		    if (nextChild instanceof TextView) {
		       TextView textViewToConvert = (TextView) nextChild;
		       textViewToConvert.setTypeface(typeface_light);
		    }
		}
		
		MatchListPagerAdapter pagerAdapter = new MatchListPagerAdapter(getSupportFragmentManager(),maxRound,currentRound, Integer.parseInt(leagueId),matchDayDisplayArray);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setCurrentItem(currentRound-1);
	}
	
	private void loadStatistics(){
		ViewPager mViewPager = (ViewPager) findViewById(R.id.statistic_pager);
		
		PagerTabStrip pagerTabStrip = (PagerTabStrip) findViewById(R.id.statistics_pager_tab_strip);
//		pagerTabStrip.setDrawFullUnderline(true);
		pagerTabStrip.setTabIndicatorColor(getResources().getColor(R.color.blue_overlay));
		
		for (int i = 0; i < pagerTabStrip.getChildCount(); ++i) {
		    View nextChild = pagerTabStrip.getChildAt(i);
		    if (nextChild instanceof TextView) {
		       TextView textViewToConvert = (TextView) nextChild;
		       textViewToConvert.setTypeface(typeface_light);
		    }
		}
		
		StatisticsPagerAdapter pageAdapter = new StatisticsPagerAdapter(getSupportFragmentManager(),Integer.parseInt(leagueId));
        mViewPager.setAdapter(pageAdapter);
        mViewPager.setCurrentItem(0);
	}
	
	public static class MatchListPagerAdapter extends FragmentStatePagerAdapter {

    	private int maxRound;
    	private int currentRound;
    	private int leagueId;
    	private String[] matchDisplayArray;
    	
        public MatchListPagerAdapter(FragmentManager fm, int maxRound,int currentRound, int leagueId,
        		String[] matchDisplayArray) {
            super(fm);
        	this.maxRound = maxRound;
        	this.currentRound = currentRound;
        	this.leagueId = leagueId;
        	this.matchDisplayArray = matchDisplayArray;
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new MatchListFragment();
            Bundle args = new Bundle();
            args.putString("currentRound", (i+1)+""); 
            args.putString("leagueId", ""+leagueId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return maxRound;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	if(matchDisplayArray!=null){
        		return matchDisplayArray[position];
        	}else{
        		if(currentRound==(position+1)){
            		return "Current";
            	}else{
            		return "Matchday " + (position + 1);
            	}
        	}
        }
    }
	
	public static class StatisticsPagerAdapter extends FragmentStatePagerAdapter {

    	private int leagueId;
    	
        public StatisticsPagerAdapter(FragmentManager fm, Integer leagueId) {
            super(fm);
        	this.leagueId = leagueId;
        }

        @Override
        public Fragment getItem(int i) {
        	if(i==0){
                Fragment fragment = new LeagueStandingFragment();
                Bundle args = new Bundle();
                args.putString("leagueId", ""+leagueId);
                args.putFloat("density", density);
                fragment.setArguments(args);
                return fragment;
        	}else if(i==1){
        		Fragment fragment = new TopScorerFragment();
                Bundle args = new Bundle();
                args.putString("leagueId", ""+leagueId);
                args.putFloat("density", density);
                fragment.setArguments(args);
                return fragment;
        	}else{
        		Fragment fragment = new TopAssistFragment();
                Bundle args = new Bundle();
                args.putString("leagueId", ""+leagueId);
                args.putFloat("density", density);
                fragment.setArguments(args);
                return fragment;
        	}
            
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String title = "";
        	if(position==0){
        		title="Standing";
        	}else if(position==1){
        		title = "Top Scorers";
        	}else{
        		title="Top Assists";
        	}
        	return title;
        }
    }
 
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    	if(firstTimeFlag){
    		firstTimeFlag = false;
    	}else{
    		currentRound = Integer.parseInt(leagueCurrentRoundArray[itemPosition]);
    		maxRound = Integer.parseInt(leagueMaxRoundArray[itemPosition]);
    		leagueId = leagueIdArray[itemPosition];
    		initViewPager();
    	}
        return true;
    }
    
    private void filloutVideoData(){
    	if(videoList.size()>0){
    		ListView video_list_view = (ListView) findViewById(R.id.video_list_view);
    		VideoListAdapter adapter=new VideoListAdapter(this, videoList, video_list_view, 0, density);
    		video_list_view.setAdapter(adapter);
	        
    		video_list_view.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
						long arg3) {
					Intent intent = new Intent(arg0.getContext(),
							VideoPlayerActivity.class);
					Video video = videoList.get(arg2);
					if(video!=null){
						intent.putExtra("videoURL",video.getUrl());  
					}
					startActivity(intent);

				}
			});
    	}
    }
}
