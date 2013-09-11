package org.scify.NewSumServer.Server.DataCollections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.scify.NewSumServer.Server.Storage.InsectFileIO;
import org.scify.NewSumServer.Server.Structures.Article;

/**
 *
 * @author scify
 */


public class Articles extends ArrayList <Article> implements java.io.Serializable{

    private static final String OBJECTNAME = "AllArticles";
    private static final String CATEGORY = "feeds";
    
    public Articles() {
        super();
    }

    public Articles(Collection<? extends Article> c) {
        super(c);
    }
    
    public void save() throws IOException{
        InsectFileIO.getInstance().saveObject(this, OBJECTNAME, CATEGORY);
    }
    
    public void save(String name,String category) throws IOException{
        InsectFileIO.getInstance().saveObject(this, name , category);
    }
    
    public static Articles load() throws IOException{
        return (Articles)InsectFileIO.getInstance().loadObject(OBJECTNAME, CATEGORY);
    }
    
    public static Articles load(String name,String category) throws IOException{
        return (Articles)InsectFileIO.getInstance().loadObject(name , category);
    }
    
}
