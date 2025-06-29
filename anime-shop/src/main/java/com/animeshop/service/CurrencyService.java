package com.animeshop.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private final OkHttpClient client = new OkHttpClient();

    // Метод для получения курса без пересчёта
    public double getRate(String currencyCode) {
        try {
            Request request = new Request.Builder()
                    .url("https://api.exchangerate-api.com/v4/latest/RUB")
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) return -1;

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONObject("rates").getDouble(currencyCode.toUpperCase());

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Метод с пересчётом суммы
    public double convertRUBto(String currencyCode, double amountRUB) {
        double rate = getRate(currencyCode);
        return rate > 0 ? amountRUB * rate : -1;
    }
}
