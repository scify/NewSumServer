package org.scify.NewSumServer.Server.JSon;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author scify
 */


public class LanguageData extends ArrayList <String> implements JSonizable{

    /**
     * Make new LanguageData
     * 
     * @return a empty instance of LanguageData.
     */
    public LanguageData() {
    }

    /**
     * Make new LanguageData from JSON String
     * @param json input String containing the JSON code for the object.
     * @throws JsonSyntaxException.
     * @return a instance of LanguageData corresponding to input format.
     */
    public LanguageData(String json){
        super(JSon.json.fromJson(json, CategoriesData.class));
    }

 
    /**
     * Make new LanguageData from collection
     * 
     * @param c collection of Strings to be used. 
     * @return a instance of LanguageData from current input collection.
     */
    public LanguageData(Collection<? extends String> c) {
        super(c);
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
     * Returns an instance of LanguageData relevant to the JSON string
     * 
     * @param jsonstring the String in json format to be converted.
     * @throws JsonSyntaxException.
     * @return instance of object corresponding to the JSON String.
     */
    public static LanguageData unjsonize(String jsonstring) throws Exception {
        return JSon.json.fromJson(jsonstring, LanguageData.class);
    }
}
