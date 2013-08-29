package org.scify.NewSumServer.Server.JSon;

/**
 *
 * @author grv
 */

/**
 * Class contains the basic data needed for the summary.
 * 
 */
public class SnippetData {
    private String summary;
    private String sourceUrl;
    private String sourceName;
    private String feedUrl;

    /**
     * Make new SnippetData from input
     * 
     * @param summary the String containing the summary snippet.
     * @param sourceUrl the String containing the url.
     * @param sourceName the name of the source - a label.
     * @param feedUrl the url of the rss feed.
     * @return a instance of SnippetData corresponding to input.
     */
    public SnippetData(String summary, String sourceUrl, String sourceName, String feedUrl) {
        this.summary = summary;
        this.sourceUrl = sourceUrl;
        this.sourceName = sourceName;
        this.feedUrl = feedUrl;
    }

    /**
     * Get the summary snippet
     * 
     * @return the summary snippet as a String.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Get the source url
     * 
     * @return the source url as a String.
     */
    public String getSourceUrl() {
        return sourceUrl;
    }

    /**
     * Get the label
     * 
     * @return sourceName - label as a String.
     */
    public String getSourceName() {
        return sourceName;
    }

    /**
     * Get the feed url
     * 
     * @return the feed url containing the rss feed as a String.
     */
    public String getFeedUrl() {
        return feedUrl;
    }
    
    
}
