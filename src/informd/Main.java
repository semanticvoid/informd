/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package informd;

import dygest.text.ScoredSentence;
import dygest.text.summerizer.SynmanticSummerizer;
import informd.data.Result;
import informd.data.TwitterAPI;
import informd.news.BOSSNewsAggregator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author anand
 */
public class Main {

    public static void main(String[] args) {
        try {
            TwitterAPI twitter = TwitterAPI.getSingleton();
            BOSSNewsAggregator newsAgg = new BOSSNewsAggregator();
            SynmanticSummerizer s = new SynmanticSummerizer();

            // get trending topics
            HashSet<String> trendingTopics = twitter.getTrendingTopics();

            // get top results for trending topics
            ArrayList<Result> topResults = newsAgg.getNewsForTopics(trendingTopics);

            for (Result result : topResults) {
                List<ScoredSentence> sentences = s.summerize(result.url);
                StringBuffer summary = new StringBuffer();
                int count = 0;
                for(ScoredSentence sentence : sentences) {
                    summary.append(sentence.getText() + " ");
                    count++;
                    if(count > 4) {
                        break;
                    }
                }

                System.out.println(result.title);
                System.out.println(summary.toString());
                System.out.println("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
