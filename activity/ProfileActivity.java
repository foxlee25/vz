package com.vizoal.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.vizoal.common.constant.CommonConstant;
import com.vizoal.common.constant.ServiceEndpointConstant;
import com.vizoal.domain.Player;
import com.vizoal.domain.PlayerComment;
import com.vizoal.domain.PlayerCommentListAll;
import com.vizoal.domain.PlayerNationality;
import com.vizoal.domain.PlayerPosition;
import com.vizoal.domain.PlayerStatistics;
import com.vizoal.util.DensityUtils;
import com.vizoal.util.DisplayImageOptionUtil;
import com.vizoal.util.PlayerPositonCalculator;
import com.vizoal.util.Util;
import com.vizoal.view.PullToRefreshListView;
import com.vizoal.vo.CommentListAllResponseVO;
import com.vizoal.vo.CommentListResponseVO;
import com.vizoal.vo.PlayerResponseVO;
import com.vizoal.vo.PlayerStatisticsVO;

public class ProfileActivity extends BaseActivity implements OnScrollListener{

	private String TAG = "ProfileActivity";
	private ViewPager mPager; 
	private List<View> listViews;  
	private LinearLayout t1, t2, t3, t4; 
	final private Context context = this;
	private Player player = null;
	private PlayerStatistics playerStatistics;
	private float density;
	private String playerId = "";
	private int screenW = 0;
	
	private TextView label_played,label_manOftheMatch,label_rating,label_goals,label_assists,label_yellowcard,label_redcard,
					 label_goalPG,label_shotsPG,label_shortsOnTargetPG,label_passSuccessPercent,label_dribblesPG,label_tacklesPG,
					 label_interceptionPG,label_clearancesPG,label_arialWonsPG,label_foulsPG;
	
	private TextView text_played,text_manOftheMatch,text_rating,text_goals,text_assists,text_yellowcard,text_redcard,
					 text_goalPG,text_shotsPG,text_shortsOnTargetPG,text_passSuccessPercent,text_dribblesPG,text_tacklesPG,
					 text_interceptionPG,text_clearancesPG,text_arialWonsPG,text_foulsPG;
	
	private PullToRefreshListView list = null;
    final Handler mHandler = new Handler();
    
    private ArrayList<HashMap<String, Object>> listItem;
    private ArrayList<HashMap<String, Object>> moreNewListItem;
    private ArrayList<HashMap<String, Object>> moreOldListItem;
    private SimpleAdapter listItemAdapter;
    
    private int totalCommentCountWhenFirstLoad = 0;
    private int totalOldCommentCountDisplayed = 0;
    private boolean firstLoadCommentFlag = true;
    
    private String savedUserName = "";
    private SharedPreferences prefs;
    
    private RelativeLayout loading;
    
   	private boolean refreshable;
   	
   	private int firstPlayerCommentId;
   	private int lastPlayerCommentId;
   	
   	private LinearLayout mainProfileLayout;
   	
   	private ProgressBar loadingProgressBar,statistics_loadingProgressBar;
   	
   	private Typeface typeface_regular,typeface_light;
   	
   	private TextView label_full_name,label_birth_place,label_age,label_birth_date,label_nation,
   					 label_height,label_weight,label_strong_foot,label_club_number;
   	
