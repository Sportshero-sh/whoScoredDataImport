import utils.ResponseException;

import java.io.IOException;

public class Main {


    public static final String mCookie = "visid_incap_774904=VDkgIMWQRqa4Q8AS9fikVRJMQ1kAAAAAQUIPAAAAAABtSK/2y61D5asPaeP62CiP; __gads=ID=5e6785c7f8bc0179:T=1497582617:S=ALNI_MZ-m08pqUBHBtNOzmc7UV7S7ap8KA; incap_ses_500_774908=5WX5MCzAuB5d3/71xFvwBub2dlkAAAAAk9ekOjrvmq8avRiXnO79kA==; visid_incap_774908=oI41Z2UeTTCE+vZuYQuIug7CXFkAAAAAQ0IPAAAAAACAFWN9AdINauLjwlPSilu8fPHgHy/n7vmT; incap_ses_431_774904=utlITDVzD2jXGeV8tzj7BfX7flkAAAAAxMZu7OVH/VlAWUheEMbuPQ==; _ga=GA1.2.1282970298.1497582615; _gid=GA1.2.1047059441.1501494266; permutive-session=%7B%22session_id%22%3A%22faa3c597-235e-4b13-9714-b1ef0e65070d%22%2C%22last_updated%22%3A%222017-07-31T10%3A32%3A41.912Z%22%7D; permutive-id=7846ce4b-dadc-4b87-8e44-7803e8a73db4; _psegs=%5B1920%2C1930%2C2126%2C1907%2C2441%2C2300%2C1956%5D; incap_ses_430_774908=nbrlNEcNb1QHv8LHVKv3Bfzgf1kAAAAAgMwUphNZj2wrynZUObMIjg==; ___utmvmRluDSiS=GiaQJZpWhkP; ___utmvbRluDSiS=eZG XXoOealf: Cto";

    public static void main(String[] args) throws IOException, ResponseException {
        if (args == null || args.length == 0) {
            return;
        }

        // All start from 1122333
        switch (args[0]) {
            case "fetch_from_ws":
                // Fetch data from WS
//                WSFetcher fetcher = new WSFetcher();
//
                int startAt = 1075829;
//                for (int i = startAt; i > startAt - 3000  ; i --) {
//                    fetcher.fetchMatch(i);
//                }

                new WSMultiFetcher(1053829, 3000, 8);

                break;

            case "fetch_from_ws_with_fixture":
                WSFetcher fetcher = new WSFetcher();
//                for (int i = 7228; i <= 7228; i ++) {
//                    fetcher.fetchStage(i);
//                }
//                int[] stageList = {15151 , 15617, 15404 ,15375, 15243, 15177,15619};
//                for(Integer i : stageList){
//                    fetcher.fetchStage(i);
//                }
                new WSMultiFetcher(fetcher.getMatchesFromStages(), 16);
                break;

            case "parser_ws_data":
                // Parser WS data
                WSDataParser parser = new WSDataParser();
                startAt = 1068829;
                for (int i = startAt; i > 1053829  ; i --) {
                    parser.parserMatch(i, -1);
                }

                parser.close();
                break;

            case "parser_ws_data_by_stage":
                parser = new WSDataParser();
                parser.parseStages();
//                parser.updateMatchWithStage();
                break;

            case "create_prediction_match":
                // Create prediction match data
//                WhoScoredPredictionDataCreator creator = new WhoScoredPredictionDataCreator();
//                int totalNumber = creator.createPredictionDataByStage("full");

                SHPredictionDataCreator creator = new SHPredictionDataCreator();
                int amount = creator.createPredictionData("sh_full", 10000);

                System.out.println("Total Number is: " + amount);
                break;

            case "separate_sample_test":
                creator = new SHPredictionDataCreator();
                creator.separateSampleTest("predictions/sh_full.txt","predictions/test.csv", "predictions/training.csv", 0.1f, 19);
                break;

            case "team_name_translate":
                TeamNameTranslator translator = new TeamNameTranslator();
                translator.startTranslate("teamNames/Korea.txt");
                break;

            default:


                break;
        }
    }
}
