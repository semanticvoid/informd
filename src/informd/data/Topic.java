/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package informd.data;

/**
 *
 * @author anand
 */
public class Topic {

    private String value;
    private double score;
    private int rank;

    public Topic(String value, double score) {
        this.value = value;
        this.score = score;
        this.rank = 9999;
    }

    public Topic(String value, double score, int rank) {
        this.value = value;
        this.score = score;
        this.rank = rank;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return the score
     */
    public double getScore() {
        return score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * @return the rank
     */
    public int getRank() {
        return rank;
    }

    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }

    
}
