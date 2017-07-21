package sh;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengyu on 11/07/2017.
 */
public class TeamSquad {
    public float rating;
    public int man_of_the_match;
    public int formation_place;
//    public int total_sub_on;
//    public int total_sub_off;
    public int totalPasses;
    public int passAccuracy;
    public int aerialsWon;
    public int touches;
    public int fouls;
    public int shots;
    public int dribblesWon;
    public int tackles;
    public int saves;
    public int assist;
    public int goal_penalty;
    public int goals;
    public int goal_own;
    public int penalty_missed;
    public int minutes_played;
    public int shots_blocked;
    public int was_dribbled;
    public int interceptions;
    public int was_fouled;
    public int offsides;
    public int dispossessed;
    public int turnovers;
    public int crosses;
    public int long_balls;
    public int through_balls;
    public int shotsOnTarget;
    public int yellow;
    public int red;
    public int secondYellow;
    public int penaltySave;
    public int error_lead_to_goal;
    public int last_man_tackle;
    public int clearance_off_line;
    public int hit_woodwork;

    public String toString() {
        Field[] fields = TeamSquad.class.getFields();
        StringBuffer sb = new StringBuffer();
        try {
            for (Field field: fields) {
                if (field.getGenericType().getTypeName().equals("float")) {
                    sb.append(field.getFloat(this)).append(",");
                } else if (field.getGenericType().getTypeName().equals("int")) {
                    sb.append(field.getInt(this)).append(",");
                }
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}
