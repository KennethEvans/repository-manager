package net.kenevans.git.repositorymanager.preferences;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.kenevans.git.repositorymanager.model.IConstants;
import net.kenevans.git.repositorymanager.ui.RepositoryManager;
import net.kenevans.git.repositorymanager.utils.Utils;

/**
 * PreferencesDialog is a dialog to set the Preferences for Repository Manager. It only
 * returns after Done. It can save the values to the preference store or set
 * them in the application. In either case it remains visible.
 * 
 * @author Kenneth Evans, Jr.
 */
/**
 * PreferencesDialog
 * 
 * @author Kenneth Evans, Jr.
 */
public class PreferencesDialog extends JDialog implements IConstants
{
    private static final long serialVersionUID = 1L;
    private RepositoryManager application;
    /**
     * The return value. It is always true.
     */
    private boolean ok = true;

    private JTextField gitExtensionsPathTxt;

    /**
     * Constructor
     */
    public PreferencesDialog(JFrame parent, RepositoryManager manager) {
        super(parent);
        this.application = manager;
        if(manager == null) {
            Utils.errMsg("RepositoryManager is null");
            return;
        }
        init();
        Settings settings = new Settings();
        settings.loadFromPreferences();
        setValues(settings);
        // Locate it on the screen
        this.setLocationRelativeTo(parent);
    }

    /**
     * This method initializes this dialog
     * 
     * @return void
     */
    private void init() {
        this.setTitle("Preferences");
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new GridBagLayout());

        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.fill = GridBagConstraints.NONE;
        GridBagConstraints gbc = null;
        int gridy = -1;

        // File Group //////////////////////////////////////////////////////
        JPanel fileGroup = new JPanel();
        fileGroup.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("File"),
            BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        gridy++;
        fileGroup.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        contentPane.add(fileGroup, gbc);

        // Git Extensions path
        JLabel label = new JLabel("Git Extensions Path:");
        label.setToolTipText("The full path to the Git Extensions executable.");
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        fileGroup.add(label, gbc);

        // File JPanel holds the filename and browse button
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        fileGroup.add(filePanel, gbc);

        gitExtensionsPathTxt = new JTextField(30);
        gitExtensionsPathTxt.setToolTipText(label.getText());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 100;
        filePanel.add(gitExtensionsPathTxt, gbc);

        JButton button = new JButton();
        button.setText("Browse");
        button.setToolTipText("Choose the file.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                if(gitExtensionsPathTxt == null) {
                    return;
                }
                String initialDirName = gitExtensionsPathTxt.getText();
                String dirName = browse(initialDirName);
                gitExtensionsPathTxt.setText(dirName);
            }
        });
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridx = 1;
        filePanel.add(button);

        // Button panel /////////////////////////////////////////////////////
        gridy++;
        JPanel buttonPanel = new JPanel();
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = gridy;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        contentPane.add(buttonPanel, gbc);

        button = new JButton();
        button.setText("Use Current");
        button.setToolTipText("Set to the current application values.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                Settings settings = application.getSettings();
                if(settings == null) {
                    Utils.errMsg("Settings in the application do not exist");
                    return;
                }
                setValues(settings);
            }
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Use Defaults");
        button.setToolTipText("Set to the application default values.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                Settings settings = new Settings();
                if(settings == null) {
                    Utils.errMsg("Default settings do not exist");
                    return;
                }
                setValues(settings);
            }
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Use Stored");
        button.setToolTipText("Reset to the current stored preferences.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                Settings settings = new Settings();
                settings.loadFromPreferences();
                if(settings == null) {
                    Utils.errMsg("Cannot load preferences");
                    return;
                }
                setValues(settings);
            }
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Save");
        button.setToolTipText("Save the changes as preferences.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                save();
            }
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Set Current");
        button.setToolTipText("Set the current values in the application.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                setToApplication();
            }
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Done");
        button.setToolTipText("Close the dialog and do nothing.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                PreferencesDialog.this.setVisible(false);
            }
        });
        buttonPanel.add(button);

        pack();
    }

    /**
     * Brings up a JFileChooser to choose a directory.
     */
    private String browse(String initialDirName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(initialDirName != null) {
            File dir = new File(initialDirName);
            chooser.setCurrentDirectory(dir);
            chooser.setSelectedFile(dir);
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Process the directory
            String dirName = chooser.getSelectedFile().getPath();
            File dir = new File(dirName);
            if(!dir.exists()) {
                Utils.errMsg("Does not exist: " + dirName);
                return null;
            }
            if(!dir.isDirectory()) {
                Utils.errMsg("Not a diretory: " + dirName);
                return null;
            }
            return dirName;
        } else {
            return null;
        }
    }

    /**
     * Set the Controls from the given Settings. Can also be used to initialize
     * the dialog.
     * 
     * @param settings
     */
    public void setValues(Settings settings) {
        if(application == null) {
            return;
        }
        if(gitExtensionsPathTxt != null) {
            gitExtensionsPathTxt.setText(settings.getGitExtensionsPath());
        }
    }

    /**
     * Sets the current values in the given Settings then checks if they are
     * valid.
     * 
     * @param settings
     * @return True if they are valid, else false.
     */
    public boolean setSettingsFromValues(Settings settings) {
        if(settings == null) {
            Utils.errMsg("Input settings is null");
            return false;
        }
        try {
            settings.setGitExtensionsPath(gitExtensionsPathTxt.getText());
        } catch(Exception ex) {
            Utils.excMsg("Error reading values", ex);
            return false;
        }

        // Check if the values are valid
        boolean res = settings.checkValues(true);
        if(!res) {
            Utils.errMsg("Some values are invalid");
        }
        return res;
    }

    /**
     * Saves the current values to the preference store if they are valid.
     */
    public void save() {
        Settings settings = new Settings();
        boolean res = setSettingsFromValues(settings);
        if(!res) {
            Utils.errMsg("Aborting");
            return;
        }
        // Save to the preference store
        try {
            res = settings.saveToPreferences(true);
        } catch(Exception ex) {
            Utils.excMsg("Error saving preferences", ex);
            return;
        }
        if(res) {
            // Utils.errMsg("Preferences saved successfully");
        } else {
            Utils.errMsg("Error saving preferences");
        }
    }

    /**
     * Sets the current values to the application if they are valid.
     */
    public void setToApplication() {
        Settings settings = new Settings();
        boolean res = setSettingsFromValues(settings);
        if(!res) {
            Utils.errMsg("Aborting");
            return;
        }
        // Copy to the application settings
        try {
            application.onPreferenceReset(settings);
        } catch(Exception ex) {
            Utils.excMsg("Error setting application settings", ex);
            return;
        }
        if(res) {
            // Utils.errMsg("Settings set successfully");
        } else {
            Utils.errMsg("Error setting application settings");
        }
    }

    /**
     * Shows the dialog and returns whether it was successful or not. However
     * currently it is always successful and returns only on Done.
     * 
     * @return
     */
    public boolean showDialog() {
        setVisible(true);
        dispose();
        return ok;
    }

}
