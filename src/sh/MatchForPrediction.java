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
    public ArrayList<MatchStats> homeMatchStats;
    public ArrayList<MatchStats> awayMatchStats;

    public String toString() {
        StringBuffer sb = new StringBuffer();

        // Out put the home team
        for (MatchStats aHomeMatchStats : homeMatchStats) {
            sb.append(aHomeMatchStats.toString());
        }

        // Out put the away team
        for (MatchStats aWayMatchStats : awayMatchStats) {
            sb.append(aWayMatchStats.toString());
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
