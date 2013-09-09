
package org.scify.NewSumServer.Server.Structures;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author gkioumis
 */


public class Summary extends LinkedList<Sentence> {
    
//    /**
//     * all the sources that the summary comes from
//     */
//    private String allSources;
    
    

    public Summary(LinkedList<Sentence> sentences) {
        
        super(sentences);
        
    }

    public Summary() {
        
        super();
    
    }
    /**
     * 
     * @return all different sources that this summary comes from
     */
    public HashSet<Source> getAllSources() {
        
        HashSet allSources = new HashSet<Source>();
        
        
        // TODO generate all sources. here or elsewhere?
        for (Sentence eachSentence : this) {
            
            allSources.add(new Source(eachSentence.getLinkToSource(), null, eachSentence.getSourceImageUrl()));
            
        }
        
        return allSources;
        
    }
    
    
    
}
