import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.FilePersistConnection;
import utils.ResponseException;
import ws.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by zhengyu on 10/07/2017.
 */
public class WSFetcher implements Callable<Object>{

    public static final String WHO_SCORE_URL = "http://mapi01.whoscored.com/";
    public static final String WHO_SCORE_STAGE_URL = WHO_SCORE_URL + "Stages/Show/";
    public static final String WHO_SCORE_MATCH_URL = WHO_SCORE_URL + "Matches/Show/";
    public static final String WHO_SCORE_TEAM_URL = WHO_SCORE_URL + "Teams/Show/";
    public static final String WHO_SCORE_Player_URL = WHO_SCORE_URL + "Players/Show/";
    public static OkHttpClient mClient = new OkHttpClient();
    public static FilePersistConnection mConnection;

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
                .header("Cookie", Main.mCookie);
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

    public void fetchStage(int id) throws ResponseException, IOException {
        if (!mConnection.isStageExist(id)) {
            System.out.println("Begin fetch stage: " + WHO_SCORE_STAGE_URL + id);

            Request request = generateRequest()
                    .url(WHO_SCORE_STAGE_URL + id)
                    .get()
                    .build();

            Response response = null;

            try {
                response = mClient.newCall(request).execute();
                if (response.code() == 200) {
                    Gson gson = new Gson();

                    String responseString = response.body().string();

                    mConnection.persistStage(id, responseString);

                } else {
                    throw new ResponseException(response.code());
                }

            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.body().close();
                    response.close();
                }
            }
        }
    }

    public ArrayList<Integer> getMatchesFromStages() {
        ArrayList<String> stages = mConnection.getAllStages();
        Gson gson = new Gson();


        ArrayList<Integer> matchList = new ArrayList<>();
        for (String stageString : stages) {
            Stage stage = gson.fromJson(stageString, Stage.class);


            for (Fixture fixture : stage.fixtures) {
                if (!mConnection.isMatchExist(fixture.id) && !matchList.contains(fixture.id)) {
                    matchList.add(fixture.id);
                }
            }
        }

        return matchList;
    }


    public void fetchMatch(int id, boolean forceFetch)throws ResponseException, IOException {
        mMatchId = id;

        if (!mConnection.isMatchExist(id) || forceFetch) {
            System.out.println("Begin fetch match: " + WHO_SCORE_MATCH_URL + id);

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

            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.body().close();
                    response.close();
                }
            }
        }
    }

    public void fetchTeam(int id) throws ResponseException, IOException {

        if (!mConnection.isTeamExist(id)) {
            System.out.println("Begin fetch team: " + WHO_SCORE_TEAM_URL + id);

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

            } catch (IllegalStateException e) {
                e.printStackTrace();
            } finally {
                if (response != null) {
                    response.body().close();
                    response.close();
                }
            }
        }

    }

    public void fetchPlayer(int id) throws ResponseException, IOException {

        if (!mConnection.isPlayerExist(id))
        {
            System.out.println("Begin fetch player: " + WHO_SCORE_Player_URL + id);

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

            } catch (IllegalStateException e) {
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
