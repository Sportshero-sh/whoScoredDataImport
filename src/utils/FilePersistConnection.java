package utils;

import ws.Match;
import ws.Player;
import ws.Team;

import java.io.*;

/**
 * Created by zhengyu on 10/07/2017.
 */
public class FilePersistConnection {

    public boolean isMatchExist(int id) {
        File file = new File("matches/" + id + ".txt");
        return file.exists();
    }


    public void persistMatch(int id, Match match, String responseString) {
        persistToFile(responseString, "matches/" + id + ".txt");
    }

    public String getMatch(int id) {
        return getFromFile("matches/" + id + ".txt");
    }


    public boolean isTeamExist(int id) {
        File file = new File("teams/" + id + ".txt");
        return file.exists();
    }


    public void persistTeam(int id, Team team, String responseString) {
        persistToFile(responseString, "teams/" + id + ".txt");
    }

    public String getTeam(int id) {
        return getFromFile("teams/" + id + ".txt");
    }


    public boolean isPlayerExist(int id) {
        File file = new File("players/" + id + ".txt");
        return file.exists();
    }


    public void persistPlayer(int id, Player player, String responseString) {
        persistToFile(responseString, "players/" + id + ".txt");
    }

    public String getPlayer(int id) {
        return getFromFile("players/" + id + ".txt");
    }


    public void persistMatchForPrediction(int matchId, String jsonContent) {
        persistToFile(jsonContent, "predictions/" + matchId + ".txt");
    }

    private void persistToFile(String responseString, String name) {
        try {
            FileOutputStream out = new FileOutputStream(name);
            PrintStream p = new PrintStream(out);
            p.print(responseString);
            p.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getFromFile(String name) {
        try {
            FileReader fileReader = new FileReader(name);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            StringBuffer sb = new StringBuffer();
            String line = bufferedReader.readLine();
            while (line != null) {
                sb.append(line).append('\n');
                line = bufferedReader.readLine();
            }

            return sb.toString();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
