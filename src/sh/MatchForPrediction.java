package sh;

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
    public TeamSquad[] homeSquad;
    public TeamSquad[] awaySquad;

    public String toString() {
        StringBuffer sb = new StringBuffer();

        // Out put the home team
        for (TeamSquad aHomeSquad : homeSquad) {
            sb.append(aHomeSquad.toString());
        }

        // Out put the away team
        for (TeamSquad aAwaySquad : awaySquad) {
            sb.append(aAwaySquad.toString());
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
