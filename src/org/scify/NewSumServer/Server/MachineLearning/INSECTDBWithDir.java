/*
 * Copyright 2013 SciFY NPO <info@scify.org>.
 *
 * This product is part of the NewSum Free Software.
 * For more information about NewSum visit
 * 
 * 	http://www.scify.gr/site/en/our-projects/completed-projects/newsum-menu-en
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * If this code or its output is used, extended, re-engineered, integrated, 
 * or embedded to any extent in another software or hardware, there MUST be 
 * an explicit attribution to this work in the resulting source code, 
 * the packaging (where such packaging exists), or user interface 
 * (where such an interface exists). 
 * The attribution must be of the form "Powered by NewSum, SciFY"
 */ 

package org.scify.NewSumServer.Server.MachineLearning;

import gr.demokritos.iit.jinsect.storage.INSECTFileDB;
import java.io.Serializable;
import java.lang.String;
import java.util.ArrayList;

/**
 *
 * @author panagiotis giotis
 * 
 * Contains the override methods of INSECTFileDB
 * 
 */
public class INSECTDBWithDir extends INSECTFileDB{

    public INSECTDBWithDir(String sPrefix, String sBaseDir) {
        super(sPrefix, sBaseDir);
    }

    public INSECTDBWithDir() {
        super();
    }
    
    public static final String ListCategoryName = "nameList";
   
    /**
     * Returns a String table with all names for the category that asked
     * @param sObjectCategory The category name
     * @return A String table with all names 
     */
    
    @Override
    public String[] getObjectList(String sObjectCategory) {
        
            if((super.getObjectList(sObjectCategory).length == 0)||super.getObjectList(sObjectCategory).length == 1) {
                String[] tableList = new String[0];
                return tableList;
            }else{
                ArrayList<String> nlist = (ArrayList<String>)loadObject(sObjectCategory, ListCategoryName);
                String[] tableList = new String [nlist.size()];
        
                return nlist.toArray(tableList);
             
            }
                  
        
       
    }

    
    
    /**
     * Save object with a given name
     * @param oObj The save object
     * @param sObjectName The object name 
     * @param sObjectCategory  The category name
     */
    
    @Override
    public void saveObject(Serializable oObj, String sObjectName, String sObjectCategory) {
        
        super.saveObject(oObj, sObjectName, sObjectCategory);
        if (existsObject(sObjectCategory, ListCategoryName)){
            
            ArrayList<String> nlist = (ArrayList<String>)loadObject(sObjectCategory, ListCategoryName); //create a name list that it contains all names of save object
            nlist.add(sObjectName);                                                                     // add name in the name list
        
            super.saveObject(nlist, sObjectCategory, ListCategoryName);                                 // save the name list
            
        }else {
            ArrayList<String>  nlist = new ArrayList<String>();
            nlist.add(sObjectName);
        
            super.saveObject(nlist, sObjectCategory, ListCategoryName);
        }
        
        
    }
    
   
    
    /**
     * Deletes the object
     * @param sObjectName The object name
     * @param sObjectCategory  The category name
     */
    @Override
   public void deleteObject(String sObjectName, String sObjectCategory) {
        int index;
        super.deleteObject(sObjectName, sObjectCategory);                           // delete the object
        ArrayList<String> nlist = (ArrayList<String>)loadObject(sObjectCategory, ListCategoryName); // load the name list
        index= nlist.indexOf(sObjectName);                                          //find the index in the name list
        nlist.remove(index);                                                        // remove name from name list
        super.saveObject(nlist, sObjectCategory, ListCategoryName);                 //save new name list
    }
    
    

}
