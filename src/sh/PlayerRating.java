package sh;

/**
 * Created by zhengyu on 07/08/2017.
 */
public class PlayerRating {
    public int id;
    public int appearance;
    public float avgRating;

    public PlayerRating() {
        appearance = 0;
        avgRating = 0;
    }

    public boolean isValid() {
        return true;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(appearance).append(",");
        sb.append(avgRating).append(",");

        return sb.toString();
    }
}
