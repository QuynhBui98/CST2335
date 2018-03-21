package com.example.johnnytunguyen.androidlabs;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.lang.String.format;

public class WeatherForecast extends Activity {

    final static String NAME = "Lab 6: ";
    ProgressBar prB ;
    TextView current,min,max;
    ImageView img ;
    String url = "http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);
        AnhXa();
        new ForecastQuery().execute("http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric");
    }

    public void AnhXa(){
        current = findViewById(R.id.currentTemp);
        min = findViewById(R.id.minTemp);
        max = findViewById(R.id.maxTemp);
        img = findViewById(R.id.WeatherImg);
        prB = findViewById(R.id.progressBar);
        prB.setVisibility(View.VISIBLE);

    }


     private class ForecastQuery extends AsyncTask<String,Integer,String>{


        private String currentTemp1=null;
        private String minTemp1 = null;
        private String maxTemp1 = null;
        private String iconFilename = null;
        private Bitmap weatherImage = null;





        @Override
        protected String doInBackground(String... params) {
            InputStream inputStream = null;
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 );
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                inputStream = conn.getInputStream();
                XmlPullParser parser = Xml.newPullParser();
//                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(inputStream, null);

                int eventType = parser.next();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() != XmlPullParser.START_TAG) {
                        continue;
                    }

                    if (parser.getName().equalsIgnoreCase("temperature")){
                              currentTemp1 = parser.getAttributeValue(null,"value");
                             publishProgress(25);
                            minTemp1 = parser.getAttributeValue(null, "min");
                             publishProgress(50);
                             maxTemp1 = parser.getAttributeValue(null, "max");
                             publishProgress(75);
                         }

                    else if (parser.getName().equalsIgnoreCase("weather")){
                             iconFilename = parser.getAttributeValue(null,"icon");
                             String fileTemp = iconFilename+".png";
                             File file = getBaseContext().getFileStreamPath(fileTemp);
                             if (file.exists()) {
                                 FileInputStream Stream=null;
                                 try {
                                     Stream = new FileInputStream(file);
                                 }catch (Exception e){
                                     e.getMessage();
                                 }
                                 weatherImage = BitmapFactory.decodeStream(inputStream);
                                 Log.i(NAME, "Image already exists");

                             } else {
                                 URL iconUrl = new URL("http://openweathermap.org/img/w/" + iconFilename+".png");
                                 weatherImage = getImage(iconUrl);
                                 FileOutputStream outputStream = openFileOutput(iconFilename+ ".png", Context.MODE_PRIVATE);
                                 weatherImage.compress(Bitmap.CompressFormat.PNG, 80, outputStream);
                                 outputStream.flush();
                                 outputStream.close();
                                 Log.i(NAME, "Adding new image");
                             }
                             publishProgress(100);
                         }

                    eventType = parser.next();}

            } catch (IOException e) {
                Log.i(NAME, "IOException: " + e.getMessage());
            } catch (XmlPullParserException e) {
                Log.i(NAME, "XmlPullParserException: " + e.getMessage());
            } finally {
                if (inputStream != null)
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.i(NAME, "IOException: " + e.getMessage());
                    }
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            prB.setProgress(values[0]);
//
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            current.setText(currentTemp1);
            min.setText(minTemp1);
            max.setText(maxTemp1);
            img.setImageBitmap(weatherImage);
            prB.setVisibility(View.INVISIBLE);

        }

         private Bitmap getImage(URL url) {
             HttpURLConnection connection = null;
             try {
//                 URL url = new URL("url");
                 connection = (HttpURLConnection) url.openConnection();
                 connection.connect();
                 int responseCode = connection.getResponseCode();
                 if (responseCode == 200) {
                     weatherImage = BitmapFactory.decodeStream(connection.getInputStream());
                     return BitmapFactory.decodeStream(connection.getInputStream());
                 } else {
                     return null;
                 }
             } catch (Exception e) {
                 e.printStackTrace();
                 return null;

             } finally {
                 if (connection != null) {
                     connection.disconnect();
                 }
             }
         }


    }
}
