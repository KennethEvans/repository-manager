package net.kenevans.git.repositorymanager.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.kenevans.git.repositorymanager.ui.RepositoryManager;
import net.kenevans.git.repositorymanager.utils.Utils;

public class RepositoryLocations implements IConstants
{
    private ArrayList<String> parentDirectories = new ArrayList<>();
    private ArrayList<String> individualRepositories = new ArrayList<>();

    /**
     * RepositoryLocations constructor that sets the parentDirectories and
     * individualRepositories.
     * 
     * @param parentDirectories
     * @param individualRepositories
     */
    public RepositoryLocations(ArrayList<String> parentDirectories,
        ArrayList<String> individualRepositories) {
        this.parentDirectories = parentDirectories;
        this.individualRepositories = individualRepositories;
    }

    /**
     * RepositoryLocations copy constructor that sets the parentDirectories and
     * individualRepositories from the input, making an independent copy.
     * 
     * @param repositoryLocations
     */
    public RepositoryLocations(RepositoryLocations oldRepositoryLocations) {
        for(String string : oldRepositoryLocations.getParentDirectories()) {
            parentDirectories.add(string);
        }
        for(String string : oldRepositoryLocations
            .getIndividualRepositories()) {
            individualRepositories.add(string);
        }
    }

    /**
     * RepositoryLocations constructor that has empty lists for the
     * parentDirectories and individualRepositories.
     */
    public RepositoryLocations() {
    }

    /**
     * Loads the repository locations from the preferences.
     */
    public void loadFromPreferences() {
        Preferences prefs = RepositoryManager.getUserPreferences();

        String jsonRepositoryLocations = prefs.get(P_REPOSITORY_LOCATIONS,
            D_REPOSITORY_LOCATIONS);
        loadFromJsonString(jsonRepositoryLocations);
    }

    /**
     * Save the current values to the preferences.
     * 
     * @param showErrors Use Utils.errMsg() to show the errors.
     * @return
     */
    public boolean saveToPreferences(boolean showErrors) {
        boolean retVal = true;
        try {
            String jsonRepositoryLocations = getJsonString();
            Preferences prefs = RepositoryManager.getUserPreferences();
            // // DEBUG
            // System.out.println(
            // "Save: jsonRepositoryLocations=" + jsonRepositoryLocations);
            prefs.put(P_REPOSITORY_LOCATIONS, jsonRepositoryLocations);
        } catch(Exception ex) {
            retVal = false;
            if(showErrors) {
                Utils.excMsg("Error storing repositories", ex);
            }
        }
        return retVal;
    }

    /**
     * Loads the repository locations from a JSON string.
     */
    public void loadFromJsonString(String jsonRepositoryLocations) {
        // // DEBUG
        // System.out.println(
        // "Load: jsonRepositoryLocations=" + jsonRepositoryLocations);
        Gson gson = new Gson();
        RepositoryLocations newLocations = gson.fromJson(
            jsonRepositoryLocations, new TypeToken<RepositoryLocations>() {
            }.getType());
        if(newLocations != null && newLocations.getParentDirectories() != null
            && newLocations.getIndividualRepositories() != null) {
            this.parentDirectories = newLocations.parentDirectories;
            this.individualRepositories = newLocations.individualRepositories;
        }
    }

    /**
     * Loads the repository locations from a JSON string.
     */
    public void loadFromJsonFile(File file) {
        String jsonString;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while(line != null) {
                sb.append(line);
                line = br.readLine();
            }
            jsonString = sb.toString();
            loadFromJsonString(jsonString);
        } catch(Exception ex) {
            Utils.excMsg("Error parsing JSON file", ex);
            return;
        } finally {
            if(br != null) {
                try {
                    br.close();
                } catch(Exception ex) {
                    // Do nothing
                }
            }
        }
    }

    /**
     * get the JSON string corresponding to this RepositoryLocations
     * 
     * @return
     */
    public String getJsonString() {
        Gson gson = new Gson();
        String jsonRepositoryLocations = gson.toJson(this,
            new TypeToken<RepositoryLocations>() {
            }.getType());
        return jsonRepositoryLocations;
    }

    /**
     * @return The value of parentDirectories.
     */
    public ArrayList<String> getParentDirectories() {
        return parentDirectories;
    }

    /**
     * @param parentDirectories The new value for parentDirectories.
     */
    public void setParentDirectories(ArrayList<String> parentDirectories) {
        this.parentDirectories = parentDirectories;
    }

    /**
     * @return The value of individualRepositories.
     */
    public ArrayList<String> getIndividualRepositories() {
        return individualRepositories;
    }

    /**
     * @param individualRepositories The new value for individualRepositories.
     */
    public void setIndividualRepositories(
        ArrayList<String> individualRepositories) {
        this.individualRepositories = individualRepositories;
    }

}
