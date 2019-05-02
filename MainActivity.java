package com.example.wang.scanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import android.text.format.Time;
public class MainActivity extends AppCompatActivity{
    //WIFI stuff
    private WifiManager wifiManager;
    //list stuff
    private ListView listView;
    private List<ScanResult> results;
  //arraylist stuff
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    //updator
    private Handler mHandler;

    private Handler handlerr;
    //file path
    public String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SSIDInformation";
   //buttons
   private Button buttonScan;
    private Button buttonSave;
    private Button gpsBtn;
    int counter = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.scanBtn);
        buttonSave = findViewById(R.id.saveBtn);
        gpsBtn = findViewById(R.id.gpsBtn);

        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //auto update stuff
        Handler handler = new Handler();
        //file stuff
        File dir = new File(path);
        dir.mkdirs();
        final File file = new File(path + "/ssid.txt");

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        //button stuff
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonSave(file);
            }
        });

        gpsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonGPS(file);
            }
        });


        //arraylist stuff
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);


        //auto update wifi scan every 30 sec
        mHandler = new Handler();
        m_Runnable.run();



    }//end of oncreate

    /*
    gets the long and lat and saves into a string to be appended to a textfile
     */
    public void buttonGPS(File file) {
        GPSTracker gps = new GPSTracker(this);
        double latitude = gps.getLatitude();
        double longitude = gps.getLongitude();

        String lat = Double.toString(latitude);
        String lon = Double.toString(longitude);

        try {
            String stringBuilder = "";
            stringBuilder += "New Section ";
            stringBuilder = stringBuilder + ("\n" + "The latitude is: " + lat + " \n The longitude is: " + lon);
            String[] saveText;
            saveText = stringBuilder.split(System.getProperty("line.separator"));
            Toast.makeText(getApplicationContext(), "Saved the location!", Toast.LENGTH_LONG).show();
            Save2(file, saveText);
        }catch(Exception e){
            System.out.println(e);
        }

    }//end of buttonGPS method


    /*
      parse the arraylist and saves into a string to be appended to a textfile
       */
    public void buttonSave(File file){
        try {
        String stringBuilder = " ";
        //unix time stamp using num of sec since (Jan 1, 1970)
        //String timeStamp = String.valueOf(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));

            stringBuilder = stringBuilder + "\n";
            for(int i = 0; i<arrayList.size();i++){
            //index,date/time,stuff in arraylist
            stringBuilder = stringBuilder + (i+1) + " "+ Clock.getNow()  + " " + arrayList.get(i) + " \n ";
        }
            String[] saveText;
            saveText = stringBuilder.split(System.getProperty("line.separator"));
            Toast.makeText(getApplicationContext(), "Saved list", Toast.LENGTH_LONG).show();
            Save2(file,saveText);
        }catch(Exception e){
            System.out.println(e);
        }
    }////end of buttonSave method


    /*
    to save to a textfile without appending feature
     */
    public void Save(File file, String[] data)
    {
        FileOutputStream fos = null;
        try
        {
                fos = new FileOutputStream(file);

        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        try
        {
            try
            {
                for (int i = 0; i<data.length; i++)
                {
                    fos.write(data[i].getBytes());

                    if (i < data.length-1)
                    {
                        fos.write("\n".getBytes());
                    }
                }
            }
            catch (IOException e) {e.printStackTrace();}
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (IOException e) {e.printStackTrace();}
        }
    }//end of Save method
    /*
       to save to a textfile with appending feature
        */
    public void Save2(File file, String[] data) {


        FileOutputStream fos = null;
        try {

            //append to a end of a textfile
            fos = new FileOutputStream(file, true);

            try {

                for (int i = 0; i < data.length; i++) {
                    fos.write(data[i].getBytes());

                    if (i < data.length - 1) {
                        fos.write("\n".getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }//end of Save2 method

    /*
    Scan the SSID in the area
     */
    private void scanWifi() {
        arrayList.clear();

        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }//end of scanwifi method

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

                for (ScanResult scanResult : results) {
                    arrayList.add(scanResult.SSID + " - " + scanResult.capabilities);
                    adapter.notifyDataSetChanged();

                }

        }
    };

    //Auto update wifi every 15sec
    private final Runnable m_Runnable = new Runnable()
    {
        public void run()
        {
            Toast.makeText(MainActivity.this,"Scanning auto",Toast.LENGTH_SHORT).show();
           //TODO
           //Bad Implementation, placeholder for auto scans and saves to textfile.
           switch(counter){
               case 1 : buttonScan.performClick();
                        gpsBtn.performClick();
                        counter++;
                        break;
               case 2:  buttonSave.performClick();
                        counter--;
                        break;

                default:
                        //oops
                        break;


           }
            MainActivity.this.mHandler.postDelayed(m_Runnable,15000);
        }
    };//runnable


    /*
    to get the date and time
     */
    public static class Clock {
        /**
         * Get current time in human-readable form.
         * @return current time as a string.
         */
        public static String getNow() {
            Time now = new Time();
            now.setToNow();
            String sTime = now.format("%Y_%m_%d %T");
            return sTime;
        }
        /**
         * Get current time in human-readable form without spaces and special characters.
         * The returned value may be used to compose a file name.
         * @return current time as a string.
         */
        public static String getTimeStamp() {
            Time now = new Time();
            now.setToNow();
            String sTime = now.format("%Y_%m_%d_%H_%M_%S");
            return sTime;
        }

    }//end of Clock class

}//end of MainActivity class