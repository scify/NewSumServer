package org.scify.NewSumServer.Server.JSon;

import com.google.gson.Gson;

/**
 *
 * @author grv
 */

    /**
     * Class used as wrapper for Gson with data types that aren't in this package
     * JSonizable interface is implemented for the other classes
     */
public class JSon {
    protected static Gson json=new Gson();
 
    /**
     * Returns a String of the JSON format of the object
     * 
     * @param object any object to be converted.
     * @return JSON format of object.
     */
    public static String jsonize(Object object){
        return json.toJson(object);
    } 

    /**
     * Returns an instance of the Object relevant to the JSON string
     * 
     * @param jsonstring the String in json format to be converted.
     * @param classOfT the template the object corresponds to.
     * @return instance of object corresponding to the JSON String.
     */
    public static <T> T unjsonize(String jsonstring, Class<T> classOfT) throws Exception{
        return json.fromJson(jsonstring, classOfT);
    }
}