   	private DisplayImageOptions playerOptions = DisplayImageOptionUtil.createPlayerImageOption();
	private DisplayImageOptions emptyOptions = DisplayImageOptionUtil.createEmptyImageOption();
   	
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.player_profile);
		Intent intent=this.getIntent();
		
        playerId=intent.getStringExtra("playerId");
        
        DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screenW = dm.widthPixels;
		density = dm.density;
		density = Util.normalizeDensity(density);
		
		InitTextView();
		InitViewPager();
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		typeface_regular = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeue.ttf");
		typeface_light = Typeface.createFromAsset(context.getAssets(), "fonts/HelveticaNeueLight.ttf");
		
		ActionBar ab = getSupportActionBar();
		ab.setDisplayShowTitleEnabled(false);
        ab.setDisplayShowTitleEnabled(true);
        
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
 
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	}
    
    private void firstLoadPlayerComment(){
    	String url = ServiceEndpointConstant.PLAYER_COMMENT_ENDPOINT+playerId;
		AsyncHttpClient client = new AsyncHttpClient();
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Gson gson = new Gson();
				CommentListAllResponseVO commentRes = gson.fromJson(response,
						CommentListAllResponseVO.class);
				if(commentRes!=null){
					PlayerCommentListAll playerCommentListAll = commentRes.getPlayerCommentListAll();
					if(playerCommentListAll!=null && playerCommentListAll.getPlayerCommentList()!=null){
						if(playerCommentListAll.getPlayerCommentList().size()>0){
							List<PlayerComment> initalCommentList = playerCommentListAll.getPlayerCommentList();
							//Get the first playerCommentid and last playerCommentId
							firstPlayerCommentId = initalCommentList.get(0).getPlayerCommentId();
							lastPlayerCommentId = initalCommentList.get(initalCommentList.size()-1).getPlayerCommentId();
							//Total comments count, it might change after pull to refresh
							totalCommentCountWhenFirstLoad = playerCommentListAll.getTotalCount();
							listItem = Util.constructCommentList(playerCommentListAll.getPlayerCommentList());
							
							if(listItem!=null && listItem.size()>0){
								filloutPlayerComment();
							}
						}
					}
				}
			}

			@Override
			public void onFailure(java.lang.Throwable t) {
				String errorMSG = "Failed to get club list from server.";
				Log.e(TAG, errorMSG + " Error: " + t.getMessage(), t);
				Toast.makeText(getBaseContext(), errorMSG, Toast.LENGTH_SHORT)
						.show();
			}
		});
    }
    
    private void loadOldPlayerComment(){
    	
    	String url = ServiceEndpointConstant.PLAYER_OLD_COMMENT_ENDPOINT+playerId+"/"+lastPlayerCommentId;
		AsyncHttpClient client = new AsyncHttpClient();
		moreOldListItem = null;
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Gson gson = new Gson();
				CommentListResponseVO commentRes = gson.fromJson(response,
						CommentListResponseVO.class);
				if(commentRes!=null && commentRes.getPlayerList().size()>0){
					int size = commentRes.getPlayerList().size();
					List<PlayerComment> playerCommentList = commentRes.getPlayerList();
					lastPlayerCommentId = playerCommentList.get(size-1).getPlayerCommentId();
					moreOldListItem = Util.constructCommentList(commentRes.getPlayerList());
					totalOldCommentCountDisplayed = totalOldCommentCountDisplayed + size;
					
					if(listItem==null){
						listItem = new ArrayList<HashMap<String, Object>>();
					}
					//Add the new data to existing list
					listItem.addAll(moreOldListItem);
					listItemAdapter.notifyDataSetChanged();
					loading.setVisibility(View.GONE);
					if(totalOldCommentCountDisplayed>=totalCommentCountWhenFirstLoad){
						refreshable = false;
					}else{
						refreshable = true;
					}
				}else{
					loading.setVisibility(View.GONE);
				}
			}

			@Override
			public void onFailure(java.lang.Throwable t) {
				String errorMSG = "Failed to get club list from server.";
				Log.e(TAG, errorMSG + " Error: " + t.getMessage(), t);
				Toast.makeText(getBaseContext(), errorMSG, Toast.LENGTH_SHORT)
						.show();
			}
		});
    }
    
