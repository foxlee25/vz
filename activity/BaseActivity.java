package com.vizoal.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xxx.domain.ClientUser;
import com.xxx.service.UserDBHander;

public class BaseActivity extends ActionBarActivity {

	protected boolean signed_in = false;
	
	protected ClientUser user;

	UserDBHander dbHander = new UserDBHander(this);
	
	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.base_actions, menu);
	    setup_search(menu);	    
	    return super.onCreateOptionsMenu(menu);
	}

	public void setup_search(Menu menu) {
		MenuItem searchItem = menu.findItem(R.id.action_search);
	    SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    // Assumes current activity is the searchable activity
	    SearchableInfo si = searchManager.getSearchableInfo(getComponentName());
	    if (si == null) {
            System.out.println("failed to get searchable info");
        }
	    searchView.setSearchableInfo(si);
	    searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default
	}
	
//	@Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//		user = dbHander.findUser();
//		if(user!=null){
//			signed_in = true;
//		}
//		MenuItem sign_in_btn = menu.findItem(R.id.action_sign_in);
//        if (signed_in) {
//            sign_in_btn.setTitle("Sign out");
//        }
//        else {
//            sign_in_btn.setTitle("Sign in");
//        }
//        return super.onPrepareOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Intent intent = new Intent(this, HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;    
            case R.id.action_search:
        	    return true;
            case R.id.team_followed:
            	startActivityForResult(new Intent(this, FavTeamActivity.class), 0);
            	return true;
//            case R.id.action_feedback:
//            	Toast.makeText(getBaseContext(), "Feedback pressed!", Toast.LENGTH_SHORT).show();
//            	return true;
//            case R.id.action_help:
//            	Toast.makeText(getBaseContext(), "Help pressed!", Toast.LENGTH_SHORT).show();
//            	return true;
//            case R.id.action_sign_in:
//              	if (signed_in) {
//              		dbHander.deleteUser();
//              		signed_in = false;
//		        }
//              	else {
//		        	startActivityForResult(new Intent(this, LoginActivity.class), 0);
//		            signed_in = true;
//		        }
//              	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
