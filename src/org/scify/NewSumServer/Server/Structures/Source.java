package org.scify.NewSumServer.Server.Structures;

import org.scify.NewSumServer.Server.JSon.JSon;
import org.scify.NewSumServer.Server.JSon.JSonizable;

/**
 *
 * @author scify
 */


public class Source implements java.io.Serializable, JSonizable {
    
    private String link;
    private String sourceName;
    private String sourceLogoUrl;

    public Source(String link, String sourceName, String sourceLogoUrl) {
    
        this.link = link;
        this.sourceName = sourceName;
        this.sourceLogoUrl = sourceLogoUrl;
    
    }
    
    public String getLink() {
        return link;
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceLogoUrl() {
        return sourceLogoUrl;
    }
    
    public void setLink(String link) {
        this.link = link;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setSourceLogoUrl(String sourceLogoUrl) {
        this.sourceLogoUrl = sourceLogoUrl;
    }

    @Override
    public String jsonize() {
        return JSon.json.toJson(this);
    }

}
