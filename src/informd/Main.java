/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package informd;

import dygest.text.ScoredSentence;
import dygest.text.summerizer.SynmanticSummerizer;
import informd.data.Result;
import informd.data.Topic;
import informd.data.TwitterAPI;
import informd.news.BOSSNewsAggregator;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author anand
 */
public class Main {

    // datastructure to store trending topics
    // <topic, timestamp in seconds since epoch>
    HashMap<String, Topic> trendingList;
    // Twitter API
    TwitterAPI twitter;
    // News agg source
    BOSSNewsAggregator newsAgg;
    // Summarizer
    SynmanticSummerizer dygest;
    // trending topics path
    String topicsFileStorePath;
    // default score for a story
    final double DEFAULT_SCORE = 2.0;
    // max summary length
    final int MAX_SUMMARY_LEN = 4;
    // max cluster size
    final int MAX_CLUSTER_SIZE = 3;

    public Main(String trendsFilePath) throws Exception {
        twitter = TwitterAPI.getSingleton();
        newsAgg = new BOSSNewsAggregator();
        trendingList = new HashMap<String, Topic>();
        topicsFileStorePath = trendsFilePath;

        readTrendsFromFile(topicsFileStorePath);
    }

    /**
     * Function to read the saved daily trends from file
     * File format:
     *  <timestamp> <trending topic>
     * @param filePath
     */
    public void readTrendsFromFile(String filePath) {
        try {
            FileReader fstream = new FileReader(filePath);
            BufferedReader in = new BufferedReader(fstream);

            String line;
            while ((line = in.readLine()) != null) {
                // skip comments if any
                if (line.startsWith("#") || line.startsWith("/")) {
                    continue;
                }

                String[] tokens = line.split("[\t]+");

                // ignore empty lines
                if (tokens.length > 1) {
                    trendingList.put(tokens[0], new Topic(tokens[0], Double.parseDouble(tokens[1])));
                }
            }

            in.close();
            fstream.close();
        } catch (Exception e) {
            e.printStackTrace();
            // do nothing
            // just skip this part
        }
    }

    /**
     * Function to save the current trends 
     */
    public void saveTrendsToFile() {
        try {
            FileWriter fstream = new FileWriter(topicsFileStorePath);
            BufferedWriter out = new BufferedWriter(fstream);

            if (trendingList != null) {
                Iterator<String> itr = trendingList.keySet().iterator();
                while (itr.hasNext()) {
                    String topic = itr.next();
                    double score = trendingList.get(topic).getScore();
                    // half life of a topic (4 hrs)
                    score = Math.sqrt(score);

                    // skip topic is timestamp is less than some constant
                    if (score <= 1.1) {
                        continue;
                    }

                    out.write(topic + "\t" + score + "\n");
                }
            }

            out.close();
            fstream.close();
        } catch (Exception e) {
            e.printStackTrace();
            // do nothing
            // just skip this part
        }
    }

    /**
     * Function to add current trends to list
     */
    public void addCurrentTrends() {
        HashSet<String> trendingTopics = twitter.getTrendingTopics();

        if (trendingTopics != null) {
            for (String topic : trendingTopics) {
                // if topic already exists add default score to it
                // else assign default score
                if (trendingList.containsKey(topic)) {
                    Topic t = trendingList.get(topic);
                    t.setScore((t.getScore() + new Date().getTime()) * 1.342);
                    trendingList.put(topic, t);
                } else {
                    trendingList.put(topic, new Topic(topic, new Date().getTime()));
                }
            }
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

        if (results != null) {
            for (Result result : results) {
                try {
                    dygest = new SynmanticSummerizer();
                    StringBuffer summary = new StringBuffer();
                    List<ScoredSentence> sentences = dygest.summerize(result.url);

                    int numSentences = 0;
                    for (ScoredSentence sentence : sentences) {
                        summary.append(sentence.getText());
                        summary.append(" ");
                        numSentences++;

                        if (numSentences == MAX_SUMMARY_LEN) {
                            break;
                        }
                    }

                    result.snippet = summary.toString();
                } catch (Exception e) {
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
        header.append("<tr><td colspan=2 align=right><font size=2>auto-generated on " + new Date().toLocaleString() + " PDT</font></td></tr>");
        header.append("<tr><td colspan=2 align=center bgcolor=#ffc>this page is an autogenerated collection of realtime news stories with machine-generated summaries</td></tr>");
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

        // sort the topics by score
        ArrayList<Topic> sortedTrendingTopics = new ArrayList<Topic>();
        for (String topic : trendingList.keySet()) {
            sortedTrendingTopics.add(trendingList.get(topic));
        }
        Collections.sort(sortedTrendingTopics, new Comparator<Topic>() {

            public int compare(Topic obj1, Topic obj2) {
                if (obj1.getScore() > obj2.getScore()) {
                    return -1;
                } else if (obj1.getScore() < obj2.getScore()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        for (Topic topic : sortedTrendingTopics) {
            ArrayList<Result> topicCluster = getTopStories(topic.getValue(), MAX_CLUSTER_SIZE);

            if (topicCluster == null) {
                continue;
            }

            StringBuffer story = new StringBuffer("<div id='story-" + storyNum + "'>");
            StringBuffer related = new StringBuffer("<p><b>Related:</b>&nbsp;&nbsp;");
            story.append("<p align='left'>");

            boolean isFirst = true;
            boolean isError = false;
            boolean hasRelated = false;
            for (Result result : topicCluster) {
                if (isFirst) {
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

                    if (result.snippet == null) {
                        isError = true;
                        break;
                    }
                } else {
                    if(hasRelated) {
                        related.append("&nbsp;&nbsp;|&nbsp;&nbsp;");
                    }
                    related.append("<a href='");
                    related.append(result.url);
                    related.append("'>");
                    related.append(result.title);
                    related.append("</a>");
                    hasRelated = true;
                }
            }

            if (hasRelated) {
                story.append(related);
            }
            story.append("</p>");
            story.append("</div>");

            if (!isError) {
                content.append(story);
                storyNum++;
            }
        }

        content.append("</div></td></tr></table></center>");

        return content.toString();
    }

    public static void main(String[] args) {
        try {
            Main m = new Main("/Users/anand/trending_topics");
            m.addCurrentTrends();

            System.out.println(m.generatePageHeader());
            System.out.println(m.generatePageContent());

            m.saveTrendsToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
