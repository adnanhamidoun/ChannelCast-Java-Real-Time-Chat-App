package com.mycompany.socketchatV02;

import org.json.*;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class Apis {
    public String getClasificacionSpain(){
        String classificationString ="";
        try {
            String apiKey = "tu api"; // Aquí va tu API Key
            String urlString = "https://api.football-data.org/v4/competitions/PD/standings"; // URL de la API de LaLiga

            URL url = new URL(urlString);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET"); // Solicitud GET
            connection.setRequestProperty("X-Auth-Token", apiKey); // Autenticación con la API Key

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            classificationString = displayClassification(response.toString());



        } catch (Exception e) {
            e.printStackTrace();
        }

        return classificationString;
    }

    /**
     * Método que procesa el JSON y devuelve la clasificación de LaLiga como un String.
     * @param jsonResponse JSON de la API con los datos de la clasificación.
     * @return Un String con la clasificación de LaLiga.
     */
    private static String displayClassification(String jsonResponse) {
        StringBuilder result = new StringBuilder();

        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray standingsArray = jsonObject.getJSONArray("standings");

        JSONArray table = standingsArray.getJSONObject(0).getJSONArray("table");


        result.append("Clasificación de LaLiga:\n");
        String horizontalLine = "+------+----------------------+----+----+----+----+----+----+-----+-----+\n";
        result.append(horizontalLine);
        result.append(String.format("| %-4s | %-20s | %-2s | %-2s | %-2s | %-2s | %-2s | %-2s | %-3s | %-3s |\n",
                "Pos", "Equipo", "PJ", "V", "E", "D", "GF", "GC", "Dif", "Pts"));
        result.append(horizontalLine);


        for (int i = 0; i < table.length(); i++) {
            JSONObject team = table.getJSONObject(i);
            int position = team.getInt("position");
            String teamName = team.getJSONObject("team").getString("name");

            if (teamName.length() > 20) {
                teamName = teamName.substring(0, 17) + "...";
            }
            int playedGames = team.getInt("playedGames");
            int won = team.getInt("won");
            int drawn = team.getInt("draw");
            int lost = team.getInt("lost");
            int goalsFor = team.getInt("goalsFor");
            int goalsAgainst = team.getInt("goalsAgainst");
            int goalDifference = team.getInt("goalDifference");
            int points = team.getInt("points");

            String row = String.format("| %-4d | %-20s | %-2d | %-2d | %-2d | %-2d | %-2d | %-2d | %-3d | %-3d |\n",
                    position, teamName, playedGames, won, drawn, lost, goalsFor, goalsAgainst, goalDifference, points);
            result.append(row);
        }
        result.append(horizontalLine);

        return result.toString();
    }


    public String getCriptoMonedas() {
        String apiUrl = "https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=50&page=1";
        String cryptoList = "";
        try {
            String cryptoData = getCryptoData(apiUrl);


            cryptoList = displayCryptoList(cryptoData);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return cryptoList;

    }


    private String getCryptoData(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");


        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new Exception("Error en la solicitud: Código " + responseCode);
        }
    }

    private String displayCryptoList(String jsonResponse) {
        JSONArray jsonArray = new JSONArray(jsonResponse);

        StringBuilder result = new StringBuilder();
        result.append("Lista de las 50 principales criptomonedas:\n");

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject crypto = jsonArray.getJSONObject(i);


            String name = crypto.getString("name");
            String symbol = crypto.getString("symbol");
            double price = crypto.getDouble("current_price");


            result.append((i + 1) + ". " + name + " (" + symbol.toUpperCase() + ")\n");
            result.append("   Precio: $" + price + "\n");
            result.append("-------------------------------\n");
        }

        // Convertir el StringBuilder a String y devolverlo
        return result.toString();
    }


    public String getNoticias() {
        String apiKey = "pn tu api";
        String apiUrl = "https://gnews.io/api/v4/top-headlines?token=" + apiKey + "&lang=es";
        String newsData = "";
        try {
            newsData = getNewsData(apiUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsData;
    }

    private static String getNewsData(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // Código 200
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return formatNewsData(response.toString());
        } else {
            throw new Exception("Error en la solicitud: Código " + responseCode);
        }
    }

    private static String formatNewsData(String jsonResponse) {
        StringBuilder formattedNews = new StringBuilder();

        if (jsonResponse.contains("\"articles\":")) {
            String[] articles = jsonResponse.split("\"articles\":")[1].split("\\},\\{");
            for (String article : articles) {
                if (article.contains("\"title\":") && article.contains("\"content\":")) {
                    String title = article.split("\"title\":\"")[1].split("\",")[0];
                    String content = article.split("\"content\":\"")[1].split("\",")[0];
                    formattedNews.append("Título: ").append(title).append("\n");
                    formattedNews.append("Contenido: ").append(content).append("\n");
                    formattedNews.append("---------------------------\n");
                }
            }
        } else {
            formattedNews.append("No se encontraron artículos.");
        }

        return formattedNews.toString();
    }
}
