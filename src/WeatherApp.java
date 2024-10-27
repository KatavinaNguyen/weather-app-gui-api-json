import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

// Fetches weather data from the API to display in the GUI
public class WeatherApp {
    // Fetch weather data for a given location
    public static JSONObject getWeatherData(String locationName) {
        // Get coordinates from location name
        JSONArray locationData = getLocationData(locationName);

        // Extract latitude and longitude
        JSONObject location = (JSONObject) locationData.get(0);
        double latitude = (double) location.get("latitude");
        double longitude = (double) location.get("longitude");

        // Build the API request URL with location and unit parameters
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relativehumidity_2m,weathercode,windspeed_10m" +
                "&temperature_unit=fahrenheit" +
                "&windspeed_unit=mph" +
                "&timezone=America%2FLos_Angeles";

        try {
            // Connect to the API and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Check if connection was successful (status 200)
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // Read and store JSON response
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            // Parse JSON response
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(resultJson.toString());

            // Get hourly data and current hour index
            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // Extract weather details for the current hour
            double temperature = (double) ((JSONArray) hourly.get("temperature_2m")).get(index);
            String weatherCondition = convertWeatherCode((long) ((JSONArray) hourly.get("weathercode")).get(index));
            long humidity = (long) ((JSONArray) hourly.get("relativehumidity_2m")).get(index);
            double windspeed = (double) ((JSONArray) hourly.get("windspeed_10m")).get(index);

            // Build JSON object for frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windspeed);

            return weatherData;
        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Fetch geographic coordinates for a location name
    public static JSONArray getLocationData(String locationName) {
        // Replace spaces with + in location name
        locationName = locationName.replaceAll(" ", "+");

        // Build URL for geolocation API
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try{
            // Connect to API and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // Check if connection was successful
            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // Read and store JSON response
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            // Parse JSON and retrieve location data
            JSONParser parser = new JSONParser();
            JSONObject resultsJsonObj = (JSONObject) parser.parse(resultJson.toString());
            return (JSONArray) resultsJsonObj.get("results");

        } catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Make API connection with given URL
    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Find the index of the current hour in the time array
    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            if (timeList.get(i).equals(currentTime)) {
                return i;
            }
        }
        return 0;
    }

    // Get the current time formatted for API comparison
    private static String getCurrentTime(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        return currentDateTime.format(formatter);
    }

    // Convert weather code to descriptive condition
    private static String convertWeatherCode(long weathercode) {
        if (weathercode == 0L) return "Clear";
        if (weathercode >= 1L && weathercode <= 3L) return "Cloudy";
        if ((weathercode >= 51L && weathercode <= 67L) || (weathercode >= 80L && weathercode <= 99L)) return "Rain";
        if (weathercode >= 71L && weathercode <= 77L) return "Snow";
        return "Unknown";
    }
}