private void loadNewPlayerComment(){
    	
    	String url = ServiceEndpointConstant.PLAYER_NEW_COMMENT_ENDPOINT+playerId+"/"+firstPlayerCommentId;
		AsyncHttpClient client = new AsyncHttpClient();
		moreNewListItem = null;
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String response) {
				Gson gson = new Gson();
				CommentListResponseVO commentRes = gson.fromJson(response,
						CommentListResponseVO.class);
				if(commentRes!=null && commentRes.getPlayerList()!=null && commentRes.getPlayerList().size()>0){
					List<PlayerComment> playerCommentList = commentRes.getPlayerList();
					firstPlayerCommentId = playerCommentList.get(0).getPlayerCommentId();
					moreNewListItem = Util.constructCommentList(commentRes.getPlayerList());
					
					if(listItem==null){
						listItem = new ArrayList<HashMap<String, Object>>();
					}
					//Add the new data to existing list
			    	listItem.addAll(0, moreNewListItem);
			    	list.onRefreshComplete();
			    	listItemAdapter.notifyDataSetChanged();
				}else{
					list.onRefreshComplete();
				}
				
			}

			@Override
			public void onFailure(java.lang.Throwable t) {
				String errorMSG = "Failed to get club list from server.";
				Log.e(TAG, errorMSG + " Error: " + t.getMessage(), t);
				Toast.makeText(getBaseContext(), errorMSG, Toast.LENGTH_SHORT)
						.show();
			}
		});
    }
    
    private void filloutPlayerComment(){
    		Object listObj = findViewById(R.id.player_comment_list);
    		
    		if(listObj!=null){
    			list = (PullToRefreshListView)listObj ;
    			loading = (RelativeLayout) findViewById(R.id.loading);
    			totalOldCommentCountDisplayed = listItem.size();
    			if(totalOldCommentCountDisplayed<totalCommentCountWhenFirstLoad){
    				refreshable = true;
    			}
    			
			    listItemAdapter = new SimpleAdapter(context,listItem, 
		            R.layout.player_comment_items,
		            new String[] { "comment_poster_name","comment_content","comment_post_time"}, 
		            new int[] {R.id.comment_poster_name,R.id.comment_content,R.id.comment_post_time}
		        );
		        list.setAdapter(listItemAdapter);
    		        
		        list.setShowLastUpdatedText(true);
		        list.setLastUpdatedDateFormat(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
    		        
		        list.onRefreshComplete();

		        list.setOnRefreshListener(new com.vizoal.view.PullToRefreshListView.OnRefreshListener() {
					@Override
					public void onRefresh() {
						loadNewPlayerComment();
					}
				});
		        
		        list.setOnScrollListener(this);
    		}
    }
    
    @Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if ((firstVisibleItem + visibleItemCount == totalItemCount)
				&& (totalItemCount != 0)) {
			if (refreshable) {
				refreshable = false;
				loading.setVisibility(View.VISIBLE);
				loadOldPlayerComment();
			}
		}
	}
    
    @Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub

	}
    
    private void filloutPlayerData(){
    	if(player!=null){
    		
    		label_full_name = (TextView)findViewById(R.id.label_full_name);
    		label_birth_place = (TextView)findViewById(R.id.label_birth_place);
    		label_age = (TextView)findViewById(R.id.label_age);
    		label_birth_date = (TextView)findViewById(R.id.label_birth_date);
    		label_nation = (TextView)findViewById(R.id.label_nation);
    		label_height = (TextView)findViewById(R.id.label_height);
    		label_weight = (TextView)findViewById(R.id.label_weight);
    		label_strong_foot = (TextView)findViewById(R.id.label_strong_foot);
    		label_club_number = (TextView)findViewById(R.id.label_club_number);
    		
    		label_full_name.setTypeface(typeface_regular);
    		label_birth_place.setTypeface(typeface_regular);
    		label_age.setTypeface(typeface_regular);
    		label_birth_date.setTypeface(typeface_regular);
    		label_nation.setTypeface(typeface_regular);
    		label_height.setTypeface(typeface_regular);
    		label_weight.setTypeface(typeface_regular);
    		label_strong_foot.setTypeface(typeface_regular);
    		label_club_number.setTypeface(typeface_regular);
    		
    		//Fill out the player picture
        	ImageView playerPic = (ImageView) findViewById(R.id.player_photo);
    		String playerPic_URL = ServiceEndpointConstant.PLAYER_IMAGE_PRE+density+"/"+player.getFmId()+".png";
    		imageLoader.displayImage(playerPic_URL, playerPic, playerOptions);
    		
    		ImageView countryPic = (ImageView) findViewById(R.id.country_flag);
    		String countryPic_URL = ServiceEndpointConstant.COUNTRY_FLAG_IMAGE_PRE+density+"/"+player.getNationDisplay_fmid()+".png";
    		imageLoader.displayImage(countryPic_URL, countryPic, emptyOptions);
    		
    		
    		ImageView clubPic = (ImageView) findViewById(R.id.club_logo);
    		String clubPic_URL = ServiceEndpointConstant.CLUB_FLAG_IMAGE_PRE+density+"/"+player.getClub_fm_id()+".png";
    		imageLoader.displayImage(clubPic_URL, clubPic, emptyOptions);
    		
    		clubPic.setOnClickListener(new View.OnClickListener() {
    		    public void onClick(View v) {
    		    	Intent intent = new Intent(context, ClubProfileActivity.class);
                    Integer clubId = player.getCurrentClub();
                    intent.putExtra("clubId", ""+clubId);
                    intent.putExtra("clubName", player.getCurrentClubName());
                    startActivity(intent);
                }
            });
    		
    		//Get the player name
    		TextView playerName = (TextView)findViewById(R.id.player_name);
    		playerName.setText(player.getFirstName()+" "+player.getLastName());
    		playerName.setTypeface(typeface_light);
    		
    		//Set the club name
    		TextView playerClub = (TextView)findViewById(R.id.club_name);
    		playerClub.setText(player.getCurrentClubName());
    		playerClub.setTypeface(typeface_light);
    		
    		playerClub.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(context, ClubProfileActivity.class);
                    Integer clubId = player.getCurrentClub();
                    intent.putExtra("clubId", ""+clubId);
                    intent.putExtra("clubName", player.getCurrentClubName());
                    startActivity(intent);
                }
            });
    		
    		//Set full name
    		TextView player_fullname_textView = (TextView)findViewById(R.id.text_full_name);
    		player_fullname_textView.setText(player.getFullName());
    		player_fullname_textView.setTypeface(typeface_light);
    		
    		//Set display nationality
    		TextView country_name = (TextView)findViewById(R.id.country_name_new);
    		if(country_name!=null) {
			    country_name.setText(player.getNationOfDisplayName());
			    country_name.setTypeface(typeface_light);
		    }
    		
    		//Set nationalities
    		TextView player_nation_textView = (TextView)findViewById(R.id.text_nationality);
    		
    		if(player.getPlayerPlayerNationalityList()!=null){
                String nationalityStr = "";
                List<PlayerNationality> nationalityList = player.getPlayerPlayerNationalityList();
//              for(int j=0;j<nationalityList.size();j++){
//                  if(j!=0){
//                      nationalityStr = nationalityStr+", ";
//                  }
//                  nationalityStr = nationalityStr+nationalityList.get(j).getName();
//              }
//              if(player_nation_textView!=null){
//                  player_nation_textView.setText(nationalityStr);
//              }
                if (nationalityList.size()>1)
                    player_nation_textView.setSingleLine(false);
                for(int j=0;j<nationalityList.size();j++){
                    if(j!=0){
                        nationalityStr = nationalityStr+"\n";
                    }
                    PlayerNationality playerNationality = nationalityList.get(j);
                    nationalityStr = nationalityStr + playerNationality.getName();
                    // Create the nation flag ImageView
                    ImageView image = new ImageView(this);

                    // Convert dp to px for LayoutParams
                    int pixels = (int) (18 * density + 0.5f);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(pixels, pixels);
                    pixels = (int) (6 * density + 0.5f);
                    int px_top = (int) (1 * density + 0.5f);
                    if (j==0)
                        params.setMargins(0, px_top, pixels, 0);
                    else
                        params.setMargins(0, pixels, pixels, 0);
                    image.setLayoutParams(params);
                    
                    String URL = ServiceEndpointConstant.COUNTRY_FLAG_IMAGE_PRE+density+"/"+playerNationality.getFmId()+".png";
                    // Get the existing layout and add the nation flag ImageView
                    LinearLayout layout = (LinearLayout) findViewById(R.id.nationality_flag_layout);
                    layout.addView(image);
                    imageLoader.displayImage(URL, image, emptyOptions);
                }
                if(player_nation_textView!=null){
                    player_nation_textView.setText(nationalityStr);
                    player_nation_textView.setTypeface(typeface_light);
                }
    		}
    		//Set birthplace
    		TextView player_birthplace_textView = (TextView)findViewById(R.id.text_birth_place);
    		if(player_birthplace_textView!=null){
    			player_birthplace_textView.setText(player.getCityOfBirth()+", "+player.getNationOfBirthName());
    			player_birthplace_textView.setTypeface(typeface_light);
    		}
    		
    		//Set age
    		TextView player_age_textView = (TextView)findViewById(R.id.text_age);
    		if(player_age_textView!=null){
    			player_age_textView.setText(""+player.getAge());
    			player_age_textView.setTypeface(typeface_light);
    		}
    		
    		//Set birthday
    		TextView player_birthdate_textView = (TextView)findViewById(R.id.text_birth_date);
    		if(player_birthdate_textView!=null){
    			player_birthdate_textView.setText(player.getDateOfBirth_andriod());
    			player_birthdate_textView.setTypeface(typeface_light);
    		}
    		
    		//Set height
    		TextView player_height_textView = (TextView)findViewById(R.id.text_height);
    		if(player_height_textView!=null){
    			player_height_textView.setText(player.getHeight() + " cm");
    			player_height_textView.setTypeface(typeface_light);
    		}
    		
    		//Set weight
    		TextView player_weight_textView = (TextView)findViewById(R.id.text_weight);
    		if(player_weight_textView!=null){
    			player_weight_textView.setText(player.getWeight() + " kg");
    			player_weight_textView.setTypeface(typeface_light);
    		}
    		
    		//Set strong_foot
    		TextView player_stringfoot_textView = (TextView)findViewById(R.id.text_strong_foot);
    		if(player_stringfoot_textView!=null){
    			player_stringfoot_textView.setText(player.getPreferFoot()!=null?player.getPreferFoot().toUpperCase():"");
    			player_stringfoot_textView.setTypeface(typeface_light);
    		}

    		//Set club number
    		TextView player_clubnumber_textView = (TextView)findViewById(R.id.text_club_number);
    		if(player_clubnumber_textView!=null){
    			player_clubnumber_textView.setText(player.getClubNumber());
    			player_clubnumber_textView.setTypeface(typeface_light);
    		}

    		//Set up player position
			RelativeLayout football_field_layout = (RelativeLayout)findViewById(R.id.football_field_layout);
			LayoutParams football_field_layout_params = football_field_layout.getLayoutParams();

			int fieldWidth = (screenW-2*DensityUtils.getPixByDP(10,density));
			int fieldHeight = fieldWidth*348/500;
			football_field_layout_params.height = fieldHeight;
			football_field_layout.setLayoutParams(football_field_layout_params);
			
			if(player.getPlayerPositionList()!=null){
    			for(int i=0;i<player.getPlayerPositionList().size() && i<8 ;i++){
    				ImageView imageview =null;
    				TextView textview = null;
    				RelativeLayout position_layout = null;
    				if(i==0){
    					imageview = (ImageView)findViewById(R.id.position_image1);
    					textview = (TextView)findViewById(R.id.position_lable1);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout1);
    				}else if(i==1){
    					imageview = (ImageView)findViewById(R.id.position_image2);
    					textview = (TextView)findViewById(R.id.position_lable2);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout2);
    				}else if(i==2){
    					imageview = (ImageView)findViewById(R.id.position_image3);
    					textview = (TextView)findViewById(R.id.position_lable3);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout3);
    				}else if(i==3){
    					imageview = (ImageView)findViewById(R.id.position_image4);
    					textview = (TextView)findViewById(R.id.position_lable4);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout4);
    				}else if(i==4){
    					imageview = (ImageView)findViewById(R.id.position_image5);
    					textview = (TextView)findViewById(R.id.position_lable5);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout5);
    				}else if(i==5){
    					imageview = (ImageView)findViewById(R.id.position_image6);
    					textview = (TextView)findViewById(R.id.position_lable6);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout6);
    				}else if(i==6){
    					imageview = (ImageView)findViewById(R.id.position_image7);
    					textview = (TextView)findViewById(R.id.position_lable7);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout7);
    				}else if(i==7){
    					imageview = (ImageView)findViewById(R.id.position_image8);
    					textview = (TextView)findViewById(R.id.position_lable8);
    					position_layout = (RelativeLayout)findViewById(R.id.position_layout8);
    				}
    				PlayerPosition playerPosition = player.getPlayerPositionList().get(i);
    				imageview.setVisibility(View.VISIBLE);
    				textview.setText(playerPosition.getName());
    				
					int id = getResources().getIdentifier("position_red", "drawable", getPackageName());
    				imageview.setImageResource(id);    				
    				if("100%".equalsIgnoreCase(playerPosition.getEfficiency())){
    					imageview.setImageDrawable(getResources().getDrawable(R.drawable.player_position_icon_100));
    				}else if("90%".equalsIgnoreCase(playerPosition.getEfficiency())){
    					imageview.setImageDrawable(getResources().getDrawable(R.drawable.player_position_icon_90));
    				}else if("80%".equalsIgnoreCase(playerPosition.getEfficiency())){
    					imageview.setImageDrawable(getResources().getDrawable(R.drawable.player_position_icon_80));
    				}else if("70%".equalsIgnoreCase(playerPosition.getEfficiency())){
    					imageview.setImageDrawable(getResources().getDrawable(R.drawable.player_position_icon_70));
    				}else if("60%".equalsIgnoreCase(playerPosition.getEfficiency())){
    					imageview.setImageDrawable(getResources().getDrawable(R.drawable.player_position_icon_60));
    				}
    				LayoutParams position_layout_params = position_layout.getLayoutParams();
    				position_layout_params.height = PlayerPositonCalculator.calculateHeight(fieldHeight, playerPosition.getName(), density);
    				position_layout_params.width = PlayerPositonCalculator.calculateWidth(fieldWidth, playerPosition.getName(), density);
    				position_layout.setLayoutParams(position_layout_params);
    			}
    		}
    	}
    }
    
    private void filloutPlayerParticipationData(){
    	
    	if(playerStatistics!=null){
    		ScrollView sview = (ScrollView)findViewById(R.id.scroll_view);
    		sview.setVisibility(View.VISIBLE);
    		
        	label_played=(TextView)findViewById(R.id.label_played);
        	label_manOftheMatch=(TextView)findViewById(R.id.label_manOftheMatch);
        	label_rating=(TextView)findViewById(R.id.label_rating);
        	label_goals=(TextView)findViewById(R.id.label_goals);
        	label_assists=(TextView)findViewById(R.id.label_assists);
        	label_yellowcard=(TextView)findViewById(R.id.label_yellowcard);
        	label_redcard=(TextView)findViewById(R.id.label_redcard);
    		label_goalPG=(TextView)findViewById(R.id.label_goalPG);
    		label_shotsPG=(TextView)findViewById(R.id.label_shotsPG);
    		label_shortsOnTargetPG=(TextView)findViewById(R.id.label_shortsOnTargetPG);
    		label_passSuccessPercent=(TextView)findViewById(R.id.label_passSuccessPercent);
    		label_dribblesPG=(TextView)findViewById(R.id.label_dribblesPG);
    		label_tacklesPG=(TextView)findViewById(R.id.label_tacklesPG);
    		label_interceptionPG=(TextView)findViewById(R.id.label_interceptionPG);
    		label_clearancesPG=(TextView)findViewById(R.id.label_clearancesPG);
    		label_arialWonsPG=(TextView)findViewById(R.id.label_arialWonsPG);
    		label_foulsPG=(TextView)findViewById(R.id.label_foulsPG);
    		
    		text_played=(TextView)findViewById(R.id.text_played);
        	text_manOftheMatch=(TextView)findViewById(R.id.text_manOftheMatch);
        	text_rating=(TextView)findViewById(R.id.text_rating);
        	text_goals=(TextView)findViewById(R.id.text_goals);
        	text_assists=(TextView)findViewById(R.id.text_assists);
        	text_yellowcard=(TextView)findViewById(R.id.text_yellowcard);
        	text_redcard=(TextView)findViewById(R.id.text_redcard);
    		text_goalPG=(TextView)findViewById(R.id.text_goalPG);
    		text_shotsPG=(TextView)findViewById(R.id.text_shotsPG);
    		text_shortsOnTargetPG=(TextView)findViewById(R.id.text_shortsOnTargetPG);
    		text_passSuccessPercent=(TextView)findViewById(R.id.text_passSuccessPercent);
    		text_dribblesPG=(TextView)findViewById(R.id.text_dribblesPG);
    		text_tacklesPG=(TextView)findViewById(R.id.text_tacklesPG);
    		text_interceptionPG=(TextView)findViewById(R.id.text_interceptionPG);
    		text_clearancesPG=(TextView)findViewById(R.id.text_clearancesPG);
    		text_arialWonsPG=(TextView)findViewById(R.id.text_arialWonsPG);
    		text_foulsPG=(TextView)findViewById(R.id.text_foulsPG);
    		
    		label_played.setTypeface(typeface_regular);
        	label_manOftheMatch.setTypeface(typeface_regular);
        	label_rating.setTypeface(typeface_regular);
        	label_goals.setTypeface(typeface_regular);
        	label_assists.setTypeface(typeface_regular);
        	label_yellowcard.setTypeface(typeface_regular);
        	label_redcard.setTypeface(typeface_regular);
    		label_goalPG.setTypeface(typeface_regular);
    		label_shotsPG.setTypeface(typeface_regular);
    		label_shortsOnTargetPG.setTypeface(typeface_regular);
    		label_passSuccessPercent.setTypeface(typeface_regular);
    		label_dribblesPG.setTypeface(typeface_regular);
    		label_tacklesPG.setTypeface(typeface_regular);
    		label_interceptionPG.setTypeface(typeface_regular);
    		label_clearancesPG.setTypeface(typeface_regular);
    		label_arialWonsPG.setTypeface(typeface_regular);
    		label_foulsPG.setTypeface(typeface_regular);
    		
    		text_played.setTypeface(typeface_light);
        	text_manOftheMatch.setTypeface(typeface_light);
        	text_rating.setTypeface(typeface_light);
        	text_goals.setTypeface(typeface_light);
        	text_assists.setTypeface(typeface_light);
        	text_yellowcard.setTypeface(typeface_light);
        	text_redcard.setTypeface(typeface_light);
    		text_goalPG.setTypeface(typeface_light);
    		text_shotsPG.setTypeface(typeface_light);
    		text_shortsOnTargetPG.setTypeface(typeface_light);
    		text_passSuccessPercent.setTypeface(typeface_light);
    		text_dribblesPG.setTypeface(typeface_light);
    		text_tacklesPG.setTypeface(typeface_light);
    		text_interceptionPG.setTypeface(typeface_light);
    		text_clearancesPG.setTypeface(typeface_light);
    		text_arialWonsPG.setTypeface(typeface_light);
    		text_foulsPG.setTypeface(typeface_light);
    		
    		text_played.setText(playerStatistics.getAppearance());
        	text_manOftheMatch.setText(playerStatistics.getManOfTheMatch());
        	text_rating.setText(playerStatistics.getRating());
        	text_goals.setText(playerStatistics.getGoals());
        	text_assists.setText(playerStatistics.getAssists());
        	text_yellowcard.setText(playerStatistics.getYellowCards());
        	text_redcard.setText(playerStatistics.getRedCards());
    		text_goalPG.setText(playerStatistics.getGoalsPerGame());
    		text_shotsPG.setText(playerStatistics.getShotsPerGame());
    		text_shortsOnTargetPG.setText(playerStatistics.getShotsOnTargetPerGame());
    		text_passSuccessPercent.setText(playerStatistics.getPassSuccessPercentage());
    		text_dribblesPG.setText(playerStatistics.getDribblePerGame());
    		text_tacklesPG.setText(playerStatistics.getTacklesPerGame());
    		text_interceptionPG.setText(playerStatistics.getInterceptionPerGame());
    		text_clearancesPG.setText(playerStatistics.getClearancePerGame());
    		text_arialWonsPG.setText(playerStatistics.getArialWonPerGame());
    		text_foulsPG.setText(playerStatistics.getFoulPerGame());
    	}else{
    		TextView text_nodatafound = (TextView)findViewById(R.id.text_nodatafound);
    		text_nodatafound.setVisibility(View.VISIBLE);
    	}
    }

    private void InitTextView() {
		t1 = (LinearLayout) findViewById(R.id.tab1);
		t2 = (LinearLayout) findViewById(R.id.tab2);
		t3 = (LinearLayout) findViewById(R.id.tab3);
		t4 = (LinearLayout) findViewById(R.id.tab4);

		t1.setOnClickListener(new MyOnClickListener(0));
		t2.setOnClickListener(new MyOnClickListener(1));
		t3.setOnClickListener(new MyOnClickListener(2));
		t4.setOnClickListener(new MyOnClickListener(3));
		
		loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
		mainProfileLayout = (LinearLayout) findViewById(R.id.mainProfileLayout);
	}

	private void InitViewPager() {
		mPager = (ViewPager) findViewById(R.id.vPager2);
		listViews = new ArrayList<View>();
		LayoutInflater mInflater = getLayoutInflater();
		listViews.add(mInflater.inflate(R.layout.profile_tab1, null));
		listViews.add(mInflater.inflate(R.layout.profile_tab2, null));
		listViews.add(mInflater.inflate(R.layout.profile_tab3, null));
		listViews.add(mInflater.inflate(R.layout.profile_tab4, null));
		mPager.setAdapter(new MyPagerAdapter(listViews));
		mPager.setCurrentItem(0);
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
				if(player==null){
					loadingProgressBar.setVisibility(View.VISIBLE);
					mainProfileLayout.setVisibility(View.INVISIBLE);
					String url = ServiceEndpointConstant.PLAYER_PROFILE_ENDPOINT+playerId;
					AsyncHttpClient client = new AsyncHttpClient();
					client.get(url, new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(String response) {
							Gson gson = new Gson();
							PlayerResponseVO playerRes = gson.fromJson(response,
									PlayerResponseVO.class);
							if(playerRes!=null){
								player= playerRes.getPlayer();
							}
							loadingProgressBar.setVisibility(View.GONE);
							mainProfileLayout.setVisibility(View.VISIBLE);
							filloutPlayerData();
						}

						@Override
						public void onFailure(java.lang.Throwable t) {
							loadingProgressBar.setVisibility(View.GONE);
							String errorMSG = "Failed to get club list from server.";
							Log.e(TAG, errorMSG + " Error: " + t.getMessage(), t);
							Toast.makeText(getBaseContext(), errorMSG, Toast.LENGTH_SHORT)
									.show();
						}
					});
				}
			}
			if (pageIndex == 1) {
//				
			}
			if (pageIndex == 2) {
				statistics_loadingProgressBar = (ProgressBar) findViewById(R.id.statistics_loadingProgressBar);
				
				String url = ServiceEndpointConstant.PLAYER_STATISTICS_ENDPOINT+playerId;
				AsyncHttpClient client = new AsyncHttpClient();
				client.get(url, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(String response) {
						statistics_loadingProgressBar.setVisibility(View.GONE);
						Gson gson = new Gson();
						PlayerStatisticsVO playerStatisticsVO = gson.fromJson(response,
								PlayerStatisticsVO.class);
						if(playerStatisticsVO!=null){
							playerStatistics= playerStatisticsVO.getPlayerStatistics();
						}
						filloutPlayerParticipationData();
					}

					@Override
					public void onFailure(java.lang.Throwable t) {
						statistics_loadingProgressBar.setVisibility(View.GONE);
						String errorMSG = "Failed to get club list from server.";
						Log.e(TAG, errorMSG + " Error: " + t.getMessage(), t);
						Toast.makeText(getBaseContext(), errorMSG, Toast.LENGTH_SHORT)
								.show();
					}
				});
			}
			if(pageIndex == 3){
				if(firstLoadCommentFlag){
					firstLoadCommentFlag = false;
					firstLoadPlayerComment();
				}
				
				
				prefs = context.getSharedPreferences("com.vizoal", Context.MODE_PRIVATE);
				savedUserName = prefs.getString(CommonConstant.USER_NAME,"");

				final EditText edittext = (EditText) findViewById(R.id.comment_edit_box);
				edittext.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
						    edittext.setEnabled(false);
							AlertDialog.Builder alert = new AlertDialog.Builder(context);
							alert.setCancelable(false);
	
							LayoutInflater factory = LayoutInflater.from(context);

							//text_entry is an Layout XML file containing two text field to display in alert dialog
							final View comment_dialog = factory.inflate(R.layout.comment_dialog, null);

							final EditText usernameInput = (EditText) comment_dialog.findViewById(R.id.username_box);
    						final EditText commentInput = (EditText) comment_dialog.findViewById(R.id.comment_box);

    						if(savedUserName!=null && !"".equals(savedUserName.trim())){
    							usernameInput.setText(savedUserName);
    							commentInput.requestFocus();
    						}
							alert.setIcon(R.drawable.profile_tab_4).setTitle("Post Comment").setView(comment_dialog).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
							        String username = usernameInput.getText().toString();
							        String comment = commentInput.getText().toString();
							        new PostCommentTask().execute(username,comment,ServiceEndpointConstant.PLAYER_POST_COMMENT_ENDPOINT);
							        findViewById(R.id.mainProfileLayout).requestFocus();
							        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							        imm.hideSoftInputFromWindow(usernameInput.getWindowToken(), 0);
							        edittext.setEnabled(true);
							    }
							});
	
							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							    public void onClick(DialogInterface dialog, int whichButton) {
							        // Canceled.
							    	findViewById(R.id.mainProfileLayout).requestFocus();
							    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
							        imm.hideSoftInputFromWindow(usernameInput.getWindowToken(), 0);
							        edittext.setEnabled(true);
							    }
							});
							
							// Request soft keyboard to show up in the alert dialog
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
	
							alert.show();
						}
					}
			    });
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

	public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}
		public void onClick(View v) {
			mPager.setCurrentItem(index);
		}
	};
	
	private class PostCommentTask extends AsyncTask<String, String, PlayerComment[]> {
        @Override
        protected PlayerComment[] doInBackground(String... params) {
        	sendCommentJson(params[0],params[1],params[2]);
        	return null;
        }
		@Override
        protected void onPostExecute(PlayerComment[] result) {
//        	mPager.getAdapter().notifyDataSetChanged();
//            mPager.setCurrentItem(3);
			if(list==null){
				firstLoadPlayerComment();
			}else{
				loadNewPlayerComment();
			}
        }
    }
	
	protected void sendCommentJson(final String username, final String comment, final String url) {
                HttpClient client = new DefaultHttpClient();
                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
//                HttpResponse response;
                String postUserName = CommonConstant.USER_NAME_ANOYMOUS;
                if(username!=null && !"".equals(username.trim())){
                	postUserName = username;
                	SharedPreferences.Editor editor = prefs.edit();
                	editor.putString(CommonConstant.USER_NAME, username);
    				editor.commit();
    				savedUserName = username;
                }
                
                JSONObject json = new JSONObject();
                try {
                    HttpPost post = new HttpPost(url);
                    json.put("playerId", ""+player.getPlayerId());
                    json.put("userName", postUserName);
                    json.put("comment", comment);
                    StringEntity se = new StringEntity( json.toString(),HTTP.UTF_8);  
                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                    post.setEntity(se);
                    client.execute(post);

                    /*Checking response */
                    //Pending...
//                    if(response!=null){
//                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
//                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
    }
	
	/**
	 * Page change listener
	 *
	 */
	public class TabPageChangeListener implements OnPageChangeListener {
		LinearLayout tab1 = (LinearLayout) findViewById(R.id.tab1);
		LinearLayout tab2 = (LinearLayout) findViewById(R.id.tab2);
		LinearLayout tab3 = (LinearLayout) findViewById(R.id.tab3);
		LinearLayout tab4 = (LinearLayout) findViewById(R.id.tab4);
		
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				tab1.setBackgroundColor(Color.parseColor("#577b9f"));
				tab2.setBackgroundColor(Color.parseColor("#3b536b"));
				tab3.setBackgroundColor(Color.parseColor("#3b536b"));
				tab4.setBackgroundColor(Color.parseColor("#3b536b"));
				break;
			case 1:
				tab1.setBackgroundColor(Color.parseColor("#3b536b"));
				tab2.setBackgroundColor(Color.parseColor("#577b9f"));
				tab3.setBackgroundColor(Color.parseColor("#3b536b"));
				tab4.setBackgroundColor(Color.parseColor("#3b536b"));
				break;
			case 2:
				tab1.setBackgroundColor(Color.parseColor("#3b536b"));
				tab2.setBackgroundColor(Color.parseColor("#3b536b"));
				tab3.setBackgroundColor(Color.parseColor("#577b9f"));
				tab4.setBackgroundColor(Color.parseColor("#3b536b"));
				break;
			case 3:
				tab1.setBackgroundColor(Color.parseColor("#3b536b"));
				tab2.setBackgroundColor(Color.parseColor("#3b536b"));
				tab3.setBackgroundColor(Color.parseColor("#3b536b"));
				tab4.setBackgroundColor(Color.parseColor("#577b9f"));
				break;
			}
		}
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}
		public void onPageScrollStateChanged(int arg0) {
		}
	}

	/** Defines a default (dummy) share intent to initialize the action provider.
	  * However, as soon as the actual content to be used in the intent
	  * is known or changes, you must update the share intent by again calling
	  * mShareActionProvider.setShareIntent()
	  */
