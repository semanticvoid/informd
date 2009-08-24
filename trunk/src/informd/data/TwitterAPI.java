/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package informd.data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;

/**
 *
 * @author anand
 */
public class TwitterAPI {

    private static Twitter twit = null;
    private static TwitterAPI t = null;

    // singleton
    private TwitterAPI() {
        twit = new Twitter("informd_624", "informd1983");
    }

    public static TwitterAPI getSingleton() {
        if(t == null) {
            t = new TwitterAPI();
        }

        return t;
    }

    public HashSet<String> getTrendingTopics() {
        HashSet<String> topics = new HashSet<String>();
        
        try {
            Trends trends = twit.getCurrentTrends();
            Trend[] tArr = trends.getTrends();
            for(int i=0; i<tArr.length; i++) {
                Trend t = tArr[i];
                String query = t.getQuery();

                // ignore hashtags
                if(query.contains("#")) {
                    continue;
                }

                topics.add(query);
            }

//            List<Trends> dailyTrends = twit.getDailyTrends();
//            for(Trends dTrends : dailyTrends) {
//                tArr = trends.getTrends();
//                for(int i=0; i<tArr.length; i++) {
//                    Trend t = tArr[i];
//                    String query = t.getQuery();
//
//                    if(query.contains("#")) {
//                        continue;
//                    }
//
//                    topics.add(query);
//                }
//            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return topics;
    }
}
