package com.jeffrey.dynamicclock;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private ImageView img;
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		img = (ImageView) findViewById(R.id.testImg);
		IconScript clockscript=new IconScript(MainActivity.this, img);
		img.setImageDrawable(clockscript);
		clockscript.run();
	}
}
