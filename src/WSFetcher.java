import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.FilePersistConnection;
import utils.ResponseException;
import ws.Match;
import ws.Player;
import ws.Team;
import ws.TeamLiveStatistics;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhengyu on 10/07/2017.
 */
public class WSFetcher implements Callable<Object>{

    public static final String WHO_SCORE_URL = "http://mapi01.whoscored.com/";
    public static final String WHO_SCORE_MATCH_URL = WHO_SCORE_URL + "Matches/Show/";
    public static final String WHO_SCORE_TEAM_URL = WHO_SCORE_URL + "Teams/Show/";
    public static final String WHO_SCORE_Player_URL = WHO_SCORE_URL + "Players/Show/";
    public static OkHttpClient mClient = new OkHttpClient();
    public static FilePersistConnection mConnection;
    public static final String mCookie = "visid_incap_774904=VDkgIMWQRqa4Q8AS9fikVRJMQ1kAAAAAQUIPAAAAAABtSK/2y61D5asPaeP62CiP; __gads=ID=5e6785c7f8bc0179:T=1497582617:S=ALNI_MZ-m08pqUBHBtNOzmc7UV7S7ap8KA; _ga=GA1.2.1282970298.1497582615; permutive-session=%7B%22session_id%22%3A%228d150b63-2b10-4181-9ffb-99132be0b850%22%2C%22last_updated%22%3A%222017-07-07T08%3A28%3A34.542Z%22%7D; permutive-id=7846ce4b-dadc-4b87-8e44-7803e8a73db4; _psegs=%5B1920%2C1930%2C2126%2C1907%2C2441%2C2300%2C1956%5D; visid_incap_774908=oI41Z2UeTTCE+vZuYQuIug7CXFkAAAAAQ0IPAAAAAACAFWN9AdINauLjwlPSilu8fPHgHy/n7vmT; incap_ses_430_774908=rjaNHSRMnAYOuZdxUqv3BaOWcVkAAAAAoZgxQXFEI1AoJ68oRR12HA==";

    private int mMatchId;

    @Override
    public Object call() throws Exception {
        return null;
    }

    public WSFetcher() {
        mConnection = new FilePersistConnection();
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
    }

    public Request.Builder generateRequest() {
        return new Request.Builder()
                .header("Content-Type", "application/json; charset=UTF-8")
                .header("Accept-Language", "en-US;q=1, zh-Hans-US;q=0.9")
                .header("PersistConnection", "keep-alive")
                .header("User-Agent", "WSMobile/1.5.1 (iPhone; iOS 10.3.2; Scale/2.00)")
                .header("Cookie", mCookie);
    }

    public void updateCookie(Headers headers) {
        StringBuilder cookie = new StringBuilder();

        for (int i = 0; i < headers.size(); i ++) {
            String name = headers.name(i);
            if ("Set-Cookie".equals(name)) {
                cookie.append(headers.value(i)).append(";");
            }
        }

        //mCookie = cookie.toString();
    }

    public int getMatchId() {
        return mMatchId;
    }

    public void fetchMatch(int id, boolean forceFetch)throws ResponseException {
        System.out.println("Begin fetch match: " + WHO_SCORE_MATCH_URL + id);
        mMatchId = id;

        if (!mConnection.isMatchExist(id) || forceFetch) {
            Request request = generateRequest()
                    .url(WHO_SCORE_MATCH_URL + id)
                    .get()
                    .build();

            Response response = null;

            try {
                response = mClient.newCall(request).execute();
                if (response.code() == 200) {
                    Gson gson = new Gson();

                    String responseString = response.body().string();


                    Match match = gson.fromJson(responseString, Match.class);
                    mConnection.persistMatch(id, match, responseString);

                    fetchTeam(match.info.homeId);
                    fetchTeam(match.info.awayId);

                    if (match.liveMatch != null &&
                            match.liveMatch.liveStatistics != null) {
                        TeamLiveStatistics homeTeamLiveStats = match.liveMatch.liveStatistics.home;
                        if (homeTeamLiveStats != null && homeTeamLiveStats.players != null) {
                            for (int i = 0; i < homeTeamLiveStats.players.length; i ++) {
                                fetchPlayer(homeTeamLiveStats.players[i].id);
                            }
                        }
                    }

                    if (match.liveMatch != null &&
                            match.liveMatch.liveStatistics != null) {
                        TeamLiveStatistics awayTeamLiveStats = match.liveMatch.liveStatistics.away;
                        if (awayTeamLiveStats != null && awayTeamLiveStats.players != null) {
                            for (int i = 0; i < awayTeamLiveStats.players.length; i ++) {
                                fetchPlayer(awayTeamLiveStats.players[i].id);
                            }
                        }
                    }
                } else {
                    throw new ResponseException(response.code());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.body().close();
                    response.close();
                }
            }
        }
    }

    public void fetchTeam(int id) throws ResponseException {
        System.out.println("Begin fetch team: " + WHO_SCORE_TEAM_URL + id);

        if (!mConnection.isTeamExist(id)) {
            Request request = generateRequest()
                    .url(WHO_SCORE_TEAM_URL + id)
                    .get()
                    .build();

            Response response = null;

            try {
                response = mClient.newCall(request).execute();
                if (response.code() == 200) {
                    updateCookie(response.headers());

                    Gson gson = new Gson();

                    String responseString = response.body().string();

                    Team team = gson.fromJson(responseString, Team.class);

                    mConnection.persistTeam(id, team, responseString);
                } else {
                    throw new ResponseException(response.code());
                }

            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.body().close();
                    response.close();
                }
            }
        }

    }

    public void fetchPlayer(int id) throws ResponseException {
        System.out.println("Begin fetch player: " + WHO_SCORE_Player_URL + id);

        if (!mConnection.isPlayerExist(id))
        {
            Request request = generateRequest()
                    .url(WHO_SCORE_Player_URL + id)
                    .get()
                    .build();

            Response response = null;

            try {
                response = mClient.newCall(request).execute();
                if (response.code() == 200) {
                    updateCookie(response.headers());

                    Gson gson = new Gson();

                    String responseString = response.body().string();

                    Player player = gson.fromJson(responseString, Player.class);

                    mConnection.persistPlayer(id, player, responseString);
                } else {
                    throw new ResponseException(response.code());
                }

            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.body().close();
                    response.close();
                }
            }
        }
    }
}
