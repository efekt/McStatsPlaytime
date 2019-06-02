package it.efekt.mcstatsplaytime;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StatsGatherer {
    private List<JsonObject> stats = new ArrayList();
    private final String PLAYTIME_PROPERTY = "stat.playOneMinute";
    private final String RESULT_FILENAME = "playtime.txt";

    public boolean execute(){
        try {
            getFiles();
            sortByPlayTime();
            printResults();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void printResults(){
        int i = 1;
        MinecraftUUIDResolver resolver = new MinecraftUUIDResolver();
        String resultString = "";
        for (JsonObject stats : this.stats) {
            String uuid = stats.get("uuid").getAsString();
            long playtime = stats.get(PLAYTIME_PROPERTY).getAsLong() / 20; // This is in Minecraft ticks, to get it in seconds, divide by 20
            long hours = TimeUnit.SECONDS.toHours(playtime);
            long minutes = TimeUnit.SECONDS.toMinutes(playtime) - (hours * 60);
            String resultLine = i + ". " + resolver.getName(uuid) + ": " + hours + "h " + minutes + "m";
            resultString = resultString.concat(resultLine + "\n");
            System.out.println(resultLine);
            i++;
        }
        try {
            saveResultToFile(resultString);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveResultToFile(String resultString) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(RESULT_FILENAME)){
            out.println(resultString);
            System.out.println("Saved to " + RESULT_FILENAME);
        }
    }

    private void sortByPlayTime(){
        this.stats.sort(Comparator.comparing((JsonObject item) -> item.get(PLAYTIME_PROPERTY).getAsLong()).reversed());
    }

    private void getFiles() throws FileNotFoundException {
        System.out.println("Looking for files...");
        File folder = new File(".");
        File[] files = folder.listFiles();

        if (files.length == 0){
            System.out.println("Didn't find any files.");
            return;
        }

        for (int i = 0; i < files.length; i++){
            if (files[i].isFile() && files[i].getName().toLowerCase().endsWith(".json")){
                BufferedReader bufferedReader = new BufferedReader(new FileReader(files[i]));
                JsonObject jsonObject = new Gson().fromJson(bufferedReader, JsonObject.class);
                System.out.print("Checking validity of json file... ");
                if (isCorrectJsonFile(jsonObject)) {
                    System.out.print("verified.\n");
                    String fileName = files[i].getName();
                    jsonObject.addProperty("uuid", fileName.substring(0, fileName.length() - 5).replace("-", ""));
                    this.stats.add(jsonObject);
                } else {
                    System.out.print("not valid, omitting.\n");
                }
            }
        }

        System.out.println("Gathered " + this.stats.size() + " .json files");
    }

    private boolean isCorrectJsonFile(JsonObject jsonObject){
        return jsonObject.has(PLAYTIME_PROPERTY);
    }
}
