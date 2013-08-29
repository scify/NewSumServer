package org.scify.NewSumServer.Server.JSon;

import java.util.Calendar;

/**
 *
 * @author grv
 */

/**
 * Class contains the data needed to specify a topic.
 * 
 */
public class TopicData{
        private String topicID;
        private String topicTitle;
        private Calendar date;
        private int sourcesNum;
        private String imageUrl;
    
    /**
     * Make new TopicData from input
     * 
     * @param topicID the String containing the ID that specifies the topic.
     * @param topicTitle the String that describes the topic.
     * @param date an instance of Calendar - the date the news occurred.
     * @param imageUrl a String, the url that contains an image to be used for the summary.
     * @param sourcesNum number of different sources that were used to obtain the summary.
     * @return a instance of LinksData corresponding to input format.
     */
    public TopicData(String topicID, String topicTitle, Calendar date, int sourcesNum, String imageUrl) {
        this.topicID = topicID;
        this.topicTitle = topicTitle;
        this.date = date;
        this.sourcesNum = sourcesNum;
        this.imageUrl = imageUrl;
    }

    /**
     * Get the topicID
     * 
     * @return String containing the id specifying a topic.
     */
    public String getTopicID() {
        return topicID;
    }

    /**
     * Get the topicTitle
     * 
     * @return String containing the title that describes a topic.
     */
    public String getTopicTitle() {
        return topicTitle;
    }

    /**
     * Get the Date
     * 
     * @return Calendar containing date the event happened.
     */
    public Calendar getDate() {
        return date;
    }

    /**
     * Get the Number of sources that were used
     * 
     * @return an int , the number of sources that were used to obtain the summary
     */
    public int getSourcesNum() {
        return sourcesNum;
    }

    /**
     * Get the image url
     * 
     * @return the image url
     */
    public String getImageUrl() {
        return imageUrl;
    }
        
}
