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

    public Topic(String value, double score) {
        this.value = value;
        this.score = score;
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

    
}
