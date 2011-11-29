package com.gebogebo.android.distancecalcfree;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * simple activity which displays report for distance calculator'ssession 
 * 
 * @author shweta
 */
public class DistanceCalculatorReportActivity extends Activity {
    public static final String INTENT_PARAM_AUTO_SAVE = "com.gebogebo.distancecalcpaid.report.autosave";
    public static final String INTENT_PARAM_AUTO_REPORT = "com.gebogebo.distancecalc.report";
    
    private boolean saveWhenRendered = false;
    private AdView adView = null; 

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        DistanceCalcReport report = (DistanceCalcReport)getIntent().getSerializableExtra(INTENT_PARAM_AUTO_REPORT);
        if(report == null) {
            //if report object is not set in intent, show error and go back
            Toast.makeText(this, R.string.reportGenerationError, Toast.LENGTH_LONG).show();
            this.finish();
        }
        if (getIntent().getSerializableExtra(INTENT_PARAM_AUTO_SAVE) != null) {
            Log.i("report", "trying to see if report needs to be saved after rendered");
            saveWhenRendered = (Boolean) getIntent().getSerializableExtra(INTENT_PARAM_AUTO_SAVE);
        }
        Log.i("report", "saveWhenRendered: " + saveWhenRendered);
        
        setContentView(R.layout.report);
        
        // AdManager.setTestDevices( new String[] { AdManager.TEST_EMULATOR,
        // "CA101E12F9C3DF4E8301247EF68FB13C" } );
        adView = new AdView(this, AdSize.BANNER, DistanceCalculatorUtilities.ADMOB_KEY);
        adView.loadAd(new AdRequest());
        ((LinearLayout)findViewById(R.id.reportLayout)).addView(adView);
        
        TextView view = (TextView) findViewById(R.id.totalDistanceCovered);
        view.setText(report.getTotalDistanceString());

        view = (TextView) findViewById(R.id.totalTimeTaken);
        view.setText(report.getTotalTimeString());
        
        view = (TextView) findViewById(R.id.totalTimePaused);
        view.setText(report.getTotalTimePausedString());
        
        view = (TextView) findViewById(R.id.minSpeed);
        view.setText(report.getMinSpeedString());
        
        view = (TextView) findViewById(R.id.maxSpeed);
        view.setText(report.getMaxSpeedString());
        
        view = (TextView) findViewById(R.id.avgSpeed);
        view.setText(report.getAvgSpeed());
        
        view = (TextView) findViewById(R.id.currentTime);
        view.setText(report.getCurrentTime());
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // this activity is used specially when report is to be saved when rendered. it is important
        // that report is saved only when rendered so as to get its dimensions correctly
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && saveWhenRendered) {
            saveWhenRendered = false;
            View v = findViewById(R.id.reportTitleText).getRootView();
            DistanceCalculatorUtilities.saveParentView(v, this, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_back) {
            Log.i("activity", "back selected");
            this.finish();
        } else if (item.getItemId() == R.id.menu_help) {
            Log.i("activity", "Help selected");
            Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://distancecalculator.gebogebo.com/help"));
            startActivity(helpIntent);
        } else if(item.getItemId() == R.id.share) {
            Log.i("menu", "Share with selected");
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            String tmpFilename = DistanceCalculatorUtilities.saveParentView(findViewById(R.id.reportTitleText).getRootView(), this, true);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + tmpFilename));
            startActivity(Intent.createChooser(intent, getString(R.string.share)));
        } else if (item.getItemId() == R.id.menu_capture) {
            DistanceCalculatorUtilities.saveParentView(findViewById(R.id.reportTitleText).getRootView(), this, false);
            Log.i("activity", "successfully captured screenshot");
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.report_menu, menu);
        Log.i("menu", "Options menu created");
        return true;
    }
}
