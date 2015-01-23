//package com.xxx.activity;
//
//import java.io.InputStream;
//import java.util.Date;
//
//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicHeader;
//import org.apache.http.params.HttpConnectionParams;
//import org.apache.http.protocol.HTTP;
//import org.json.JSONObject;
//
//import twitter4j.Twitter;
//import twitter4j.TwitterException;
//import twitter4j.TwitterFactory;
//import twitter4j.User;
//import twitter4j.auth.AccessToken;
//import twitter4j.auth.RequestToken;
//import twitter4j.conf.Configuration;
//import twitter4j.conf.ConfigurationBuilder;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.SharedPreferences.Editor;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.support.v7.app.ActionBarActivity;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.facebook.Request;
//import com.facebook.Response;
//import com.facebook.Session;
//import com.facebook.SessionState;
//import com.facebook.model.GraphLocation;
//import com.facebook.model.GraphUser;
//import com.google.gson.Gson;
//import com.loopj.android.http.AsyncHttpClient;
//import com.loopj.android.http.AsyncHttpResponseHandler;
//import com.xxx.common.constant.CommonConstant;
//import com.xxx.common.constant.ServiceEndpointConstant;
//import com.xxx.common.constant.TwitterConst;
//import com.xxx.domain.ClientUser;
//import com.xxx.domain.Users;
//import com.xxx.service.FacebookUserDBHander;
//import com.xxx.service.TwitterUserDBHander;
//import com.xxx.service.UserDBHander;
//import com.xxx.util.DateUtil;
//import com.xxx.util.EmailValidator;
//import com.xxx.util.Hash;
//import com.xxx.util.IOUtil;
//import com.xxx.vo.UserResponseVO;
//
//public class LoginActivity extends ActionBarActivity {
//	
//	private String TAG = "LoginActivity";
//	
//	public static String currentUser = "";
//	
//	UserDBHander dbHander = new UserDBHander(LoginActivity.this);
//	
//	FacebookUserDBHander facebookDBHandler = new FacebookUserDBHander(LoginActivity.this);
//	
//	TwitterUserDBHander twitterDBHandler = new TwitterUserDBHander(LoginActivity.this);
//	
//	TextView textLoginFacebook  = null;
//	
//	TextView textLoginTwitter = null;
//	
//	ProgressDialog pd = null;
//	
//	private EditText emailEditText, passwordEditText;
//	
//	private TextView errorTextView;
//	
//	private UserResponseVO res;
//	
//	private String password, email, errorMsg;
//	
//	private static Twitter twitter;
//	private static RequestToken requestToken;
//	public static SharedPreferences mSharedPreferences;
//	
//    public void postxxxLogin(){
//    	if(res!=null){
//    		if(res.getUser()!=null){
//    			Users users = res.getUser();
//        		ClientUser clientUser = new ClientUser();
//        		clientUser.setRegister_from(CommonConstant.xxx);
//        		clientUser.setxxx_user_name(users.getxxx_user_name());
//        		clientUser.setxxx_user_id(""+users.getxxx_user_id());
//        		clientUser.setLogin_time(DateUtil.parseDateToString(new Date(), DateUtil.sdf_bidStatus));
//        		dbHander.addUser(clientUser);
//        		redirectToHomePage();
//    		}else{
//    			if(res.getResponseStatus()!=null ){
//    				if( !"200".equals(res.getResponseStatus().getCode())){
//    					errorTextView.setText(res.getResponseStatus().getCode()+":"+res.getResponseStatus().getMessage());
//    				}else if(res.getUser()==null){
//    					errorTextView.setText("Wrong username or password");
//    				}
//    			} 
//    		}
//    	}else{
//    		errorTextView.setText("The response of login call is null");
//    	}
//    	dismissProgressDialog();
//    }
//			
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.login);
//        
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
//        
//        emailEditText = (EditText)findViewById(R.id.email);
//        passwordEditText = (EditText)findViewById(R.id.password);
//        errorTextView = (TextView) findViewById(R.id.error);
//        
//        if(dbHander.findUser()!=null){
//        	redirectToHomePage(); 
//        }
//        
//        //Login button event
//        loginBtnClick();
//        
//        //Facebook
//        textLoginFacebook = (TextView)findViewById(R.id.textLoginFacebook);
//        
//        textLoginFacebook.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Session.openActiveSession(LoginActivity.this, true, new Session.StatusCallback() {
//
//				      // callback when session changes state
//				      @Override
//				      public void call(Session session, SessionState state, Exception exception) {
//				    	  if (session.isOpened()) {
//				          // make request to the /me API
//				    	  showProgressDialog();
//				          Request.executeMeRequestAsync(session, new Request.GraphUserCallback() {
//
//				            // callback after Graph API response with user object
//				            @Override
//				            public void onCompleted(GraphUser user, Response response) {
//				              if (user != null) {
////				            	  btnLoginFacebook.setText("Hello " + user.getName() + "!");
//				            	  dbHander.addUser(createClientUserFromFacebook(user));
//				            	  new CreateUserTask().execute(createUserFromFacebook(user));
//				              }
//				            }
//				          });
//				        }
//				      }
//				    });
//			}
//		});
//        Button page_title_btn = (Button)findViewById(R.id.page_title_btn);
//		if(page_title_btn!=null){
//			page_title_btn.setText("Sign In");
//		}
//        
//		TextView registerTextView = (TextView) findViewById(R.id.textRegisterxxx);
//        
//        // Listening to register new account link
//		registerTextView.setOnClickListener(new View.OnClickListener() {
//			
//			public void onClick(View v) {
//				// Switching to Register screen
//				Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
//				startActivity(i);
//			}
//		});
//		
//		//Twitter
//		mSharedPreferences = getSharedPreferences(TwitterConst.PREFERENCE_NAME, MODE_PRIVATE);
//		
//		textLoginTwitter = (TextView)findViewById(R.id.textLoginTwitter);
//		textLoginTwitter.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
////				if (isConnected()) {
////					onResume2();
////				} else {
//					askOAuth();
////				}
//			}
//		});	
//		
//		final Uri uri = getIntent().getData();
//		if (uri != null && uri.toString().startsWith(TwitterConst.CALLBACK_URL)) {
//			new AsyncTask<String,String, AccessToken>(){
//				@Override
//		        protected void onPostExecute(AccessToken accessToken) {
//					Editor e = mSharedPreferences.edit();
//	                e.putString(TwitterConst.PREF_KEY_TOKEN, accessToken.getToken()); 
//	                e.putString(TwitterConst.PREF_KEY_SECRET, accessToken.getTokenSecret()); 
//	                e.commit();
//	                System.out.println("called in Uri uri = getIntent().getData()");
//	                onResume2();
//		        }
//				@Override
//				protected AccessToken doInBackground(String... params) {
//					String verifier = uri.getQueryParameter(TwitterConst.IEXTRA_OAUTH_VERIFIER);
//					AccessToken accessToken = null;
//					try {
//						accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
//					} catch (TwitterException e) {
//						Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show(); 
//						e.printStackTrace();
//					} 
//					return accessToken;
//				}
//		    }.execute("");
//        }	
//    }
//    
//    
//    private void loginBtnClick(){
//		Button loginrBtn = (Button) findViewById(R.id.btnLogin);
//		
//		loginrBtn.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				password = passwordEditText.getText().toString();
//				email = emailEditText.getText().toString();
//				
//				errorTextView.setText("");
//				if(validateForm()){
//					showProgressDialog();
//					String login_url = ServiceEndpointConstant.VIZIAL_LOGIN_ENDPOINT+email+"/"+Hash.hash(password);
//					System.out.println("login_url: " + login_url);
//					
//					AsyncHttpClient client = new AsyncHttpClient();
//					client.get(login_url, new AsyncHttpResponseHandler() {
//						@Override
//						public void onSuccess(String response) {
//							Gson gson = new Gson();
//							res = gson.fromJson(
//									response, UserResponseVO.class);
//							postxxxLogin();
//						}
//
//						@Override
//						public void onFailure(java.lang.Throwable t) {
//							String errorMSG = "Failed to get clib list from server.";
//							Log.e(TAG, errorMSG + " Error: " + t.getMessage(), t);
//							Toast.makeText(getBaseContext(), errorMSG, Toast.LENGTH_SHORT)
//									.show();
//						}
//					});
//				}else{
//					errorTextView.setText(errorMsg);
//				}
//			}
//		});	
//	}
//    
//    private boolean validateForm(){
//		boolean flag = true;
//		errorMsg = "";
//		if(email==null || "".equals(email)){
//			errorMsg = errorMsg +" Email cannot be null;";
//			flag = false;
//		}else{
//			if(!EmailValidator.validate(email)){
//				errorMsg = errorMsg +" Wrong email format;";
//				flag = false;
//			}
//		}
//		if(password == null || "".equals(password)){
//			errorMsg = errorMsg +" password cannot be null;";
//			flag = false;
//		}else{
//			if(password.length()<6){
//				errorMsg = errorMsg +" The length of password has to be more than 6;";
//				flag = false;
//			}
//		}
//		return flag;
//		
//	}
//    
//    private void askOAuth() {
//		new AsyncTask<String,String, String>(){
//			@Override
//	        protected void onPostExecute(String result) {
//				Toast.makeText(LoginActivity.this, "Please authorize this app!", Toast.LENGTH_LONG).show();
//				LoginActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
//	        }
//			@Override
//			protected String doInBackground(String... params) {
//				ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
//				configurationBuilder.setOAuthConsumerKey(TwitterConst.CONSUMER_KEY);
//				configurationBuilder.setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET);
//				Configuration configuration = configurationBuilder.build();
//				twitter = new TwitterFactory(configuration).getInstance();
//				
//				try {
//					requestToken = twitter.getOAuthRequestToken(TwitterConst.CALLBACK_URL);
//					
//				} catch (TwitterException e) {
//					e.printStackTrace();
//				}
//				return "";
//			}
//	    }.execute("");
//	}
//    
//    protected void onResume2() {
//		super.onResume();
//		if (isConnected()) {
//			new AsyncTask<String,String, User>(){
//				@Override
//		        protected void onPostExecute(User user) {
//					textLoginTwitter.setText("Hello "+(user!=null?user.getName():""));
//					showProgressDialog();
//					dbHander.addUser(createClientUserFromTwitter(user));
//		      	    new CreateUserTask().execute(createUserFromTwitter(user));
//		        }
//				@Override
//				protected User doInBackground(String... params) {
//					String oauthAccessToken = mSharedPreferences.getString(TwitterConst.PREF_KEY_TOKEN, "");
//					String oAuthAccessTokenSecret = mSharedPreferences.getString(TwitterConst.PREF_KEY_SECRET, "");
//					
//					ConfigurationBuilder confbuilder = new ConfigurationBuilder();
//					Configuration conf = confbuilder
//										.setOAuthConsumerKey(TwitterConst.CONSUMER_KEY)
//										.setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET)
//										.setOAuthAccessToken(oauthAccessToken)
//										.setOAuthAccessTokenSecret(oAuthAccessTokenSecret)
//										.build();
////					twitterStream = new TwitterStreamFactory(conf).getInstance();
//					twitter = new TwitterFactory(conf).getInstance();
//					String screenName = null;
//					User user = null;
//					try {
//						screenName =  twitter.getScreenName();
//						user = twitter.showUser(screenName);
//						
//					} catch (IllegalStateException e) {
//						e.printStackTrace();
//					} catch (TwitterException e) {
//						e.printStackTrace();
//					}
//					return user;
//				}
//		    }.execute("");
//			
//		}  
//	}
//    
//    private boolean isConnected() {
//		return mSharedPreferences.getString(TwitterConst.PREF_KEY_TOKEN, null) != null;
//	}
//    
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
//    }
//    
//    private ClientUser createClientUserFromTwitter(User user){
//    	if(user==null){
//    		return null;
//    	}else{
//    		ClientUser clientUser = new ClientUser();
//    		clientUser.setRegister_from(CommonConstant.TWITTER);
//    		clientUser.setRegister_from_user_id(""+user.getId());
//    		clientUser.setRegister_from_username(user.getScreenName());
//    		clientUser.setRegister_from_name(user.getName());
//    		clientUser.setxxx_user_name(user.getName());
//    		clientUser.setLogin_time(DateUtil.parseDateToString(new Date(), DateUtil.sdf_bidStatus));
//    		return clientUser;
//    	}
//    }
//    
//    private ClientUser createClientUserFromFacebook(GraphUser user){
//    	if(user==null){
//    		return null;
//    	}else{
//    		ClientUser clientUser = new ClientUser();
//    		clientUser.setRegister_from(CommonConstant.FACEBOOK);
//    		clientUser.setRegister_from_user_id(user.getId());
//    		clientUser.setRegister_from_username(user.getUsername());
//    		clientUser.setRegister_from_name(user.getName());
//    		clientUser.setxxx_user_name(user.getName());
//    		clientUser.setLogin_time(DateUtil.parseDateToString(new Date(), DateUtil.sdf_bidStatus));
//    		return clientUser;
//    	}
//    }
//    
//    public static Users createUserFromFacebook(GraphUser graphUser){
//    	if(graphUser==null){
//    		return null;
//    	}else{
//    		Users user = new Users();
//    		user.setUser_id(graphUser.getId());
//    		user.setRegister_from(CommonConstant.FACEBOOK);
//    		user.setRegister_from_user_name(graphUser.getUsername());
//    		user.setFirst_name(graphUser.getFirstName());
//    		user.setLast_name(graphUser.getLastName());
//    		user.setxxx_user_name(graphUser.getName());
//    		
//    		if(graphUser.getLocation()!=null){
//    			GraphLocation gl = graphUser.getLocation();
//    			user.setAddress(gl.getStreet());
//    			user.setCity(gl.getCity());
//    			user.setState(gl.getState());
//    			user.setCountry(gl.getCountry());
//    		}
//    		return user;
//    	}
//    }
//    
//    public static Users createUserFromTwitter(User twitterUser){
//    	if(twitterUser==null){
//    		return null;
//    	}else{
//    		Users user = new Users();
//    		user.setUser_id(""+twitterUser.getId());
//    		user.setRegister_from(CommonConstant.TWITTER);
//    		user.setRegister_from_user_name(twitterUser.getScreenName());
//    		user.setFirst_name("");
//    		user.setLast_name("");
//    		user.setxxx_user_name(twitterUser.getName());
//    		
//			user.setAddress(twitterUser.getLocation());
////    		user.setCity(gl.getCity());
////    		user.setState(gl.getState());
////    		user.setCountry(gl.getCountry());
//    		return user;
//    	}
//    }
//    
//    private void redirectToRegisterPage(){
//    	Intent i = new Intent(getApplicationContext(), RegisterUsingFacebook.class);
//		startActivity(i);
//    }
//    
//    private void redirectToHomePage(){
//    	finish();
////    	Intent intent = new Intent(LoginActivity.this,HomePageActivity.class);
////		startActivity(intent);
//    }
//    
//	private class CreateUserTask extends AsyncTask<Users,String, String> {
//        
//		@Override
//        protected void onPostExecute(String result) {
//			System.out.println("create user response: "+result);
//			Long id = null;
//			if(result!=null){
//				result = result.trim();
//			}
//			try{
//				id = Long.valueOf(result);
//			}catch(Exception e){
//				e.printStackTrace();
//			}
//			if(id!=null){
//				ClientUser cu = dbHander.findUser();
//				if(id<0){ //This user already exists
//					cu.setxxx_user_id((-1* id)+"");
//					dbHander.addUser(cu);
//					if(CommonConstant.FACEBOOK.equalsIgnoreCase(cu.getRegister_from())){
//						facebookDBHandler.addUser(cu.getxxx_user_id(), cu.getRegister_from_user_id(), cu.getRegister_from_name());
//					}else if(CommonConstant.TWITTER.equalsIgnoreCase(cu.getRegister_from())){
//						twitterDBHandler.addUser(cu.getxxx_user_id(), cu.getRegister_from_user_id(), cu.getRegister_from_name());
//					}
//					dismissProgressDialog();
//					redirectToHomePage();
//				}else{
//					cu.setxxx_user_id(id+"");
//					dbHander.addUser(cu);
//					if(CommonConstant.FACEBOOK.equalsIgnoreCase(cu.getRegister_from())){
//						facebookDBHandler.addUser(cu.getxxx_user_id(), cu.getRegister_from_user_id(), cu.getRegister_from_name());
//					}else if(CommonConstant.TWITTER.equalsIgnoreCase(cu.getRegister_from())){
//						twitterDBHandler.addUser(cu.getxxx_user_id(), cu.getRegister_from_user_id(), cu.getRegister_from_name());
//					}
//					dismissProgressDialog();
//					redirectToRegisterPage();
//				}
//			}
//			dismissProgressDialog();
//        }
//		@Override
//		protected String doInBackground(Users... params) {
//			return sendCommentJson(params[0],ServiceEndpointConstant.CREATE_USER_ENDPOINT);
//		}
//    }
//	
//	public static String sendCommentJson(final Users user,String url) {
//                HttpClient client = new DefaultHttpClient();
//                HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
////                HttpResponse response;
//                JSONObject json = new JSONObject();
//                try {
//                    HttpPost post = new HttpPost(url);
//                    json.put("xxx_user_id", user.getxxx_user_id());
//                    json.put("user_id", ""+user.getUser_id());
//                    json.put("xxx_user_name", user.getxxx_user_name());
//                    json.put("xxx_password", user.getxxx_password());
//                    json.put("first_name", user.getFirst_name());
//                    json.put("last_name", user.getLast_name());
//                    json.put("register_from", user.getRegister_from());
//                    json.put("register_from_user_name", user.getRegister_from_user_name());
//                    json.put("birth_day", user.getBirth_day());
//                    json.put("address", user.getAddress());
//                    json.put("city", user.getCity());
//                    json.put("state", user.getState());
//                    json.put("country", user.getCountry());
//                    json.put("email", user.getEmail());
//                    
//                    StringEntity se = new StringEntity( json.toString());  
//                    se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
//                    post.setEntity(se);
//                    HttpResponse response = client.execute(post);
//                    
//                    /*Checking response */
//                    if(response!=null){
//                        InputStream in = response.getEntity().getContent(); //Get the data in the entity
//                        return IOUtil.convertStreamToString(in);
//                    }
//                    return null;
//                } catch(Exception e) {
//                    e.printStackTrace();
//                }
//				return null;
//    }
//	
//	private void showProgressDialog(){
//		pd = new ProgressDialog(this);
//        pd.setMessage("Processing...");
//        pd.setCancelable(false);
//        pd.setIndeterminate(true);
//        pd.show();
//	}
//	
//	private void dismissProgressDialog(){
//		if(pd!=null){
//			pd.dismiss();
//		}
//	}
//}
//
