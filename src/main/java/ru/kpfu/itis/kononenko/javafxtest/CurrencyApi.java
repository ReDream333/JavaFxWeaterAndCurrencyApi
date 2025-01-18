package ru.kpfu.itis.kononenko.javafxtest;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CurrencyApi {
    private static final String API_KEY_CURRENCY = "02c622f203fe44bbb17a3c09fa686187";
    private static final String LINK = "https://openexchangerates.org/api/latest.json?app_id=%s&symbols=RUB,%s";

    public String currencyInfo(String currency){
        String urlStr = String.format(
                LINK,
                API_KEY_CURRENCY,
                currency
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
