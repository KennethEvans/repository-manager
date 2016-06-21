package net.kenevans.git.repositorymanager.preferences;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.kenevans.git.repositorymanager.model.IConstants;
import net.kenevans.git.repositorymanager.model.RepositoryLocations;
import net.kenevans.git.repositorymanager.ui.RepositoryManager;
import net.kenevans.git.repositorymanager.utils.Utils;

/**
 * Settings stores the settings for the RepositoryManager.
 * 
 * @author Kenneth Evans, Jr.
 */
public class Settings implements IConstants
{
    private String jsonRepositoryLocations;
    private RepositoryLocations repositoryLocations = new RepositoryLocations(
        new ArrayList<String>(), new ArrayList<String>());

    /**
     * Loads the settings from the preferences
     */
    public void loadFromPreferences() {
        Preferences prefs = RepositoryManager.getUserPreferences();

        jsonRepositoryLocations = prefs.get(P_REPOSITORY_LOCATIONS,
            D_REPOSITORY_LOCATIONS);
        // // DEBUG
        // System.out.println(
        // "Load: jsonRepositoryLocations=" + jsonRepositoryLocations);
        Gson gson = new Gson();
        RepositoryLocations newLocations = gson.fromJson(
            jsonRepositoryLocations, new TypeToken<RepositoryLocations>() {
            }.getType());
        if(newLocations != null && newLocations.getParentDirectories() != null
            && newLocations.getIndividualRepositories() != null) {
            repositoryLocations = newLocations;
        }
    }

    /**
     * Save the current values to the preferences.
     * 
     * @param showErrors Use Utils.errMsg() to show the errors.
     * @return
     */
    public boolean saveToPreferences(boolean showErrors) {
        boolean retVal = checkValues(showErrors);
        if(!retVal) {
            return retVal;
        }
        try {
            Preferences prefs = RepositoryManager.getUserPreferences();
            Gson gson = new Gson();
            String jsonRepositoryLocations = gson.toJson(repositoryLocations,
                new TypeToken<RepositoryLocations>() {
                }.getType());
            // // DEBUG
            // System.out.println(
            // "Save: jsonRepositoryLocations=" + jsonRepositoryLocations);
            prefs.put(P_REPOSITORY_LOCATIONS, jsonRepositoryLocations);
        } catch(Exception ex) {
            retVal = false;
            if(showErrors) {
                Utils.excMsg("Error saving preferences", ex);
            }
        }
        return retVal;
    }

    /**
     * Returns if the parameters are valid
     * 
     * @param showErrors Use Utils.errMsg() to show the errors.
     * @return
     */
    public boolean checkValues(boolean showErrors) {
        boolean retVal = true;

        // RepositoryLocations
        // if(defaultDirectory == null) {
        // if(showErrors) {
        // Utils.errMsg("Value for the default directory is null");
        // }
        // retVal = false;
        // } else {
        // File file = new File(defaultDirectory);
        // if(file == null) {
        // if(showErrors) {
        // Utils.errMsg("The default directory is invalid");
        // }
        // retVal = false;
        // } else {
        // if(!file.exists()) {
        // if(showErrors) {
        // Utils.errMsg("The default directory does not exist");
        // }
        // retVal = false;
        // } else {
        // if(!file.isDirectory()) {
        // if(showErrors) {
        // Utils
        // .errMsg("The default directory is not a directory");
        // }
        // retVal = false;
        // }
        // }
        // }
        // }
        //
        // // Database
        // if(database == null) {
        // if(showErrors) {
        // Utils.errMsg("Value for the database is null");
        // }
        // retVal = false;
        // } else {
        // File file = new File(database);
        // if(file == null) {
        // if(showErrors) {
        // Utils.errMsg("The database is invalid");
        // }
        // retVal = false;
        // } else {
        // if(!file.exists()) {
        // if(showErrors) {
        // Utils.errMsg("The database does not exist");
        // }
        // retVal = false;
        // }
        // }
        // }

        return retVal;
    }

    /**
     * Copies the values in the given settings to this settings.
     * 
     * @param settings
     */
    public void copyFrom(Settings settings) {
        this.jsonRepositoryLocations = settings.jsonRepositoryLocations;
    }

    /**
     * @return The value of jsonRepositoryLocations.
     */
    public String getJsonRepositoryLocations() {
        return jsonRepositoryLocations;
    }

    /**
     * @param jsonRepositoryLocations The new value for jsonRepositoryLocations.
     */
    public void setJsonRepositoryLocations(String jsonRepositoryLocations) {
        this.jsonRepositoryLocations = jsonRepositoryLocations;
    }

    /**
     * @return The value of repositoryLocations.
     */
    public RepositoryLocations getRepositoryLocations() {
        return repositoryLocations;
    }

    /**
     * @param repositoryLocations The new value for repositoryLocations.
     */
    public void setRepositoryLocations(
        RepositoryLocations repositoryLocations) {
        this.repositoryLocations = repositoryLocations;
    }

}
