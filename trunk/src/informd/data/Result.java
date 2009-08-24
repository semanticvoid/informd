/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package informd.data;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author anand
 */
public class Result {

    public String title;
    public String snippet;
    public String url;
    public String date;
    public String time;

    public String getTimeInHours() {
        String timeInHours = "";
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss zzz");
            Date d = (Date) formatter.parse(date + " " + time + " GMT");
            Date cDate = new Date();

            double diff = (cDate.getTime() - d.getTime()) / (60 * 60 * 1000);
            int t = 0;
            String metric = "hours ago";
            if (diff < 1) {
                diff = (cDate.getTime() - d.getTime()) / (60 * 1000);
                t = (int) (diff);
                metric = "mins ago";
            } else {
                t = (int) diff;
                if (t == 1) {
                    metric = "hour ago";
                }
            }

            timeInHours = t + " " + metric;
        } catch(Exception e) {
            // return the blank time
        }

        return timeInHours;
    }
}
