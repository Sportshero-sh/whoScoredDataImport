import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import ws.Match;
import ws.Player;
import ws.Team;
import ws.TeamLiveStatistics;

import java.io.IOException;

/**
 * Created by zhengyu on 10/07/2017.
 */
public class WSFetcher {

    public static final String WHO_SCORE_URL = "http://mapi01.whoscored.com/";
    public static final String WHO_SCORE_MATCH_URL = WHO_SCORE_URL + "Matches/Show/";
    public static final String WHO_SCORE_TEAM_URL = WHO_SCORE_URL + "Teams/Show/";
    public static final String WHO_SCORE_Player_URL = WHO_SCORE_URL + "Players/Show/";
    public static OkHttpClient mClient = new OkHttpClient();
    public static FilePersistConnection mConnection;
    public static final String mCookie = "visid_incap_774904=VDkgIMWQRqa4Q8AS9fikVRJMQ1kAAAAAQUIPAAAAAABtSK/2y61D5asPaeP62CiP; __gads=ID=5e6785c7f8bc0179:T=1497582617:S=ALNI_MZ-m08pqUBHBtNOzmc7UV7S7ap8KA; incap_ses_571_774904=pVVwSvdQCA8GHGC1wZnsBytGX1kAAAAA8PlXraH5h5JdiEbM4U++8g==; _ga=GA1.2.1282970298.1497582615; permutive-session=%7B%22session_id%22%3A%228d150b63-2b10-4181-9ffb-99132be0b850%22%2C%22last_updated%22%3A%222017-07-07T08%3A28%3A34.542Z%22%7D; permutive-id=7846ce4b-dadc-4b87-8e44-7803e8a73db4; _psegs=%5B1920%2C1930%2C2126%2C1907%2C2441%2C2300%2C1956%5D; incap_ses_500_774904=zgOuCyzQaQELNXxHwFvwBpFMX1kAAAAATnuieNdKtFkVztXb8s5VAA==; incap_ses_500_774908=uxBpQ3bpkCT/KZFHwFvwBiVdX1kAAAAAFvdOocqzmCDUiGmEk/0HfQ==; incap_ses_570_774908=/8a+TglGTlfegpEOSgzpBy0dY1kAAAAAU0UDN1w6ywst758bFFdAdA==; ___utmvc=navigator%3Dtrue,navigator.vendor%3DGoogle%20Inc.,navigator.appName%3DNetscape,navigator.plugins.length%3D%3D0%3Dfalse,navigator.platform%3DMacIntel,navigator.webdriver%3Dundefined,plugin_ext%3Dplugin,plugin_ext%3Dno%20extention,ActiveXObject%3Dfalse,webkitURL%3Dtrue,_phantom%3Dfalse,callPhantom%3Dfalse,chrome%3Dtrue,yandex%3Dfalse,opera%3Dfalse,opr%3Dfalse,safari%3Dfalse,awesomium%3Dfalse,puffinDevice%3Dfalse,__nightmare%3Dfalse,_Selenium_IDE_Recorder%3Dfalse,document.__webdriver_script_fn%3Dfalse,document.%24cdc_asdjflasutopfhvcZLmcfl_%3Dfalse,process.version%3Dfalse,navigator.cpuClass%3Dfalse,navigator.oscpu%3Dfalse,navigator.connection%3Dfalse,window.outerWidth%3D%3D0%3Dfalse,window.outerHeight%3D%3D0%3Dfalse,window.WebGLRenderingContext%3Dtrue,document.documentMode%3Dundefined,eval.toString().length%3D33,digest=81039,81455,81302,81110,81326,s=a0ab7974917c6da6ad77677c827c9a84736299a1936aaa73a6a56b847b699b87a068a3a193757272; visid_incap_774908=oI41Z2UeTTCE+vZuYQuIug7CXFkAAAAAQkIPAAAAAACAQk19AdINauKzDINK7ddwuSNxuhkC5gRE; incap_ses_430_774908=Ey6gVkJdRTbtep3LTav3Bf44Y1kAAAAAFVPb8jyWKvrlGfB7jnysgg==";

    public WSFetcher() {
        mConnection = new FilePersistConnection();
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

    public void fetchMatch(int id) {
        System.out.println("Begin fetch match: " + WHO_SCORE_MATCH_URL + id);
        Request request = generateRequest()
                .url(WHO_SCORE_MATCH_URL + id)
                .get()
                .build();

        try {
            Response response = mClient.newCall(request).execute();
            if (response.code() == 200) {
                Gson gson = new Gson();

                String responseString = response.body().string();
                response.body().close();
                response.close();

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
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fetchTeam(int id) {
        System.out.println("Begin fetch team: " + WHO_SCORE_TEAM_URL + id);

        if (!mConnection.isTeamExist(id)) {
            Request request = generateRequest()
                    .url(WHO_SCORE_TEAM_URL + id)
                    .get()
                    .build();

            try {
                Response response = mClient.newCall(request).execute();
                if (response.code() == 200) {
                    updateCookie(response.headers());

                    Gson gson = new Gson();

                    String responseString = response.body().string();
                    response.body().close();
                    response.close();

                    Team team = gson.fromJson(responseString, Team.class);

                    mConnection.persistTeam(id, team, responseString);
                }
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }

    }

    public void fetchPlayer(int id) {
        System.out.println("Begin fetch player: " + WHO_SCORE_Player_URL + id);

        if (!mConnection.isPlayerExist(id))
        {
            Request request = generateRequest()
                    .url(WHO_SCORE_Player_URL + id)
                    .get()
                    .build();

            try {
                Response response = mClient.newCall(request).execute();
                if (response.code() == 200) {
                    updateCookie(response.headers());

                    Gson gson = new Gson();

                    String responseString = response.body().string();
                    response.body().close();
                    response.close();

                    Player player = gson.fromJson(responseString, Player.class);

                    mConnection.persistPlayer(id, player, responseString);
                }
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }
}
