package sh;

import java.util.ArrayList;

/**
 * Created by zhengyu on 11/07/2017.
 */
public class MatchForPrediction {
    public int homeId;
    public int awayId;
    public String homeName;
    public String awayName;
    public int homeScore;
    public int awayScore;
    public ArrayList<MatchStats> lastMeetingsStats;
    public ArrayList<MatchStats> homeMatchStats;
    public ArrayList<MatchStats> awayMatchStats;

    public static final int MAX_TEAM_PLAYER_NUMBER = 18;
    public static final int MIN_TEAM_PLAYER_NUMBER = 18;
    public ArrayList<PlayerRating> homePlayerRating;
    public ArrayList<PlayerRating> awayPlayerRating;

    public boolean isValid() {
        if (homePlayerRating.size() < MIN_TEAM_PLAYER_NUMBER || awayPlayerRating.size() < MIN_TEAM_PLAYER_NUMBER) {
            System.out.println("Not enough player rating.");
            return false;
        }

        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        // Out put the home team
        for (int i = 0; i < MAX_TEAM_PLAYER_NUMBER; i ++) {
            if (homePlayerRating.size() > i) {
                PlayerRating aRating = homePlayerRating.get(i);
                sb.append(aRating.toString());
            } else {
                PlayerRating aRating = new PlayerRating();
                sb.append(aRating.toString());
            }

        }

        // Out put the away team
        for (int i = 0; i < MAX_TEAM_PLAYER_NUMBER; i ++) {
            if (awayPlayerRating.size() > i) {
                PlayerRating aRating = awayPlayerRating.get(i);
                sb.append(aRating.toString());
            } else {
                PlayerRating aRating = new PlayerRating();
                sb.append(aRating.toString());
            }
        }

        // Out put the result
        if (homeScore > awayScore) {
            sb.append(1);
        } else if (homeScore < awayScore) {
            sb.append(2);
        } else {
            sb.append(0);
        }
        sb.append("\n");

        return sb.toString();
    }
}
