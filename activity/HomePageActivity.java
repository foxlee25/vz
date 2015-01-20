package com.vizoal.activity;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.vizoal.adapter.HomepageViewPagerAdapter;
import com.vizoal.common.constant.GCMConstant;
import com.vizoal.domain.HomepagePlayer;
import com.vizoal.service.GcmServerUtilities;
import com.vizoal.util.Util;

public class HomePageActivity extends BaseActivity {

	private String TAG = "HomePageActivity";
	private SharedPreferences prefs ;
	
	private ViewPager mPager; 
	private HomepageViewPagerAdapter mViewPagerAdapter;
	final Context context = this;
	
//	private ListView drawer_lv;
	private List<HomepagePlayer> playerList;
	
	private ImageView image_dot1, image_dot2, image_dot3, image_dot4;
	
	private TextView playerNameTextView;
//	private DrawerLayout mDrawerLayout;
//    private ActionBarDrawerToggle mDrawerToggle;
	
	float screenWidth,screenHeight,density;
    
	String homepageImageResolution; 
	
	String notificationFlag;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent=this.getIntent();
		
		String fromNotification = intent.getStringExtra("fromNotification");
		if("yes".equals(fromNotification)){
			fakeLoadingHomePage = true;
			String matchId = intent.getStringExtra("matchId");
			Intent notificationIntent = new Intent(context, TopLeagueListActivity.class);
			notificationIntent.putExtra("matchId",matchId); 
			notificationIntent.putExtra("fromNotification","yes");
//			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Clean the activity stack
	        startActivity(notificationIntent);
//	        finish();
		}else{
			loadHomePage();
		}
	}
	
	boolean fakeLoadingHomePage = false;
	
	@Override
	public void onResume() {
		Log.v(TAG, "onResume");
		if(fakeLoadingHomePage){
			fakeLoadingHomePage = false;
			loadHomePage();
		}
		super.onResume();
	}
	
	
	private void loadHomePage(){
		setContentView(R.layout.homepage);
		
		Typeface typeface_corbert = Typeface.createFromAsset(context.getAssets(), "fonts/Corbert-Regular.otf");
//		Typeface typeface_thin = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");
//		Typeface typeface_light = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
//		Typeface typeface_regular = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");
//		Typeface typeface_medium = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");
		
		
		ActionBar ab = getSupportActionBar();
		ab.setTitle("XXX");
		ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowTitleEnabled(true);

//        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
//        TextView abTextView = (TextView)findViewById(titleId);
//        abTextView.setTypeface(typeface_corbert);

		DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        density = dm.density;
        
        
        registerGCM();
        
        homepageImageResolution = Util.getHomepageImageResolution(screenWidth);
        
        
        
		try{
			Button top_player_button = (Button)findViewById(R.id.top_players_btn);
			top_player_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
				    Intent intent = new Intent(context, PlayerListActivity.class);
				    intent.putExtra("player_list_title", "Top Players");
	                startActivity(intent);   
				}
			});
			
			Button browse_btn = (Button)findViewById(R.id.competitions_btn);
			
			top_player_button.setTypeface(typeface_corbert);
			browse_btn.setTypeface(typeface_corbert);
			
			playerNameTextView = (TextView)findViewById(R.id.playerNameTextView);
			playerNameTextView.setTypeface(typeface_corbert);
			
			browse_btn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
				    Intent intent = new Intent(context, TopLeagueListActivity.class);
	                            startActivity(intent);   
				}
			});
			
		initPlayerList();
		
		//Fixed the player name not loaded in the first page
		if(playerList!=null && playerList.size()>0){
			HomepagePlayer firstHomepagePlayer = playerList.get(0);
			if(firstHomepagePlayer!=null){
				playerNameTextView.setText(firstHomepagePlayer.getName());
			}
		}
		
		initViewPager();
		} finally { };
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
	
	@Override
    protected void onDestroy() {
		GCMRegistrar.onDestroy(this);
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    // Checks whether a hardware keyboard is available
	    if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
	        Toast.makeText(this, "keyboard visible", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
	        Toast.makeText(this, "keyboard hidden", Toast.LENGTH_SHORT).show();
	    }
	    // Nav Drawer
//	    mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	}

	private void initPlayerList(){
    	prefs = this.getSharedPreferences("com.xxx", Context.MODE_PRIVATE);
		String homepagePlayerStr = prefs.getString(SplashActivity.homepagePlayerKey,"");
		
		Log.i(TAG, homepagePlayerStr);
		if(homepagePlayerStr!=null && !"".equals(homepagePlayerStr)){
			String[] strArray = homepagePlayerStr.split(";");
			playerList = new ArrayList<HomepagePlayer>();
			for(int i=0;i<strArray.length;i++){
				String[] playerArray = strArray[i].split(",");
				HomepagePlayer homepagePlayer = new HomepagePlayer(Integer.valueOf(playerArray[2]),playerArray[0],playerArray[1]);
				playerList.add(homepagePlayer);
			}
		}
    }
	
	private void initViewPager() {
		image_dot1 = (ImageView) findViewById(R.id.image_dot1);
		image_dot2 = (ImageView) findViewById(R.id.image_dot2);
		image_dot3 = (ImageView) findViewById(R.id.image_dot3);
		image_dot4 = (ImageView) findViewById(R.id.image_dot4);
		
		mPager = (ViewPager) findViewById(R.id.vPager);
		mPager.setOffscreenPageLimit(1);
		mViewPagerAdapter = new HomepageViewPagerAdapter(this, playerList, screenWidth);
		mPager.setAdapter(mViewPagerAdapter);
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyPageChangeListener(mPager));
		mPager.setOffscreenPageLimit(1);
	}

	public class MyPageChangeListener implements OnPageChangeListener {

		ViewPager mViewPager;

		public MyPageChangeListener(ViewPager mViewPager) {
			super();
			this.mViewPager = mViewPager;
		}

		@Override
		public void onPageSelected(int index) {
			System.out.println("onPageSelected: " + index);
			if(playerList!=null && playerList.get(index)!=null){
				playerNameTextView.setText(playerList.get(index).getName());
			}
			switch (index) {
			case 0: 
				image_dot1.setImageResource(R.drawable.dot_selected);
				image_dot2.setImageResource(R.drawable.dot_unselected);
				image_dot3.setImageResource(R.drawable.dot_unselected);
				image_dot4.setImageResource(R.drawable.dot_unselected);
				break;
			case 1:
				image_dot1.setImageResource(R.drawable.dot_unselected);
				image_dot2.setImageResource(R.drawable.dot_selected);
				image_dot3.setImageResource(R.drawable.dot_unselected);
				image_dot4.setImageResource(R.drawable.dot_unselected);
				break;
			case 2:
				image_dot1.setImageResource(R.drawable.dot_unselected);
				image_dot2.setImageResource(R.drawable.dot_unselected);
				image_dot3.setImageResource(R.drawable.dot_selected);
				image_dot4.setImageResource(R.drawable.dot_unselected);
				break;
			case 3:
				image_dot1.setImageResource(R.drawable.dot_unselected);
				image_dot2.setImageResource(R.drawable.dot_unselected);
				image_dot3.setImageResource(R.drawable.dot_unselected);
				image_dot4.setImageResource(R.drawable.dot_selected);
				break;
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	} 
	
	public Context getAppContext(){
		return getApplicationContext(); 
	}

	public void registerGCM(){
		try{
			GCMRegistrar.checkDevice(this);
			// Make sure the manifest was properly set - comment out this line
	        // while developing the app, then uncomment it when it's ready.
	        GCMRegistrar.checkManifest(this);
	        String regId = GCMRegistrar.getRegistrationId(this);
	        if (regId.equals("")) {
	            // Automatically registers application on startup.
	            GCMRegistrar.register(this, GCMConstant.SENDER_ID);
	            regId = GCMRegistrar.getRegistrationId(this);
	        } else {
	            // Device is already registered on GCM, check server.
//	            if (GCMRegistrar.isRegisteredOnServer(this)) {
	            	try {
	        			GcmServerUtilities.register(context, regId);
	        		} catch (UnsupportedEncodingException e) {
	        			Log.e(TAG, e.getMessage());
	        		} catch (JSONException e) {
	        			Log.e(TAG, e.getMessage());
	        		}
//	            }
	            Log.i(TAG, "registeredId: "+regId);
	        }
		}catch(Exception e){
			Log.i(TAG, "not able to register gcm. Error is: "+e.getMessage());
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.homepage_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
    
}
