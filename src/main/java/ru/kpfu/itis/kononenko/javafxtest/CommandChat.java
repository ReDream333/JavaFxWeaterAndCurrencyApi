package ru.kpfu.itis.kononenko.javafxtest;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class CommandChat extends Application {

    private TextArea chatArea;
    private TextField inputField;
    private final WeatherApi weatherApi= new WeatherApi();
    private final CurrencyApi currencyApi= new CurrencyApi();


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
                1) list - Просмотр всех команд.
                2) weather <город> - Узнать погоду в городе.
                3) exchange (или rate) <валюта> - Узнать перевод из валюты в RUB.
                4) quit - Выход. Закрыть окно.
                """;
        }
        else if (lowerCase.startsWith("weather")) {
            String[] parts = userInput.split(" ", 2);
            if (parts.length < 2) {
                return "Формат команды: weather <город>. Пример: weather Moscow";
            }
            String city = parts[1];
            return weatherApi.weatherInfo(city);
        }
        else if (lowerCase.startsWith("exchange") || lowerCase.startsWith("rate")) {
            String[] parts = userInput.split(" ", 2);
            if (parts.length < 2) {
                return "Формат команды: exchange <валюта>. Пример: exchange USD";
            }
            String currency = parts[1].toUpperCase();
            return currencyApi.currencyInfo(currency);
        }
        else {
            return "Не понимаю команду. Попробуйте 'list' для списка команд.";
        }
    }

}
