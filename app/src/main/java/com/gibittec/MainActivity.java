package com.gibittec;


import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.Camera.Parameters;
import android.hardware.SensorListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements OnSeekBarChangeListener,SensorListener{

	ImageButton btnSwitch;
	private Camera camera;
	private boolean isFlashOn;
	private boolean hasFlash;
	Parameters params;
	MediaPlayer mp;

	SeekBar seek1;
	CountDownTimer cdt = null;
	boolean lton=true;
	LinearLayout compassbg;
	ImageCompass ImageCompass;
	SensorManager sensorManager;
	static final int sensor = SensorManager.SENSOR_ORIENTATION;
	TextView batteryInfo;
	
	AdView adView;
	AdRequest request;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// flash switch button
		btnSwitch = (ImageButton) findViewById(R.id.btnSwitch);
		compassbg=(LinearLayout)findViewById(R.id.compassbg);
		seek1=(SeekBar)findViewById(R.id.seekBar1);
		batteryInfo=(TextView)findViewById(R.id.bettaryinfo);

		seek1.setOnSeekBarChangeListener(this);
		
		ImageCompass = new ImageCompass(this);
		compassbg.addView(ImageCompass);

		adView=(AdView)findViewById(R.id.adView);
		request = new AdRequest();
		adView.loadAd(request); 
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		sensorManager.registerListener(this, sensor);
		this.registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


		hasFlash = getApplicationContext().getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

		if (!hasFlash) {
			// device doesn't support flash
			// Show alert message and close the application
			AlertDialog alert = new AlertDialog.Builder(MainActivity.this)
					.create();
			alert.setTitle("Error");
			alert.setMessage("Sorry, your device doesn't support flash light!");
			alert.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// closing the application
					finish();
				}
			});
			alert.show();
			return;
		}

		// get the camera
		getCamera();
		
		toggleButtonImage();
		seek1.setEnabled(false);
		
		btnSwitch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (isFlashOn) {
					// turn off flash
					turnOffFlash();
				} else {
					// turn on flash
					turnOnFlash();
				}
			}
		});
	}

	/*
	 * Get the camera
	 */
	private void getCamera() {
		if (camera == null) {
			try {
				camera = Camera.open();
				params = camera.getParameters();
			} catch (RuntimeException e) {
				Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
			}
		}
	}

	/*
	 * Turning On flash
	 */
	private void turnOnFlash() {
		if (!isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
			// play sound
			playSound();
			seek1.setEnabled(true);
			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_TORCH);
			camera.setParameters(params);
			camera.startPreview();
			isFlashOn = true;
			
			// changing button/switch image
			toggleButtonImage();
		}

	}

	/*
	 * Turning Off flash
	 */
	private void turnOffFlash() {
		if (isFlashOn) {
			if (camera == null || params == null) {
				return;
			}
			// play sound
			playSound();
			if (cdt != null) {
				cdt.cancel();
			}
			seek1.setEnabled(false);
			params = camera.getParameters();
			params.setFlashMode(Parameters.FLASH_MODE_OFF);
			camera.setParameters(params);
			camera.stopPreview();
			isFlashOn = false;
			toggleButtonImage();
		}
	}
	
	/*
	 * Playing sound
	 * will play button toggle sound on flash on / off
	 * */
	private void playSound(){
		if(isFlashOn){
			mp = MediaPlayer.create(MainActivity.this, R.raw.light_switch_off);
		}else{
			mp = MediaPlayer.create(MainActivity.this, R.raw.light_switch_on);
		}
		mp.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.release();
            }
        }); 
		mp.start();
	}
	
	/*
	 * Toggle switch button images
	 * changing image states to on / off
	 * */
	private void toggleButtonImage(){
		if(isFlashOn){
			btnSwitch.setImageResource(R.drawable.on);
		}else{
			btnSwitch.setImageResource(R.drawable.off);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sensorManager.unregisterListener(this);
		unregisterReceiver(batteryInfoReceiver);
	}
	@Override
	protected void onPause() {
		super.onPause();
		turnOffFlash();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		this.registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(hasFlash)
			turnOnFlash();
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		// on starting the app get the camera params
		getCamera();
	}

	@Override
	protected void onStop() {
		super.onStop();
		// on stop release the camera
		if (camera != null) {
			camera.release();
			if(cdt != null){
				cdt.cancel();	
			}
			sensorManager.unregisterListener(this);
			camera = null;
		}
	}
	private void blinkLight(int interval) {

		if(cdt != null)			
		{
			cdt.cancel();
			cdt = null;	
		}
		lton=true;
	    cdt = new CountDownTimer(500*60*1000, interval) {
			@Override
			public void onTick(long millisUntilFinished) {
				//isCdtRunning = true;
				if(lton){
					params = camera.getParameters();
					params.setFlashMode(Parameters.FLASH_MODE_OFF);
					camera.setParameters(params);						
					//	compassbg.setVisibility(View.INVISIBLE);
				}
				else{
					params =camera.getParameters();
					params.setFlashMode(Parameters.FLASH_MODE_TORCH);
					camera.setParameters(params);
					//compassbg.setVisibility(View.VISIBLE);
				}
				lton=!lton;

			}				
			@Override
			public void onFinish() {			
				//if (isFinishing()==false) updateView();	
			}
		}.start();

	}
	
	///seekbar
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		if(isFlashOn){
			int value=seek1.getProgress();
			if(value > 0){
				int fv= (10 - value) * 100 ;
				blinkLight(fv);
			}				
		}else{
			seek1.setEnabled(false);
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	//sensor
	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(int sensor, float[] values) {
		// TODO Auto-generated method stub
		if (sensor != MainActivity.sensor)
			return;
		int orientation = (int) values[0];
		ImageCompass.setDirection(orientation);
	}
	
	private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int level= intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
			batteryInfo.setText(String.valueOf(level)+"%");
		}
	};

}
