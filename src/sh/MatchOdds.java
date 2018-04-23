package sh;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by zhengyu on 2018/4/16.
 */
public class MatchOdds {
    public int id;
    public int home_id;
    public int away_id;
    public java.sql.Timestamp time;
    public int goal = -1;
    public int conceded = -1;

    public static final int MIN_OUTCOME_ODDS_NUMBER = 1;
    public static final int MIN_LAST_MEETING_NUMBER = 5;

    private Map<Integer, ArrayList<Odd>> oddsMap;
    private ArrayList<LastMeeting> lastMeetings;

    public MatchOdds() {
        oddsMap = new HashMap<>();
        lastMeetings = new ArrayList<>();
    }

    public void addOdds(int outcome, Odd odd) {
        if (!oddsMap.containsKey(outcome)) {
            ArrayList<Odd> oddList = new ArrayList<>();
            oddsMap.put(outcome, oddList);
        }
        oddsMap.get(outcome).add(odd);
    }

    public void addLastMeeting(LastMeeting last) {
        lastMeetings.add(last);
    }

    public boolean isValid() {
        if (oddsMap.size() < 3) {
            return false;
        }

        if (goal < 0 || conceded < 0) {
            return false;
        }

        if (lastMeetings.size() < MIN_LAST_MEETING_NUMBER) {
            return false;
        }

        Iterator<Integer> keys =  oddsMap.keySet().iterator();
        while (keys.hasNext()) {
            int key = keys.next();
            ArrayList<Odd> oddList = oddsMap.get(key);

            if (oddList.size() < MIN_OUTCOME_ODDS_NUMBER) {
                return false;
            }

            oddList.sort(new Comparator<Odd>() {
                @Override
                public int compare(Odd o1, Odd o2) {
                    return o1.time.compareTo(o2.time);
                }
            });

            for (int i = 0; i < MIN_OUTCOME_ODDS_NUMBER; i++) {
                Odd odd = oddList.get(i);
                if (odd.odds < 0) {
                    return false;
                }
            }
        }

        for (int i = 0; i < MIN_LAST_MEETING_NUMBER; i++) {
            LastMeeting last = lastMeetings.get(i);
            if (last.goal < 0 || last.conceded < 0) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();

        Iterator<Integer> keys =  oddsMap.keySet().iterator();
        while (keys.hasNext()) {
            int key = keys.next();
            ArrayList<Odd> oddList = oddsMap.get(key);

            for (int i = 0; i < MIN_OUTCOME_ODDS_NUMBER; i++) {
                sb.append(oddList.get(i).odds).append(",");
            }
        }

        for (int i = 0; i < MIN_LAST_MEETING_NUMBER; i++) {
            LastMeeting last = lastMeetings.get(i);
            sb.append(home_id == last.home_id ? 1 : 0).append(",");
            sb.append(last.goal).append(",");
            sb.append(last.conceded).append(",");
        }

        // Out put the result
        if (goal > conceded) {
            sb.append(1);
        } else if (goal < conceded) {
            sb.append(2);
        } else {
            sb.append(0);
        }
        sb.append("\n");

        return sb.toString();
    }
}
