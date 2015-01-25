package com.xxx.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;

public class VideoPlayerActivity extends Activity {

	
	WebView wv;
	
	String videoURL = "";
	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		
		Intent intent=this.getIntent();
		videoURL = intent.getStringExtra("videoURL");
		
		wv = (WebView) findViewById(R.id.VideoView);
		wv.getSettings().setJavaScriptEnabled(true);

		wv.getSettings().setPluginState(PluginState.ON);
		WebSettings webSettings = wv.getSettings();

		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setAllowContentAccess(true);
		webSettings.setDomStorageEnabled(true);
		webSettings.setRenderPriority(RenderPriority.HIGH);
		webSettings.setUseWideViewPort(false);
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		// --------------------------------------------------------
		// if (Build.VERSION.SDK_INT < 8) {
		// webSettings.setPluginsEnabled(true);
		// } else {
		webSettings.setPluginState(PluginState.ON);
		// }
		WebChromeClient mWebChromeClient = new WebChromeClient();
		wv.setWebChromeClient(mWebChromeClient);
//		wv.setBackgroundColor(0x00000000);
		wv.setBackgroundColor(Color.argb(1, 0, 0, 0));
		final String mimeType = "text/html";
		final String encoding = "UTF-8";
		String html = getHTML();
		wv.loadDataWithBaseURL("", html, mimeType, encoding, "");

	}

	@Override
	public void onPause()
	{
	    super.onPause();
	    toggleWebViewState(true);
	}

	@Override
	public void onResume()
	{
	    super.onResume();
	    toggleWebViewState(false);
	}

	private void toggleWebViewState(boolean pause)
	{    
		if(wv!=null){
			try
		    {
		        Class.forName("android.webkit.WebView")
		        .getMethod(pause
		                ? "onPause"
		                : "onResume", (Class[]) null)
		        .invoke(wv, (Object[]) null);
		    }
		    catch (Exception e){
		    	e.printStackTrace();
		    }
		}
	    
	}
	
	public String getHTML() {
		String html = "<iframe class=\"youtube-player\" style=\"border: 0;background-color:#000000; width: 100%; height: 100%; padding:0px; margin:0px\" id=\"ytplayer\" type=\"text/html\" src=\""+videoURL+"\" frameborder=\"0\">\n"
				+ "</iframe>\n";
		return html;
	}
}
