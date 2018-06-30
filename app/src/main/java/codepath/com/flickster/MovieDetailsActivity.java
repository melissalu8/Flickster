package codepath.com.flickster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import codepath.com.flickster.models.Config;
import codepath.com.flickster.models.GlideApp;
import codepath.com.flickster.models.Movie;
import cz.msebera.android.httpclient.Header;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

import static codepath.com.flickster.MovieListActivity.API_BASE_URL;

public class MovieDetailsActivity extends AppCompatActivity {

    // constants
    // the base URL for the API
    //public final static String API_BASE_URL = "https://api.themoviedb.org/3";
    // the parameter name for the API YouTube key
    public final static String API_YOUTUBE_KEY_PARAM = "youtube_api_key";
    // tag for logging from this activity
    public final static String TAG = "MovieDetailsActivity";

    // the movie to display
    Movie movie;
    Config config;
    // instance fields
    AsyncHttpClient client;

    // the view objects
    // Automatically finds each field by the specified ID.
    @BindView(R.id.tvTitle) TextView tvTitle;
    @BindView(R.id.tvOverview) TextView tvOverview;
    @BindView(R.id.rbVoteAverage) RatingBar rbVoteAverage;
    @BindView(R.id.ivTrailer) ImageView ivTrailer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        // unwrap the movie passed in via intent, using its simple name as a key
        movie = (Movie) Parcels.unwrap(getIntent().getParcelableExtra(Movie.class.getSimpleName()));
        Log.d("MovieDetailsActivity", String.format("Showing details for '%s'", movie.getTitle()));

        // unwrap the config passed in via intent, using its simple name as a key
        config = (Config) Parcels.unwrap(getIntent().getParcelableExtra(Config.class.getSimpleName()));

        // initalize the client
        client = new AsyncHttpClient();

        // set the title and overview
        tvTitle.setText(movie.getTitle());
        tvOverview.setText(movie.getOverview());

        // vote average is 0..10, convert to 0..5 by dividing by 2
        float voteAverage = movie.getVoteAverage().floatValue();
        rbVoteAverage.setRating(voteAverage = voteAverage > 0 ? voteAverage / 2.0f : voteAverage);

        // set the backdrop image
        // build url for poster image
        String imageUrl = config.getImageUrl(config.getPosterSize(), movie.getBackdropPath());

        // load image using glide
        GlideApp.with(this)
                .load(imageUrl)
                .transform(new RoundedCornersTransformation(25, 0))
                .into(ivTrailer);
    }

    @OnClick(R.id.ivTrailer)
    protected void onClick(View view) {
        // create the url
        String url = API_BASE_URL + "/movie/" + movie.getId() + "/videos";
        // set the request parameters
        RequestParams params = new RequestParams();
        params.put(MovieListActivity.API_KEY_PARAM, getString(R.string.api_key)); // API key, always required
        // execute a GET request expecting a JSON object response
        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                // load the movie trailer
                try {
                    JSONArray results = response.getJSONArray("results");
                    String video = null;
                    JSONObject jsonObject = results.getJSONObject(0);
                    String key = jsonObject.getString("key");

                    // launch the intent
                    // Intent to open MovieTrailerActivity
                    // Passing along key
                    Intent intent = new Intent(MovieDetailsActivity.this, MovieTrailerActivity.class);
                    intent.putExtra("trailer", key);
                    startActivity(intent);
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String
                    responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpost", throwable, true);
            }
        });
    }

    // handle errors, log and alert user
    private void logError(String message, Throwable error, boolean alertUser) {
        // always log the error
        Log.e(TAG, message, error);
        // alert the user to avoid silent errors
        if (alertUser) {
            // show a long toast with the error message
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
