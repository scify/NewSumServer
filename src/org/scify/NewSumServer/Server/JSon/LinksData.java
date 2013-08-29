package org.scify.NewSumServer.Server.JSon;

import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;

/**
 *
 * @author grv
 */

/**
 * Class contains ArrayList of LinkLabelData.
 * 
 * Used to easily manipulate the data
 * and easily transform between object and JSON format
 */
public class LinksData extends ArrayList <LinkLabelData> implements JSonizable{

    /**
     * Make new empty LinksData 
     * 
     * @return a empty instance of LinksData.
     */
    public LinksData(){ //you may want to fill it up using add
        
    }
    
    /**
     * Make new LinksData from JSON String
     * 
     * @param json input String containing the JSON code for the object
     * @throws JsonSyntaxException
     * @return a instance of LinksData corresponding to input format.
     */
    public LinksData(String json) throws JsonSyntaxException{
        super(JSon.json.fromJson(json, LinksData.class));
    }

    /**
     * Make an ArrayList of the links contained herein
     * 
     * @return an ArrayList of Strings containing the links.
     */
    public ArrayList<String> getLinks(){
        ArrayList<String> links=new ArrayList();
        for (LinkLabelData each : this){
            links.add(each.getLink());
        }
        return links;
    }
    
    /**
     * Make an ArrayList of the first #thisMany links contained herein
     * 
     * @param thisMany number of links from the beginning to include
     * @return an ArrayList of the first #thisMany Strings containing the links.
     */
    public ArrayList<String> getLinks(int thisMany){
        ArrayList<String> links=new ArrayList();
        for (LinkLabelData each : this){
            links.add(each.getLink());
            thisMany--;
            if(thisMany==0){
                break;
            }
        }
        return links;
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
     * Returns an instance of LinksData relevant to the JSON string
     * 
     * @param jsonstring the String in json format to be converted.
     * @throws JsonSyntaxException
     * @return instance of object corresponding to the JSON String.
     */
    public static LinksData unjsonize(String jsonstring) throws JsonSyntaxException {
        return JSon.json.fromJson(jsonstring, LinksData.class);
    }
    
    @Override
    public String toString() {
        String temp="";
        for(LinkLabelData each : this){
            temp+=each.toString()+"\n";
        }
        return "LinksData{" + temp +'}';
    }
    
}
