package sh;

import java.util.ArrayList;

/**
 * 表示一场比赛的结果，其结果是相对于目标队伍而言。
 * 比如goal是表示这个队伍的进球数，与主客场关系无关。
 * mySquad表示目标队伍的表现，与主客场无关。
 *
 */
public class MatchStats {

    public int id;
    public int goal;
    public int conceded;
    public ArrayList<TeamSquad> mySquad = new ArrayList<>();
    public ArrayList<TeamSquad> againstSquad = new ArrayList<>();


    public boolean isValid() {
        return mySquad.size() == 11 && againstSquad.size() == 11;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(goal).append(",");
        sb.append(conceded).append(",");

        // Out put my team
        for (TeamSquad aSquad : mySquad) {
            sb.append(aSquad.toString());
        }

        // Out put against team
        for (TeamSquad aSquad : againstSquad) {
            sb.append(aSquad.toString());
        }

        return sb.toString();
    }
}
