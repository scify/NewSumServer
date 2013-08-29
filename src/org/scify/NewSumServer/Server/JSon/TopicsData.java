package org.scify.NewSumServer.Server.JSon;

import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;

/**
 *
 * @author grv
 */

/**
 * Class contains the topics.
 * 
 * Used to easily manipulate the data
 * and easily transform between object and JSON format
 */
public class TopicsData extends ArrayList <TopicData> implements JSonizable{

    /**
     * Make new empty TopicsData 
     * 
     * @return a empty instance of TopicData.
     */
    public TopicsData(){ //you may want to fill it up using add
        
    }
    /**
     * Make new TopicsData from JSON String
     * 
     * @param json input String containing the JSON code for the object
     * @throws JsonSyntaxException
     * @return a instance of TopicsData corresponding to input format.
     */
    public TopicsData(String json) throws JsonSyntaxException{
        super(JSon.json.fromJson(json, TopicsData.class));
    }
    
    /**
     * Get an ArrayList containing the topicIDs from all the topics
     * 
     * @return an ArrayList <String> containing all the topicID's
     */
    public ArrayList<String> getTopicIDs(){
        ArrayList<String> ids=new ArrayList();
        for(TopicData each : this){
            ids.add(each.getTopicID());
        }
        return ids;
    }

    /**
     * Returns a String of the JSON format of the current object
     * 
     * @return JSON format of object.
     */
    @Override
    public String jsonize() {
        return JSon.json.toJson(this);
    }

    /**
     * Returns an instance of TopicsData relevant to the JSON string
     * 
     * @param jsonstring the String in json format to be converted.
     * @throws JsonSyntaxException
     * @return instance of object corresponding to the JSON String.
     */
    public static TopicsData unjsonize(String jsonstring) throws Exception {
        return JSon.json.fromJson(jsonstring, TopicsData.class);
    }
    
    @Override
    public String toString() {
        String temp="";
        for(TopicData each : this){
            temp+=each.toString()+"\n";
        }
        return "TopicsData{" + temp +'}';
    }
    
}
