package net.kenevans.git.repositorymanager.preferences;

import java.util.prefs.Preferences;

import net.kenevans.git.repositorymanager.model.IConstants;
import net.kenevans.git.repositorymanager.ui.RepositoryManager;
import net.kenevans.git.repositorymanager.utils.Utils;

/**
 * Settings stores the settings for the RepositoryManager.
 * 
 * @author Kenneth Evans, Jr.
 */
public class Settings implements IConstants
{
    private String gitExtensionsPath = D_GIT_EXTENSIONS_PATH;

    /**
     * Loads the settings from the preferences
     */
    public void loadFromPreferences() {
        Preferences prefs = RepositoryManager.getUserPreferences();
        gitExtensionsPath = prefs.get(P_GIT_EXTENSIONS_PATH,
            D_GIT_EXTENSIONS_PATH);
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
            prefs.put(P_GIT_EXTENSIONS_PATH, gitExtensionsPath);
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
        this.gitExtensionsPath = settings.gitExtensionsPath;
    }

    /**
     * @return The value of gitExtensionsPath.
     */
    public String getGitExtensionsPath() {
        return gitExtensionsPath;
    }

    /**
     * @param gitExtensionsPath The new value for gitExtensionsPath.
     */
    public void setGitExtensionsPath(String gitExtensionsPath) {
        this.gitExtensionsPath = gitExtensionsPath;
    }

}
