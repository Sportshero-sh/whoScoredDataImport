package sh;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by zhengyu on 2018/4/16.
 */
public class MatchOdds {
    public int id;
    public int goal;
    public int conceded;

    public static final int MIN_OUTCOME_ODDS_NUMBER = 1;

    private Map<Integer, ArrayList<Odd>> oddsMap;

    public MatchOdds() {
        oddsMap = new HashMap<>();
    }

    public void addOdds(int outcome, Odd odd) {
        if (!oddsMap.containsKey(outcome)) {
            ArrayList<Odd> oddList = new ArrayList<>();
            oddsMap.put(outcome, oddList);
        }
        oddsMap.get(outcome).add(odd);
    }

    public boolean isValid() {
        if (oddsMap.size() < 3) {
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
