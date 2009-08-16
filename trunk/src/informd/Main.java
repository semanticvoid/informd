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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author anand
 */
public class Main {

    // datastructure to store trending topics
    // <topic, timestamp in seconds since epoch>
    HashMap<String, Long> trendingList;

    // Twitter API
    TwitterAPI twitter;

    // News agg source
    BOSSNewsAggregator newsAgg;

    // Summarizer
    SynmanticSummerizer dygest;

    // max summary length
    final int MAX_SUMMARY_LEN = 4;

    // max cluster size
    final int MAX_CLUSTER_SIZE = 4;


    public Main(String trendsFilePath) throws Exception {
        twitter = TwitterAPI.getSingleton();
        newsAgg = new BOSSNewsAggregator();
        trendingList = new HashMap<String, Long>();

        readTrendsFromFile(trendsFilePath);
    }

    /**
     * Function to read the saved daily trends from file
     * File format:
     *  <timestamp> <trending topic>
     * @param filePath
     */
    private void readTrendsFromFile(String filePath) {
        try {
            FileReader fstream = new FileReader(filePath);
            BufferedReader in = new BufferedReader(fstream);

            String line;
            while((line = in.readLine()) != null) {
                // skip comments if any
                if(line.startsWith("#") || line.startsWith("/")) {
                    continue;
                }

                String[] tokens = line.split("[ \t]");

                // ignore empty lines
                if(tokens.length > 1) {
                    trendingList.put(tokens[0], Long.parseLong(tokens[1]));
                }
            }
        } catch(Exception e) {
            // do nothing
            // just skip this part
        }
    }

    /**
     * Function to add current trends to list
     */
    public void addCurrentTrends() {
        HashSet<String> trendingTopics = twitter.getTrendingTopics();

        for(String topic : trendingTopics) {
            trendingList.put(topic, new Date().getTime());
        }
    }

    /**
     * Function to fetch top results for topic and
     * add the summary for it
     * @param topic the trending topic
     * @param num   the number of results required
     * @return  list of results
     */
    private ArrayList<Result> getTopStories(String topic, int num) {
        ArrayList<Result> results = newsAgg.getTopResults(topic, num);

        if(results != null) {
            for(Result result : results) {
                try {
                    dygest = new SynmanticSummerizer();
                    StringBuffer summary = new StringBuffer();
                    List<ScoredSentence> sentences = dygest.summerize(result.url);

                    int numSentences = 0;
                    for(ScoredSentence sentence : sentences) {
                        summary.append(sentence.getText());
                        summary.append(" ");
                        numSentences++;

                        if(numSentences == MAX_SUMMARY_LEN) {
                            break;
                        }
                    }

                    result.snippet = summary.toString();
                } catch(Exception e) {
                    // skip summarizing this result
                }

                System.gc();
                break;
            }
        }

        return results;
    }

    /**
     * Function to generate the header for the HTML page
     * @return  the header
     */
    private String generatePageHeader() {
        StringBuffer header = new StringBuffer();

        header.append("<center><table width=75% border=0><tr><td colspan=2>");
        header.append("<div id='header'>");
        header.append("<img width=200 height=70 src='informd_logo.png'>");
        header.append("</div>");
        header.append("</td></tr>");
        header.append("<tr><td colspan=2>&nbsp;</td></tr>");
        header.append("<tr><td colspan=2 align=right>auto-generated on "
                + new Date().toLocaleString() + " PDT</font></td></tr>");
        header.append("<tr><td colspan=2>&nbsp;</td></tr>");

        return header.toString();
    }

    /**
     * Function to generate the main content for the page
     * @return  the main content
     */
    private String generatePageContent() {
        StringBuffer content = new StringBuffer("<tr><td colspan=2><div style='margin-left: 2%' id='content'>");

        int storyNum = 0;
        for(String topic : trendingList.keySet()) {
            ArrayList<Result> topicCluster = getTopStories(topic, MAX_CLUSTER_SIZE);

            if(topicCluster == null) {
                continue;
            }

            StringBuffer story = new StringBuffer("<div id='story-" + storyNum + "'>");
            StringBuffer related = new StringBuffer("<p><b>Related:</b>&nbsp;&nbsp;");
            story.append("<p align='left'>");

            boolean isFirst = true;
            boolean isError = false;
            boolean hasRelated = false;
            for(Result result : topicCluster) {
                if(isFirst) {
                    isFirst = false;
                    story.append("<font size=5><a href='");
                    story.append(result.url);
                    story.append("'>");
                    story.append(result.title);
                    story.append("</a></font><br>");
                    story.append("<font size=2 color=green>");
                    story.append(result.getTimeInHours());
                    story.append("</font><br>");
                    story.append(result.snippet);
                    story.append("<ul>");

                    if(result.snippet == null) {
                        isError = true;
                        break;
                    }
                } else {
                    hasRelated = true;
                    related.append("<a href='");
                    related.append(result.url);
                    related.append("'>");
                    related.append(result.title);
                    related.append("</a>&nbsp;&nbsp;");
                }
            }

            if(hasRelated) {
                story.append(related);
            }
            story.append("</p>");
            story.append("</div>");

            if(!isError) {
                content.append(story);
                storyNum++;
            }
        }

        content.append("</div></td></tr></table></center>");

        return content.toString();
    }

    public static void main(String[] args) {
        try {
            Main m = new Main("");
            m.addCurrentTrends();

            System.out.println(m.generatePageHeader());
            System.out.println(m.generatePageContent());

        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
