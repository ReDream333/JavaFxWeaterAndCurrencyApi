package ru.kpfu.itis.kononenko.javafxtest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WeatherApi {

    private static final String API_KEY_WEATHER = "ec6ae61f58c44f36d3bd7d4f99c9993a";
    private static final String LINK = "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=ru";

    public String weatherInfo(String city) {
        String urlStr = String.format(
                LINK,
                city,
                API_KEY_WEATHER
        );

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = new JSONObject(response.toString());

                JSONObject main = json.getJSONObject("main");
                double temp = main.getDouble("temp");

                JSONArray weatherArr = json.getJSONArray("weather");
                JSONObject weatherInfo = weatherArr.getJSONObject(0);
                String description = weatherInfo.getString("description");

                return String.format(
                        "Погода в %s: %.1f°C, %s",
                        city, temp, description
                );
            } else {
                return "Ошибка код - %s".formatted(responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Не удалось получить погоду для: %s".formatted(city);
        }
    }

}
