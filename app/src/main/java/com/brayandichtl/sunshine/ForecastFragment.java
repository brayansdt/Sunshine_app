package com.brayandichtl.sunshine;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by brayandichtl on 10/23/14.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> forecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Integer id = item.getItemId();
        if (id == R.id.action_refresh) {
            Log.d("ID DO ITEM TOCADO", id.toString());

            FetchWeatherTask weatherTask = new FetchWeatherTask();
            weatherTask.execute("Brasilia,Br");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        forecastAdapter = new ArrayAdapter<String>(
            //current context (fragment's parent)
            getActivity(),
            //ID of list item layout
            R.layout.list_item_forecast,
            //ID of the textView to populate
            R.id.list_item_forecast_textview
        );

        FetchWeatherTask fetchWeather = new FetchWeatherTask();
        fetchWeather.execute("Brasilia,Br");

        ListView forecastListView = (ListView) rootView.findViewById(R.id.listview_forecast);
        forecastListView.setAdapter(forecastAdapter);

        forecastListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Context context = getActivity();
                CharSequence fooText = forecastAdapter.getItem(i);

                Toast fooToast = Toast.makeText(context, fooText, Toast.LENGTH_SHORT);
                fooToast.show();
            }
        });

        return rootView;
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /**
         * The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time) {
            Date date = new Date(time * 1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("E, MMM d");
            return dateFormat.format(date).toString();
        }

        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "°/" + roundedLow + "°";
            return highLowStr;
        }

        /**
         * Take the string containing the forecast JSON and pull out the data
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DATETIME = "dt";
            final String OWM_DESCRIPTION = "main";

            //extraindo lista de dias do json
            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultsStr = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                //get JSON's day
                JSONObject dayForeCastJson = weatherArray.getJSONObject(i);

                //converting datetime
                long datetime = dayForeCastJson.getLong(OWM_DATETIME);
                day = getReadableDateString(datetime);

                //getting description @todo: testar utilizando  "weather" corrente da list "dayForeCastJson.getJSONObject(OWM_WEATHER);
                JSONObject weather = dayForeCastJson.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weather.getString(OWM_DESCRIPTION);

                //temperatures
                JSONObject temperature = dayForeCastJson.getJSONObject(OWM_TEMPERATURE);
                double high = temperature.getDouble(OWM_MAX);
                double low = temperature.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultsStr[i] = day + " - " + description + " - " + highAndLow;
            }

            return resultsStr;
        }


        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if(result != null){
                forecastAdapter.clear();
                for(String info: result){
                    forecastAdapter.add(info);
                }
            }
        }

        @Override
        protected String[] doInBackground(String... params) {

            if (params.length == 0) {
                Log.e(LOG_TAG, "You must pass a localization");
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String forecastJsonStr = null;


            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                final String FORECAST_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMART_PARAM = "mode";
                final String UNIT_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String LANGUAGE = "lang";

                Uri builtUri = Uri.parse(FORECAST_URL).buildUpon().
                        appendQueryParameter(QUERY_PARAM, params[0]).
                        appendQueryParameter(FORMART_PARAM, format).
                        appendQueryParameter(UNIT_PARAM, units).
                        appendQueryParameter(DAYS_PARAM, Integer.toString(numDays)).
                        appendQueryParameter(LANGUAGE, "pt").
                        build();


                URL forecastEndPoint = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) forecastEndPoint.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                forecastJsonStr = buffer.toString();

                try {
                    return getWeatherDataFromJson(forecastJsonStr, numDays);
                } catch (JSONException exception) {
                    Log.e(LOG_TAG, exception.toString());
                }


            } catch (IOException exception) {
                Log.e(LOG_TAG, "Error", exception);
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException exc) {
                        Log.e("ForecastFragment", "Error closing stream", exc);
                    }
                }
            }

            return null;
        }

    }

}