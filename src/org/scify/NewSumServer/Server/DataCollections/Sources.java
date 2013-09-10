package org.scify.NewSumServer.Server.DataCollections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.scify.NewSumServer.Server.Storage.InsectFileIO;
import org.scify.NewSumServer.Server.Structures.FeedSource;

/**
 *
 * @author scify
 */


public class Sources extends ArrayList <FeedSource> implements java.io.Serializable{

    private static final String OBJECTNAME = "Sources";
    private static final String CATEGORY = "Generic";
    
    public Sources() {
    }

    public Sources(Collection<? extends FeedSource> c) {
        super(c);
    }
    
    public void save() throws IOException{
        InsectFileIO.getInstance().saveObject(this, OBJECTNAME, CATEGORY);
    }
    
    public void save(String name,String category) throws IOException{
        InsectFileIO.getInstance().saveObject(this, name , category);
    }
    
    public void load() throws IOException{
        InsectFileIO.getInstance().loadObject(OBJECTNAME, CATEGORY);
    }
    
    public void load(String name,String category) throws IOException{
        InsectFileIO.getInstance().loadObject(name , category);
    }
}
