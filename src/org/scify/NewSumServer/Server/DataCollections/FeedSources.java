package org.scify.NewSumServer.Server.DataCollections;

import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import org.scify.NewSumServer.Server.JSon.JSon;
import org.scify.NewSumServer.Server.JSon.JSonizable;
import org.scify.NewSumServer.Server.Storage.InsectFileIO;
import org.scify.NewSumServer.Server.Structures.FeedSource;

/**
 *
 * @author scify
 */


public class FeedSources extends HashSet <FeedSource> implements java.io.Serializable, JSonizable{

    private static final String OBJECTNAME = "FeedSources";
    private static final String CATEGORY = "Generic";
    private static final String USER = "generic";
    
    public FeedSources() throws InsectFileIO.NotInitializedException{
        InsectFileIO data=InsectFileIO.getInstance();
        HashMap <String,String> temp=data.readSources(USER);
        for(String each : temp.keySet()){
            this.add(new FeedSource(each,temp.get(each)));
        }
    }
    
    public FeedSources(Collection<? extends FeedSource> c) {
        super(c);
    }
    
    public void addLogo(String link,String logo){
        for(FeedSource each: this){
            if(each.getFeedLink().equals(link)){
                each.setFeedLogoUrl(logo);
                return;
            }
        }
    }
    //todo json - read from file  - save
    public void save() throws IOException{
        InsectFileIO.getInstance().saveObject(this, OBJECTNAME, CATEGORY);
    }
    
    public void save(String name,String category) throws IOException{
        InsectFileIO.getInstance().saveObject(this, name , category);
    }
    
    public static FeedSources load() throws IOException{
        return (FeedSources)InsectFileIO.getInstance().loadObject(OBJECTNAME, CATEGORY);
    }
    
    public static FeedSources load(String name,String category) throws IOException{
        return (FeedSources)InsectFileIO.getInstance().loadObject(name, category);
    }

    @Override
    public String jsonize() {
        return JSon.json.toJson(this);
    }
    
}
