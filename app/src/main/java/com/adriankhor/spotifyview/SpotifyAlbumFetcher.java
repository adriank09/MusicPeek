package com.adriankhor.spotifyview;

import android.net.Uri;
import android.util.Log;

import com.adriankhor.spotifyview.model.SpotifyTrack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by adriank09 on 04/03/2017.
 */

public class SpotifyAlbumFetcher {
    private static final String TAG = "SpotifyAlbumFetcher";
    private static final String SPOTIFY_WEB_API_BASE_URL = "https://api.spotify.com/";
    private static final String SPOTIFY_WEB_API_SEARCH_URL = "https://api.spotify.com/v1/search";
    private static final String SPOTIFY_WEB_API_TRACK_SEARCH_URL = "https://api.spotify.com/v1/tracks/";


    public List<SpotifyTrack> fetchTracks() {
        // will change later to randomized set of keywords
        return searchTracks("mika");
    }

    public List<SpotifyTrack> searchTracks(String query) {
        List<SpotifyTrack> spotifyTracks = new ArrayList<>();
        try {
            String url = Uri.parse(SPOTIFY_WEB_API_SEARCH_URL)
                    .buildUpon()
                    .appendQueryParameter("q", query)
                    .appendQueryParameter("type", "track")
                    .build().toString();
            String jsonString = getUrlString(url);
            JSONObject body = new JSONObject(jsonString);
            parseItems(spotifyTracks, body);
        }
        catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return spotifyTracks;
    }

    public SpotifyTrack getTrack(Uri uri) {
        SpotifyTrack track = null;

        try {
            String url = SPOTIFY_WEB_API_TRACK_SEARCH_URL + uri.toString();
            String jsonString = getUrlString(url);
            JSONObject body = new JSONObject(jsonString);

            if(body != null) {
                track = new SpotifyTrack();

                track.setArtistName(getArtistName(body));
                track.setTrackPreviewImage(getPreviewImage(body, 300));
                track.setName(body.getString("name"));
                track.setId(body.getString("id"));
                track.setPreviewUri(Uri.parse(body.getString("preview_url")));
            }
        }
        catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }
        catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }

        return track;
    }


    // gets the URL
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    // gets the data chunk (array bytes)
    public byte[] getUrlBytes (String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();

            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    // private - parse items
    private void parseItems(List<SpotifyTrack> tracks, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject tracksJSONObject = jsonBody.getJSONObject("tracks");
        JSONArray trackJSONArray = tracksJSONObject.getJSONArray("items");

        for(int i=0;i<trackJSONArray.length();i++){
            JSONObject obj = trackJSONArray.getJSONObject(i);

            // new instace of SpotifyTrack
            SpotifyTrack track = new SpotifyTrack();
            track.setId(obj.getString("id"));
            track.setName(obj.getString("name"));

            // set the artists name
            track.setArtistName(getArtistName(obj));

            // set the preview media url link
            Uri previewUrl = Uri.parse(obj.getString("preview_url"));
            track.setPreviewUri(previewUrl);

            // available dimensions - 640, 300, 64
            Uri imageUrl = getPreviewImage(obj, 300);
            track.setTrackPreviewImage(imageUrl);

            // build complete - add into lists of tracks
            tracks.add(track);
        }
    }

    // gets the preview image from the image JSON array with specified dimension
    private Uri getPreviewImage(JSONObject obj, int dimension) throws JSONException {
        JSONObject item = obj.getJSONObject("album");
        JSONArray imagesArray = item.getJSONArray("images");
        Uri uri = null;

        for(int i=0;i<imagesArray.length();i++) {
            JSONObject imageUri = imagesArray.getJSONObject(i);

            if(Integer.parseInt(imageUri.get("height").toString()) == dimension) {
                uri = Uri.parse(imageUri.get("url").toString());
                break;
            }
        }

        return uri;
    }

    // gets the artist name for the given track
    private String getArtistName(JSONObject obj) throws JSONException {
        JSONArray artistsArray = obj.getJSONArray("artists");
        StringBuilder stringBuilder = new StringBuilder();

        for(int i=0;i<artistsArray.length();i++) {
            JSONObject artist = artistsArray.getJSONObject(i);

            if(artistsArray.length() == 1) {
                stringBuilder.append(artist.getString("name"));
                break;
            }

            stringBuilder.append(artist.getString("name")).append(", ");
        }

        return stringBuilder.toString();
    }
}
