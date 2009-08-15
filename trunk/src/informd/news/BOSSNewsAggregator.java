/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package informd.news;

import informd.data.Result;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author anand
 */
public class BOSSNewsAggregator {

    private static final String URL_PREFIX = "http://boss.yahooapis.com/ysearch/news/v1/";
    private static final String KEY = "qpBYTfjV34HWf6xUMwEjWYveb6ioxgZdv21O0anUms9gcB3NFox9caeEuavV7BtPubKJNg--";

    private ArrayList<Result> getTopResults(String query, int num) {
        int resultCount = 0;
        ArrayList<Result> urls = new ArrayList<Result>();
        StringBuffer queryURL = new StringBuffer(URL_PREFIX);
        query = query.replaceAll(" ", "%20");
        queryURL.append(query);
        queryURL.append("?appid=" + KEY + "&age=12h&orderby=date");

        try {
            // get results
            URL url = new URL(queryURL.toString());
            URLConnection uconn = url.openConnection();
            BufferedReader rd = new BufferedReader(new InputStreamReader(uconn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();

            JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(sb.toString());

            JSONObject response = jsonObject.getJSONObject("ysearchresponse");
            JSONArray results = response.getJSONArray("resultset_news");

            while(!results.isEmpty() && resultCount < results.size()) {
                JSONObject result = results.getJSONObject(resultCount++);
                Result topUrl = new Result();
                topUrl.title = result.getString("title");
                // shittiest string replace - you should be hung for this
                topUrl.title = topUrl.title.replaceAll("<b>", "");
                topUrl.title = topUrl.title.replaceAll("</b>", "");
                topUrl.url = result.getString("url");
                topUrl.date = result.getString("date");
                topUrl.time = result.getString("time");
                urls.add(topUrl);

                if(resultCount == num) {
                    break;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        return urls;
    }

    public ArrayList<Result> getNewsForTopics(HashSet<String> topics) {
        ArrayList<Result> results = new ArrayList<Result>();

        for(String topic : topics) {
            System.out.println(topic);
            ArrayList<Result> topResults = getTopResults(topic, 3);
            if(topResults != null) {
                results.addAll(topResults);
            }
        }

        return results;
    }
}
