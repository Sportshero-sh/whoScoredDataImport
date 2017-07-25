import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {


    public static void main(String[] args) {
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

                new WSMultiFetcher(1068829, 3000, 8);

                break;

            case "parser_ws_data":
                // Parser WS data
                WSDataParser parser = new WSDataParser();
                startAt = 1068829;
                for (int i = startAt; i > 1068829  ; i --) {
                    parser.parserMatch(i);
                }

                parser.close();
                break;

            case "create_prediction_match":
                // Create prediction match data
                PredictionDataCreator creator = new PredictionDataCreator();

                startAt = 1122333;
                int totalNumber = 0;
                for (int i = startAt; i > 1068829  ; i --) {
                    String fileName = "Sample";
//                    if (totalNumber % 50 == 0) {
//                        fileName = "Test";
//                    }

                    if (creator.createPredictionData(i, fileName)) {
                        totalNumber ++;
                    }
                }

                System.out.println("Total Number is: " + totalNumber);
                break;
        }
    }
}
