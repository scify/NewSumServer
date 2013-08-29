package org.scify.NewSumServer.Server.JSon;

/**
 *
 * @author grv
 */

/**
 * Class contains the link-url and the sourceName which is basically a label
 * for the link-url
 */
public class LinkLabelData{
        private String link;
        private String sourceName;
        private String sourceLogoUrl;

    /**
     * Make new LinkLabelData from input link and label
     * 
     * @return a new instance of LinkLabelData with the data.
     */
    public LinkLabelData(String link, String sourceName, String sourceLogoUrl) {
        this.link = link;
        this.sourceName = sourceName;
        this.sourceLogoUrl=sourceLogoUrl;
    }
       
    /**
     * Get the link
     * 
     * @return the link which is a url. A url is the text you click on at the top of the browser
     * or youtube when you copy paste videos.
     */
    public String getLink() {
        return link;
    }

    /**
     * Get the sourceName
     * 
     * @return the label corresponding to the link usually some kind of name.
     */
    public String getSourceName() {
        return sourceName;
    }

    @Override
    public String toString() {
        return "LinkLabelData{" + "link=" + link + ", sourceName=" + sourceName + '}';
    }
    }