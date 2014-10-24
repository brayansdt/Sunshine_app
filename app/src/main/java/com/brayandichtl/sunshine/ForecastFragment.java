package com.brayandichtl.sunshine;

import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by brayandichtl on 10/23/14.
 */
public class ForecastFragment extends Fragment {

    public ForecastFragment() {
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... strings) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String forecastJsonStr = null;

            try{
                URL forecastEndPoint = new URL( strings[0] );

                urlConnection = (HttpURLConnection) forecastEndPoint.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ( (line = reader.readLine() ) != null){
                    buffer.append(line+"\n");
                }

                forecastJsonStr = buffer.toString();

            }catch(IOException exception){
                Log.e(LOG_TAG, "Error", exception);
            }finally {

                if(urlConnection != null){
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try{
                        reader.close();
                    }catch (final IOException exc){
                        Log.e("ForecastFragment", "Error closing stream", exc);
                    }
                }
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        String[] forecasts = {
                "Today — Sunny — 88/63",
                "Tomorrow — Foggy — 70/46",
                "Weds — Cloudy — 72/63",
                "Thurs — Rainy — 64/51",
                "Fri — Foggy — 70/46",
                "Sat — Sunny — 88/63",
                "Sun — Cloudy — 72/62"
        };


        ArrayAdapter<String> forecastAdapter = new ArrayAdapter<String>(
                //current context (fragment's parent)
                getActivity(),
                //ID of list item layout
                R.layout.list_item_forecast,
                //ID of the textView to populate
                R.id.list_item_forecast_textview,
                //Forecast data
                forecasts
        );

        ListView forecastList = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastList.setAdapter(forecastAdapter);

//        String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=Brasilia,Br&mode=json&units=metric&cnt=7";
//        FetchWeatherTask endPointTask = new FetchWeatherTask();
//        endPointTask.doInBackground(url);

        return rootView;

    }
}