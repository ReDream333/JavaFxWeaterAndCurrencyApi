package ru.kpfu.itis.kononenko.javafxtest;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CommandChat extends Application {

    private TextArea chatArea;
    private TextField inputField;

    private static final String API_KEY_WEATHER = "ec6ae61f58c44f36d3bd7d4f99c9993a";
    private static final String API_KEY_CURRENCY = "02c622f203fe44bbb17a3c09fa686187";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        root.setCenter(chatArea);

        inputField = new TextField();
        inputField.setPromptText("Enter your command here...");

        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> processInput());

        inputField.setOnAction(e -> sendButton.fire());

        HBox inputBox = new HBox(10, inputField, sendButton);
        inputBox.setPadding(new Insets(10));
        root.setBottom(inputBox);

        Scene scene = new Scene(root, 500, 400);
        primaryStage.setTitle("Chat-Bot");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void processInput() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) {
            return;
        }

        chatArea.appendText("Client: " + userInput + "\n");
        inputField.clear();

        String response = generateResponse(userInput);
        chatArea.appendText("Bot: " + response + "\n");

        if (userInput.toLowerCase().startsWith("quit")) {
             Stage stage = (Stage) chatArea.getScene().getWindow();
             stage.close();
        }
    }

    private String generateResponse(String userInput) {
        String lowerCase = userInput.toLowerCase().trim();

        if (lowerCase.startsWith("list")) {
            return """
                Доступные команды:
                1) list
                2) weather <город>
                3) exchange (или rate) <валюта>
                4) quit
                """;
        }
        else if (lowerCase.startsWith("weather")) {
            String[] parts = userInput.split("\\s+", 2);
            if (parts.length < 2) {
                return "Формат команды: weather <город>. Пример: weather Moscow";
            }
            String city = parts[1];
            return getWeatherInfo(city);
        }
        else if (lowerCase.startsWith("exchange") || lowerCase.startsWith("rate")) {
            String[] parts = userInput.split("\\s+", 2);
            if (parts.length < 2) {
                return "Формат команды: exchange <валюта>. Пример: exchange USD";
            }
            String currency = parts[1].toUpperCase();
            return getExchangeRate(currency);
        }
        else if (lowerCase.startsWith("quit")) {
            return "Выходим из чата... ";
        }
        else {
            return "Не понимаю команду. Попробуйте 'list' для списка команд.";
        }
    }


    private String getWeatherInfo(String city) {
        String urlStr = String.format(
                "http://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric&lang=ru",
                city, API_KEY_WEATHER
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

    private String getExchangeRate(String currency) {
        String urlStr = String.format(
                "https://openexchangerates.org/api/latest.json?app_id=%s&symbols=RUB,%s",
                API_KEY_CURRENCY, currency
        );

        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                JSONObject json = new org.json.JSONObject(sb.toString());
                JSONObject rates = json.getJSONObject("rates");

                if (!rates.has("RUB")) {
                    return "В ответе отсутствует курс RUB.";
                }
                if (!rates.has(currency)) {
                    return "Не удалось найти курс для %s".formatted(currency);
                }

                double rubRate = rates.getDouble("RUB");
                double curRate = rates.getDouble(currency);


                double result;
                if (currency.equalsIgnoreCase("USD")) {
                    result = rubRate;
                } else {
                    result = rubRate / curRate;
                }

                return String.format("1 %s = %.2f RUB", currency, result);

            } else {
                return "Ошибка код - %s".formatted(responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка - не удалось получить курс";
        }
    }

}
