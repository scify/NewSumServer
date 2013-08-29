package org.scify.NewSumServer.Server.JSon;

import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;

/**
 *
 * @author grv
 */

/**
 * Class contains data of a summary.
 * 
 * Used to easily manipulate the data
 * and easily transform between object and JSON format
 */
public class SummaryData implements JSonizable{
    private ArrayList <SourceData> sources;
    private ArrayList <SnippetData> snippets;

    /**
     * Make new SummaryData from JSON String
     * 
     * @param json input String containing the JSON code for the object
     * @throws JsonSyntaxException
     * @return a instance of SummaryData corresponding to input format.
     */
    public SummaryData(String json) throws JsonSyntaxException{
        SummaryData temp=JSon.json.fromJson(json, SummaryData.class);
        this.sources=temp.sources;
        this.snippets=temp.snippets;
    }
       
    /**
     * Make new SummaryData from ArrayLists as input
     * 
     * @param sources ArrayList of sources 
     * @param snippets ArrayList of snippets - summaries
     * @return a instance of SummaryData corresponding to input format.
     */
    public SummaryData(ArrayList<SourceData> sources, ArrayList<SnippetData> snippets) {
        this.sources = sources;
        this.snippets = snippets;
    }

    /**
     * Get an ArrayList containing the SourceData objects
     * 
     * @return ArrayList containing the sources.
     */
    public ArrayList<SourceData> getSources() {
        return sources;
    }

    /**
     * Get an ArrayList containing the SnippetData objects
     * 
     * @return ArrayList containing the snippets.
     */
    public ArrayList<SnippetData> getSnippets() {
        return snippets;
    }
    
    /**
     * Get an ArrayList containing only the snippets of the summary
     * 
     * @return ArrayList of Strings containing the snippets of the summary
     */
    public ArrayList<String> getSummaries(){
        ArrayList<String> summaries=new ArrayList();
        for(SnippetData each : snippets){
            summaries.add(each.getSummary());
        }
        return summaries;
    }
//TODO stop using the computer
    //TODO hashmap <url - > image >
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
     * Returns an instance of SummaryData relevant to the JSON string
     * 
     * @param jsonstring the String in json format to be converted.
     * @throws JsonSyntaxException
     * @return instance of object corresponding to the JSON String.
     */
    public static SummaryData unjsonize(String jsonstring) throws JsonSyntaxException {
        return JSon.json.fromJson(jsonstring, SummaryData.class);
    }

}
