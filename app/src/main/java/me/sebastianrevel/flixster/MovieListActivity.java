package me.sebastianrevel.flixster;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.sebastianrevel.flixster.models.Movie;

import cz.msebera.android.httpclient.Header;

public class MovieListActivity extends AppCompatActivity {

    // constants
    // the base URL for the API
    public final static String API_BASE_URL = "https://api.themoviedb.org/3";

    public final static String API_KEY_PARAM = "api_key";
    //tag for logging from this activity
    public final static String TAG = "MovieListActivity";

    AsyncHttpClient client;

    // base url
    String imageBaseUrl;
    // poster size
    String posterSize;
    //list of movies
    ArrayList<Movie> movies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);
        // init client
        client = new AsyncHttpClient();
        //initializing movie list
        movies = new ArrayList<>();
        // get the configuration
        printThings("get config called");
        getConfiguration();
    }

    //get current movie list
    private void getNowPlaying()
    {
        // creating a url
        String url = API_BASE_URL + "/movie/now_playing";
        // setting request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); //The API key is always required when requesting a param.
        printThings("about to call client in nowplaying");
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load movies into list
                try {
                    printThings("made it through");
                    JSONArray results = response.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        Movie movie = new Movie(results.getJSONObject(i));
                        movies.add(movie);
                    }
                    Log.i(TAG, String.format("Loaded %s movies", results.length()));
                } catch (JSONException e) {
                    logError("Failure during getNowPlaying", e, true);
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                logError("Failed to get data from now_playing endpoint", throwable, true);
            }
        });
    }

    //method that will access this configuration endpoint
    private void getConfiguration() {
        // creating a url
        String url = API_BASE_URL + "/configuration";
        // setting request parameters
        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.api_key)); //The API key is always required when requesting a param.
        printThings("about to call client" + url);
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    printThings("in config on success");
                    JSONObject images = response.getJSONObject("images");

                    imageBaseUrl = images.getString("secure_base_url");

                    JSONArray posterSizeOptions = images.getJSONArray("poster_sizes");

                    posterSize = posterSizeOptions.optString(4, "w342");

                    Log.i(TAG, String.format("Loaded configuration with imageBaseUrl %s and posterSize %s", imageBaseUrl, posterSize));
                    // get movie list
                    getNowPlaying();
                    //if we can't retrieve the secure_base_url, it will log an error.
                } catch (JSONException e) {
                    logError("Failure while parsing", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                logError("Failed getting configuration", throwable, true);
            }
        });
    }

    //handle errors and alert the user.
    private void logError(String message, Throwable error, boolean alertUser) {
        //always log the error
        Log.e(TAG, message, error);
        //alert user
        if (alertUser) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void printThings(String message) {
        Log.d("+++++ PRINTED +++++", message);
    }
}
