package com.gebogebo.android.distancecalcfree;

import com.sensedk.AswAdLayout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

/**
 * simple activity which displays report for distance calculator'ssession 
 * 
 * @author shweta
 */
public class DistanceCalculatorReportActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        DistanceCalcReport report = (DistanceCalcReport)getIntent().getSerializableExtra("com.gebogebo.distancecalc.report");
        if(report == null) {
            //if report object is not set in intent, show error and go back
            Toast.makeText(this, R.string.reportGenerationError, Toast.LENGTH_LONG).show();
            this.finish();
        }
        setContentView(R.layout.report);
        
        AswAdLayout senseAd = (AswAdLayout)findViewById(R.id.adview);
        senseAd.setActivity(this);
        //adView.userDemandToDeleteHisData();
        //adView.userOptOutFromRecommendation();

        
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_back) {
            Log.i("activity", "back selected");
            this.finish();
        } else if (item.getItemId() == R.id.menu_help) {
            Log.i("activity", "Help selected");
            Intent helpIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://distancecalculator.gebogebo.com/help"));
            startActivity(helpIntent);
        } else if (item.getItemId() == R.id.menu_capture) {
            DistanceCalculatorUtilities.saveParentView(findViewById(R.id.reportTitleText).getRootView(), this);
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
