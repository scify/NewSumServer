
package org.scify.NewSumServer.Server.Structures;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.scify.NewSumServer.Server.JSon.JSon;

/**
 *
 * @author gkioumis
 */


public class Summary extends LinkedList<Sentence> {
    
    /**
     * all the sources that the summary comes from
     */
    private HashSet<String> allSources;
    
    

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
    public HashSet<String> getAllSources() {
        // TODO solve this. How can we design it so that all sources are inside, 
        // even the ones ommited from the summariser.
        
        String klein = "", mein;
        // generate all sources. here or elsewhere?
        for (Sentence eachSentence : this) {
            allSources.add(eachSentence.getSource());
        }
        return allSources;
        // CAUTION. THIS is saved now, but when the summary will pass the redundancy remover, 
        // some sources may be omitted.
        // TODO in summariser, new Summary should be called, and stored to file.
    }
    
    
    public String toJSON() {
        
        return JSon.jsonize(this, Summary.class);
        
    }
    
    
    
}