//	private Intent getDefaultIntent() {
//	    Intent intent = new Intent(Intent.ACTION_SEND);
//	    intent.setType("image/*");
//	    return intent;
//	}
//
//	@Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_add_to_favorites:
//            	if(!signed_in){
//            		startActivityForResult(new Intent(this, LoginActivity.class), 0);
//		            signed_in = true;
//            	}else{
//            		final ClientUser inneruser = dbHander.findUser();
//            		if(inneruser!=null){
//            			signed_in = true;
//            			String url = ServiceEndpointConstant.VIZOAL_BASE_URL +"/vizoal/services/fav_player/add/"+inneruser.getVizoal_user_id()+"/"+playerId;
//                		AsyncHttpClient client = new AsyncHttpClient();
//                		client.post(url, new AsyncHttpResponseHandler() {
//                			@Override
//                			public void onSuccess(String response) {
//                				if(response!=null && Integer.valueOf(response)>0){
//                					Toast.makeText(getBaseContext(), "Add to Favorites successfully!", Toast.LENGTH_SHORT).show();
//                					redirectToFavPage(inneruser.getVizoal_user_id());
//                				}else{
//                					Toast.makeText(getBaseContext(), "This player have been added to your favorite before!", Toast.LENGTH_SHORT).show();
//                				}
//                			}
//                		});
//            		}else{
//            			startActivityForResult(new Intent(this, LoginActivity.class), 0);
//            		}
//            	}
//        	    return true;
//            case R.id.action_share_to_facebook:
//            	Toast.makeText(getBaseContext(), "Share to Facebook pressed!", Toast.LENGTH_SHORT).show();
//            	return true;
//            case R.id.action_share_to_twitter:
//            	Toast.makeText(getBaseContext(), "Share to Twitter pressed!", Toast.LENGTH_SHORT).show();
//            	return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//	}
//	
//	
//	private void redirectToFavPage(String userId){
//		Intent intent = new Intent(this, FavPlayerListActivity.class);
//		intent.putExtra("userId",userId);  
//		startActivityForResult(intent, 0);
//	}
}

