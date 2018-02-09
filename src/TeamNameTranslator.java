import com.google.gson.Gson;
import sh.NameTranslate;
import utils.FilePersistConnection;
import utils.SQLServerPersistConnection;

import java.util.ArrayList;

/**
 * Created by zhengyu on 23/01/2018.
 */
public class TeamNameTranslator {
    private FilePersistConnection mFileConnection;
    private SQLServerPersistConnection mDBConnection;

    public TeamNameTranslator() {
        mFileConnection = new FilePersistConnection();
        mDBConnection = new SQLServerPersistConnection("shc-dev.database.windows.net", "shcadmin", "1qaz@WSX3edc", "SHC-Dev");
    }

    public void startTranslate(String filePath) {
        Gson gson = new Gson();

        String responseString = mFileConnection.getRowText(filePath);
        NameTranslate[] mapping = gson.fromJson(responseString, NameTranslate[].class);
        ArrayList<NameTranslate> needDoubleCheck = new ArrayList<>();
        int inserted = 0;
        for (int i = 0; i < mapping.length; i++) {

            int teamId = mDBConnection.getTeamIdWithName(mapping[i].en);
            if (teamId >= 0) {
                String translation = mDBConnection.getTranslationWithTeamId(teamId);
                if (translation == null) {
                    System.out.println("Set " + mapping[i].en + " to " + mapping[i].zh);
                    mDBConnection.setTranslationWithTeamId(teamId, mapping[i].zh);
                    inserted ++;
                } else if (!translation.equals(mapping[i].zh)) {
                    needDoubleCheck.add(mapping[i]);
                }
            } else {
                needDoubleCheck.add(mapping[i]);
            }
        }

        System.out.println(inserted + " inserted.");
        for (NameTranslate item: needDoubleCheck) {
            System.out.println("Need to double check: " + item.en + " with zh: " + item.zh);

        }
    }
}

