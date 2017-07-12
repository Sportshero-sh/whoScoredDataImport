public class Main {


    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            return;
        }

        switch (args[0]) {
            case "fetch_from_ws":
                // Fetch data from WS
                WSFetcher fetcher = new WSFetcher();

                int startAt = 1099097;
                for (int i = startAt; i > startAt - 3000  ; i --) {
                    fetcher.fetchMatch(i);
                }
                break;

            case "parser_ws_data":
                // Parser WS data
                WSDataParser parser = new WSDataParser();
                startAt = 1109659;
                for (int i = startAt; i > startAt - 5000  ; i --) {
                    parser.parserMatch(i);
                }

                parser.close();
                break;

            case "create_prediction_match":
                // Create prediction match data
                PredictionDataCreator creator = new PredictionDataCreator();

                startAt = 1122333;
                for (int i = startAt; i > startAt - 5000  ; i --) {
                    creator.createPredictionData(i);
                }
                break;
        }
    }
}
