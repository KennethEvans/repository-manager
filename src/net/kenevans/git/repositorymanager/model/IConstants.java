package net.kenevans.git.repositorymanager.model;

/*
 * Created on Jul 9, 2012
 * By Kenneth Evans, Jr.
 */

/**
 * Provides constants for classes related to Repository Manager.
 * 
 * @author Kenneth Evans, Jr.
 */
/**
 * IConstants
 * 
 * @author Kenneth Evans, Jr.
 */
public interface IConstants
{
    public static final String LS = System.getProperty("line.separator");

    /** The title for the viewer. */
    public static final String TITLE = "Git Repository Manager";
    /** The version */
    public static final String VERSION = "1.0.0.0";
    /** The frame width for the viewer. */
    public static final int FRAME_WIDTH = 1200;
    /** The frame height for the viewer. */
    public static final int FRAME_HEIGHT = 750;
    /** The frame width for scrolled info. */
    public static final int INFO_WIDTH = 400;
    /** The frame height for scrolled info. */
    public static final int INFO_HEIGHT = 600;
    /** The frame width for the repositories dialog. */
    public static final int REPOSITORIES_DIALOG_WIDTH = 800;
    /** The frame height for the repositories dialog. */
    public static final int REPOSITORIES_DIALOG_HEIGHT = 1200;
    /** The frame width for scrolled summary details. */
    public static final int DETAILS_WIDTH = 800;
    /** The frame height for scrolled summary details. */
    public static final int DETAILS_HEIGHT = 600;
    /** The divider location for the main split pane. */
    public static final int MAIN_PANE_DIVIDER_LOCATION = 8 * FRAME_HEIGHT / 100;
    /** The divider location for the lower split pane. */
    public static final int LOWER_PANE_DIVIDER_LOCATION = 40 * FRAME_WIDTH
        / 100;
    /** The divider location for the RepositoriesDialog split pane. */
    public static final int REPOSITORIES_MAIN_PANE_DIVIDER_LOCATION = REPOSITORIES_DIALOG_WIDTH
        / 2;

    /***
     * The name of the preference node for accessing preferences for this
     * application. On Windows these are found in the registry under
     * HKCU/JavaSoft/Prefs.
     */
    public static final String P_PREFERENCE_NODE = "net/kenevans/repositorymanager/preferences";

    /*** The preference name for the repository locations. */
    public static final String P_REPOSITORY_LOCATIONS = "repositoryLocations";
    /*** The default value for the default directory for finding GPX files. */
    public static final String D_REPOSITORY_LOCATIONS = "";

    /*** The preference name for the Git Extensions path. */
    public static final String P_GIT_EXTENSIONS_PATH = "gitExtensionsPath";
    /***
     * The default value for the default directory for the Git Extensions path.
     */
    public static final String D_GIT_EXTENSIONS_PATH = "C:\\Program Files (x86)\\GitExtensions\\GitExtensions.exe";

}
