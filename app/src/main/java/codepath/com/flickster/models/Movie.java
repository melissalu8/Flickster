package codepath.com.flickster.models;

import org.json.JSONException;
import org.json.JSONObject;

public class Movie {

    // values from API
    private String title;
    private String overview;
    private String posterPath; // only the path

    // intialize from JSON data
    public Movie(JSONObject object) throws JSONException {
        title = object.getString("title");
        overview = object.getString("overview");
        posterPath = object.getString("poster_path");
        // note these are private so can only be accessed in the Movie class
        // need to expose it to the Activity by using getters
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterPath() {
        return posterPath;
    }
}
