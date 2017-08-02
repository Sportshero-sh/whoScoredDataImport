package sh;

import java.util.ArrayList;

/**
 * Created by zhengyu on 26/07/2017.
 */
public class MatchStats {

    public int id;
    public int goal;
    public int conceded;
    public ArrayList<TeamSquad> squad = new ArrayList<>();


    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(goal).append(",");
        sb.append(conceded).append(",");

        // Out put the home team
        for (TeamSquad aSquad : squad) {
            sb.append(aSquad.toString());
        }

        if (squad.size() < 11) {
            System.out.println("Match with less than 11 player played: " + id);
        }

        return sb.toString();
    }
}
