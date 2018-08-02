package com.findstop.darren.findmystop;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GoogleDirectionsParser {


    public List<List<HashMap<String, String>>> parse(JSONObject object) {

        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String, String>>>();
        JSONObject obDistance = null;
        JSONObject obDuration = null;
        JSONArray arrRoutes = null;
        JSONArray arrLegs = null;
        JSONArray arrSteps = null;
        try {
            arrRoutes = object.getJSONArray("routes");
            /** Traversing all routes */
            for (int r = 0; r < arrRoutes.length(); r++) {
                arrLegs = ((JSONObject) arrRoutes.get(r)).getJSONArray("legs");
                List<HashMap<String, String>> road = new ArrayList<HashMap<String, String>>();
                /** Traversing all legs */
                for (int i = 0; i < arrLegs.length(); i++) {
                    /** Getting distance from the json data */
                    obDistance = ((JSONObject) arrLegs.get(i)).getJSONObject("distance");
                    HashMap<String, String> APIdistance = new HashMap<String, String>();
                    APIdistance.put("distance", obDistance.getString("text"));
                    /** Getting duration from the json data */
                    obDuration = ((JSONObject) arrLegs.get(i)).getJSONObject("duration");
                    HashMap<String, String> APIduration = new HashMap<String, String>();
                    APIduration.put("duration", obDuration.getString("text"));
                    /** Adding distance object to the path */
                    road.add(APIdistance);
                    /** Adding duration object to the path */
                    road.add(APIduration);
                    arrSteps = ((JSONObject) arrLegs.get(i)).getJSONArray("steps");
                    /** Traversing all steps */
                    for (int b = 0; b < arrSteps.length(); b++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) arrSteps.get(b)).get("polyline")).get("points");
                        List<LatLng> roadlist = decodePoly(polyline);

                        /** Traversing all points */
                        for (int t = 0; t < roadlist.size(); t++) {
                            HashMap<String, String> dir = new HashMap<String, String>();
                            dir.put("lat", Double.toString(((LatLng) roadlist.get(t)).latitude));
                            dir.put("lng", Double.toString(((LatLng) roadlist.get(t)).longitude));
                            road.add(dir);
                        }
                    }
                }
                routes.add(road);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
        }
        return routes;
    }

    /**
     * Used to decode polyline point
     * Taken from: jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}