package org.scify.NewSumServer.Server.Structures;

import org.scify.NewSumServer.Server.JSon.JSon;
import org.scify.NewSumServer.Server.JSon.JSonizable;

/**
 *
 * @author scify
 */


public class FeedSource implements java.io.Serializable {
    
    private String sFeedLink;
    private String sFeedLabel;
    private String sFeedLogoURL;

    public FeedSource(String sFeed, String sourceName, String sourceLogoUrl) {
    
        this.sFeedLink = sFeed;
        this.sFeedLabel = sourceName;
        this.sFeedLogoURL = sourceLogoUrl;
    
    }
    /**
     * 
     * @return The URL permalink that this source represents
     */
    public String getFeedLink() {
        return sFeedLink;
    }
    /**
     * 
     * @return The source Label, the name we have given to that source feed 
     */
    public String getFeedName() {
        return sFeedLabel;
    }
    /**
     * 
     * @return the URL link to the RSS provider, if any
     */
    public String getFeedLogoUrl() {
        return sFeedLogoURL;
    }
    
    public void setLink(String link) {
        this.sFeedLink = link;
    }

    public void setFeedName(String sourceName) {
        this.sFeedLabel = sourceName;
    }

    public void setFeedLogoUrl(String sourceLogoUrl) {
        this.sFeedLogoURL = sourceLogoUrl;
    }

    public String toJSON() {
        
        return JSon.jsonize(this, FeedSource.class);
        
    }
    
    @Override
    public String toString() {
        
        return this.sFeedLink + "\n" + this.sFeedLabel + "\n" + this.sFeedLogoURL;
        
    }

}
