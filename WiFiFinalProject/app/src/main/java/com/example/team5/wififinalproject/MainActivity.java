package com.example.team5.wififinalproject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity {

    public WifiManager wifiManager;
    public Button btnCollect;
    public Button btnTest;
    public Button btnSaveLocation;
    public Button btnTrain;
    public Button btnPredict;
    public String location = null;
    public EditText editLocation;
    public BroadcastReceiver getWifiBSSID;

    public File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM+"/FinalProject");
    public String trainingset = "TrainingSet.csv";
    public String testset = "TestSet.csv";
    public File TrainingSet = new File(directory, trainingset);
    public File TestSet = new File (directory,testset);

    public int count = 0;

    public PredictionsAPIClient predictions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editLocation = (EditText) findViewById(R.id.editLocation);

        btnSaveLocation = (Button) findViewById(R.id.btnSaveLocation);
        btnCollect = (Button) findViewById(R.id.btnCollect);
        btnCollect.setEnabled(false);

        btnTest = (Button) findViewById(R.id.btnTest);
        btnTest.setEnabled(false);

        btnTrain = (Button) findViewById(R.id.btnTrain);
        btnTrain.setEnabled(false);

        btnPredict = (Button) findViewById(R.id.btnPredict);
        btnPredict.setEnabled(false);

        predictions = new PredictionsAPIClient();
        predictions.setup();
        /*
        try {
            if (!TrainingSet.exists()) {
                TrainingSet.createNewFile();
            }
            if (!TestSet.exists()){
                TestSet.createNewFile();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        */


        btnCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //count = 0;

                wifiManager.startScan();

                List<ScanResult> results = wifiManager.getScanResults();

                try{
                    BufferedWriter bfw = new BufferedWriter(new FileWriter(TrainingSet,true));
                    bfw.write(location+",");
                    bfw.close();
                } catch (IOException e){
                    e.printStackTrace();
                }

                for (int i=0;i<20;i++) {
                    Log.d("WiFi", "SSID=" + results.get(i).SSID + "MAC=" + results.get(i).BSSID);
                    if (i < 19) {
                        try {
                            if (!TrainingSet.exists()){
                                TrainingSet.createNewFile();
                            }
                            BufferedWriter bfw = new BufferedWriter(new FileWriter(TrainingSet, true));
                            bfw.write(results.get(i).BSSID + ",");
                            bfw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }else{
                        try {
                            BufferedWriter bfw = new BufferedWriter(new FileWriter(TrainingSet, true));
                            bfw.write(results.get(i).BSSID);
                            bfw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d("InputLocation", location);


                btnTest.setEnabled(true);
                btnTrain.setEnabled(true);
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.startScan();

                List<ScanResult> results = wifiManager.getScanResults();
                for (int i = 0;i<20;i++) {
                    Log.d("WiFi", "SSID=" + results.get(i).SSID + "MAC=" + results.get(i).BSSID);
                    if (i<19) {
                        try {
                            if (!TestSet.exists()){
                                TestSet.createNewFile();
                            }
                            BufferedWriter bfw = new BufferedWriter(new FileWriter(TestSet, true));
                            bfw.write(results.get(i).BSSID + ",");
                            bfw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            BufferedWriter bfw = new BufferedWriter(new FileWriter(TestSet, true));
                            bfw.write(results.get(i).BSSID);
                            bfw.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        btnSaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location = editLocation.getText().toString();
                btnCollect.setEnabled(true);
            }
        });

        btnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                predictions.train();
                btnPredict.setEnabled(true);
            }
        });

        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predictions.predict();
            }
        });
/*
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                List<ScanResult> results = wifiManager.getScanResults();
                for (ScanResult ap : results) {
                    Log.d("WiFi", "SSID=" + ap.SSID + "MAC=" + ap.BSSID);
                }
                Log.d("InputLocation", location);
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
*/

    }


    @Override
    protected  void onResume(){
        super.onResume();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

    }

    @Override
    protected void onPause() {
        super.onPause();

    }









    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
