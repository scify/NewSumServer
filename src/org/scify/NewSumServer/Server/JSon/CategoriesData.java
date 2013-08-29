package org.scify.NewSumServer.Server.JSon;

import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author grv
 */


/**
 * Class contains categories as strings
 */
public class CategoriesData extends ArrayList <String> implements JSonizable{

    /**
     * Make new CategoriesData
     * 
     * @return a empty instance of CategoriesData.
     */
    public CategoriesData() {
    }
    
    /**
     * Make new CategoriesData from JSON String
     * @param json input String containing the JSON code for the object.
     * @throws JsonSyntaxException.
     * @return a instance of CategoriesData corresponding to input format.
     */
    public CategoriesData(String json) throws JsonSyntaxException{
        super(JSon.json.fromJson(json, CategoriesData.class));
    }

    /**
     * Make new CategoriesData from collection
     * 
     * @param c collection of Strings to be used.
     * @return a instance of CategoriesData from current input collection.
     */
    public CategoriesData(Collection<? extends String> c) {
        super(c);
    }

    /**
     * Make String of categories from the array
     * 
     * @param delimiter String to be used to separate categories in resulting String
     * @return String of categories separated by delimiter.
     */
    public String toString(String delimiter){
        String sCategories="";
        for(String each : this){
            sCategories+=each;
            sCategories+=delimiter;
        }
        return sCategories;
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
     * Returns an instance of CategoriesData relevant to the JSON string
     * 
     * @param jsonstring the String in json format to be converted.
     * @throws JsonSyntaxException.
     * @return instance of object corresponding to the JSON String.
     */
    public static CategoriesData unjsonize(String jsonstring) throws Exception {
        return JSon.json.fromJson(jsonstring, CategoriesData.class);
    }
    
}
