/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.scify.NewSumServer.Server.SystemFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;


/**
 *
 * @author gkioumis
 */
public class Configuration {
    
    private final Properties properties;


    
    public Configuration(String configurationFileName) {
        File file = new File(configurationFileName);
        this.properties = new Properties();
        try {
            this.properties.load(new FileInputStream(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Configuration(File configurationFile) {
        this.properties = new Properties();
        try {
            this.properties.load(new FileInputStream(configurationFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Configuration() {
        this.properties = new Properties();
    }
    
    public String getServerConfigFilePath(String sLang) {

        // capitalize, if not already
        sLang = sLang.toUpperCase();
        
        return this.properties.getProperty("configPath_" + sLang, "configPath_DE");
        
    }
    
    
    public File getServerConfigFile(String sLang) {
        
        // capitalize, if not already
        sLang = sLang.toUpperCase();
        
        return new File(this.properties.getProperty("configPath_" + sLang, "configPath_DE"));
        
    }
    
    public String getCurrentLanguage() {
        
        return this.properties.getProperty("Lang");
        
    }
    
    public Locale getCurrentLocale() {
        
        return new Locale(this.properties.getProperty("Locale"));
        
    }
    
    
    public String getBaseDir() {
        
        return this.properties.getProperty("BaseDir");
        
    }
    
    public String getSourcesPath() {
        
        return this.properties.getProperty("PathToSources");
        
    }
    
    public String getindexPath() {
        
        return this.properties.getProperty("indexPath");
        
    }
    
    public String getSummaryPath() {
        
        return this.properties.getProperty("SummaryPath");
        
    }
    
    public String getArticlePath() {
        
        return this.properties.getProperty("ArticlePath");
        
    }
    
    public String getToolPath() {
        
        return this.properties.getProperty("ToolPath");
        
    }
    
    public String getCategoriesDaysFileLocation() {
        
        return this.properties.getProperty("sCatsDaysFile");
        
    }
    
    public boolean useInputDirData() {
        
        return Boolean.valueOf(this.properties.getProperty("useInputDirData"));
        
    } 
    
    public int getMaxDaysToFetchForArticles() {
        
        return Integer.valueOf(this.properties.getProperty("ArticleMaxDays"));
        
    }
    
    public boolean isThisADebugRun() {
        
        return Boolean.valueOf(this.properties.getProperty("DebugRun"));
        
    }
    
    public String[] getAvailableLanguages() {
        
        return this.properties.getProperty("Languages").split(",");
        
    }
    
    
    public void setProperty(String sKey, String sValue) {
        
        this.properties.setProperty(sKey, sValue);
        
    }

}
