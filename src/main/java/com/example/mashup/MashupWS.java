package com.example.mashup;
import com.google.common.util.concurrent.RateLimiter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@RestController
public class MashupWS {
    /**
     * Creats a thread pool to make it possible to parallelize work.
     */
    private ExecutorService executor = Executors.newFixedThreadPool(100);
    /**
     * The implemented ratelimiters make it possible do service many requests to the webservice
     * without exceeding the request rate limits set by the consumed APIs
     * mbRateLimiter controls number of questions per second to MusicBrainz, set to 1/s
     */
    RateLimiter mbRateLimiter = RateLimiter.create(1);

    /**
     * Spring Rest service returning a information about a artist/group from MusicBrainz, Wikipedia and Cover Art archive
     *
     * @param mbid - which artist to ask the webservice for
     * @return return the response object in json format
     * @throws UnsupportedEncodingException
     */
    @GetMapping(value = "/mashupws", produces = MediaType.APPLICATION_JSON_VALUE)
    public WsResponse response(@RequestParam(value = "mbid", defaultValue = "") String mbid) throws UnsupportedEncodingException {
        WsResponse response = new WsResponse(mbid);
        mbRateLimiter.acquire(1);
        //Consume MusicBrainzAPI
        String wikidataID = ExtractMusicBrainzInfo(mbid, response);
        if (wikidataID != "") {
            //Consume Cover art archive
            ExtractCoverArtArchiveAPI(response);
            //Consume wikidata
            JSONObject wdJasonObject = GetJsonFromApi("https://www.wikidata.org/w/api.php?action=wbgetentities&ids=" + wikidataID + "&format=json&props=sitelinks");
            if (wdJasonObject != null) {
                ExtractWikiDataAPI(wdJasonObject, wikidataID, response);
                //consume wikipedia
                JSONObject wpJasonObject = GetJsonFromApi("https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&redirects=true&titles=" + URLEncoder.encode(response.getName(), "UTF-8"));
                if (wpJasonObject != null) {
                    ExtractWikipediaAPI(wpJasonObject, response);
                }
            }
        }
        return response;
    }

    /**
     * Extracts information from musicbrainz API - http://musicbrainz.org/ws/2/artist/., Documentation - https://musicbrainz.org/doc/Development/XML_Web_Service/Version_2
     *
     * @param mbid     - MusicBrainz ID, artist to searchfor
     * @param response - Object to populate
     * @return - Object with the acquired information
     */
    private String ExtractMusicBrainzInfo(String mbid, WsResponse response) {
        String wikidataID = "";
        JSONParser parse = new JSONParser();
        JSONObject mbJasonObj = GetJsonFromApi("http://musicbrainz.org/ws/2/artist/" + mbid + "?&fmt=json&inc=url-rels+release-groups");
        JSONArray relationsArray = (JSONArray) mbJasonObj.get("relations");
        for (int i = 0; i < relationsArray.size(); i++) {
            JSONObject relationsJsonObject = (JSONObject) relationsArray.get(i);
            if (relationsJsonObject.get("type").toString().equals("wikidata")) {
                JSONObject urlobj = null;
                try {
                    urlobj = (JSONObject) parse.parse(relationsJsonObject.get("url").toString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String urlStr = urlobj.get("resource").toString();
                wikidataID = urlStr.substring(urlStr.lastIndexOf("/") + 1);
            }
        }

        JSONArray releaseGroupArray = (JSONArray) mbJasonObj.get("release-groups");
        for (int i = 0; i < releaseGroupArray.size(); i++) {
            JSONObject releaseGroup = (JSONObject) releaseGroupArray.get(i);
            Album album = new Album(releaseGroup.get("title").toString());
            album.setId(releaseGroup.get("id").toString());
            response.addAlbum(album);
        }
        return wikidataID;
    }

    /**
     * Extracts information from wikiData API - https://www.wikidata.org/w/api.php. documentation - https://www.wikidata.org/w/api.php
     * extracts the link to wikipedia for the selected artist
     *
     * @param wdJasonObject
     * @param wikidataID
     * @param response
     */
    private void ExtractWikiDataAPI(JSONObject wdJasonObject, String wikidataID, WsResponse response) {
        JSONObject entitiesObj = (JSONObject) wdJasonObject.get("entities");
        JSONObject wikidataIDObj = (JSONObject) entitiesObj.get(wikidataID);
        JSONObject sitelinksObj = (JSONObject) wikidataIDObj.get("sitelinks");
        JSONObject enwikiObj = (JSONObject) sitelinksObj.get("enwiki");
        response.setName(enwikiObj.get("title").toString());
    }

    /**
     * Extract from wikipedia for artist
     *
     * @param wpJsonOject -  Json Object to parse information from
     * @param response    Object with the acquired information
     */
    private void ExtractWikipediaAPI(JSONObject wpJsonOject, WsResponse response) {
        JSONObject queryObj = (JSONObject) wpJsonOject.get("query");
        JSONObject pages = (JSONObject) queryObj.get("pages");
        String pageID = "";
        for (Object key : pages.keySet()) {
            pageID = (key.toString());
        }
        JSONObject pageIdObj = (JSONObject) pages.get(pageID);
        response.setDescription(pageIdObj.get("extract").toString());
    }

    /**
     * Extracts cover art for front an albums
     *
     * @param response - object contains albums to get cover art for and adds the link to the cover art to the albums
     */
    private void ExtractCoverArtArchiveAPI(WsResponse response) {
        for (Album a : response.getAlbums()) {
            Runnable extractCA = () -> {
                JSONObject caaJsonObject = GetJsonFromApi("http://coverartarchive.org/release-group/" + a.getId());
                if (caaJsonObject != null) {
                    JSONArray imagesArray = (JSONArray) caaJsonObject.get("images");
                    for (int i = 0; i < imagesArray.size(); i++) {
                        JSONObject imageJsonObject = (JSONObject) imagesArray.get(i);
                        if (imageJsonObject.get("front").toString().equals("true")) {
                            a.setImage(imageJsonObject.get("image").toString());
                        }
                    }
                }
            };
            executor.execute(extractCA);
        }
    }

    /**
     * Access external APIs
     *
     * @param apiUrl - http link to api to access
     * @return - returns an jsonObject with the response from the API
     */
    private JSONObject GetJsonFromApi(String apiUrl) {

        JSONObject jobj = null;
        String jsonStr = "";
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responsecode = conn.getResponseCode();
            if (responsecode != 200) {
                return null;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

            String currentStr;
            while ((currentStr = br.readLine()) != null) {
                if (currentStr != null) {
                    jsonStr = currentStr;
                }
            }
            conn.disconnect();

            JSONParser parse = new JSONParser();
            jobj = (JSONObject) parse.parse(jsonStr);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return jobj;
    }

}