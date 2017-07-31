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


    public void persistPredictionMatch(String fileName, String jsonContent) {
        writeToFile(jsonContent, "predictions/" + fileName + ".txt", true);
    }

    public void separateToFiles(String dest, String target1, String target2, float target1Percentage) {
        try {
            FileReader fileReader = new FileReader(dest);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();
            int target1Num = 0;
            int target2Num = 0;
            while (line != null) {

                if (Math.random() < target1Percentage) {
                    writeToFile(line, target1, true);
                    writeToFile("\n", target1, true);
                    target1Num ++;
                } else {
                    writeToFile(line, target2, true);
                    writeToFile("\n", target2, true);
                    target2Num ++;
                }

                line = bufferedReader.readLine();
            }

            System.out.println(target1 + " has " + target1Num);
            System.out.println(target2 + " has " + target2Num);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void test() {
        try {
            FileReader fileReader = new FileReader("predictions/full_rating.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String line = bufferedReader.readLine();
            int right = 0;
            int wrong = 0;
            while (line != null) {


            }

            System.out.println();
            System.out.println();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void persistToFile(String responseString, String name) {
        writeToFile(responseString, name, false);
    }

    private void writeToFile(String responseString, String name, boolean append) {
        try {
            FileOutputStream out = new FileOutputStream(name, append);
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
