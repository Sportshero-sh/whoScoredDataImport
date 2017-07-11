public class Main {


    public static void main(String[] args) {
        // Fetch data from WS
//        WSFetcher fetcher = new WSFetcher();
//
//        int startAt = 1109659;
//        for (int i = startAt; i > startAt - 1000  ; i --) {
//            fetcher.parserMatch(i);
//        }

        // Parser WS data
        WSDataParser parser = new WSDataParser();
        parser.parserMatch(1122333);
        parser.close();
    }
}
