package net.kenevans.git.repositorymanager.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.kenevans.git.repositorymanager.model.IConstants;
import net.kenevans.git.repositorymanager.model.RepositoryLocations;
import net.kenevans.git.repositorymanager.ui.RepositoryManager;
import net.kenevans.git.repositorymanager.utils.Utils;

/**
 * RepositoriesDialog is a dialog to manage repositories. It only
 * returns after Done. It can save the values to the preference store or set
 * them in the manager. In either case it remains visible.
 * 
 * @author Kenneth Evans, Jr.
 */
/**
 * PreferencesDialog
 * 
 * @author Kenneth Evans, Jr.
 */
public class RepositoriesDialog extends JDialog implements IConstants
{
    private static final long serialVersionUID = 1L;

    /**
     * The return value. It is always true.
     */
    private boolean ok = true;

    private static enum RType {
        PARENT, INDIVIDUAL, EXCLUDE,
    };

    private RepositoryManager manager;
    private RepositoryLocations repositoryLocations;

    private DefaultListModel<String> parentListModel = new DefaultListModel<>();
    private JList<String> parentDirectoryList = new JList<>(parentListModel);
    private DefaultListModel<String> individualListModel = new DefaultListModel<>();
    private JList<String> individualList = new JList<>(individualListModel);
    private DefaultListModel<String> excludeListModel = new DefaultListModel<>();
    private JList<String> excludeList = new JList<>(excludeListModel);

    /** Keeps the last-used path for the file open dialog. */
    public String defaultAddPath;

    /** The saved value of the last JSON file read or saved. */
    private String defaultJsonFilePath;

    /**
     * Constructor
     */
    public RepositoriesDialog(JFrame parent, RepositoryManager manager) {
        super(parent);
        this.manager = manager;
        if(manager == null) {
            Utils.errMsg("RepositoryManager is null");
            return;
        }
        // Make a copy
        repositoryLocations = new RepositoryLocations(
            manager.getRepositoryLocations());
        init();
        populate(RType.PARENT);
        populate(RType.INDIVIDUAL);
        populate(RType.EXCLUDE);

        // Locate it on the screen
        this.setLocationRelativeTo(parent);
    }

    /**
     * This method initializes this dialog
     * 
     * @return void
     */
    private void init() {
        this.setTitle("Repositories");
        JButton button;

        // Initialize the lists, which don't depend on anything here.
        initLists();

        // GridBag initialization
        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.fill = GridBagConstraints.NONE;
        GridBagConstraints gbc = null;

        // Main panels
        JPanel firstPanel = new JPanel();
        JPanel secondPanel = new JPanel();

        // Create the panels for each type
        JPanel parentDirectoryPanel = createPanelForType(
            "Directories Containing Several Repositories", RType.PARENT,
            parentDirectoryList);
        JPanel individualPanel = createPanelForType("Specific Repositories",
            RType.INDIVIDUAL, individualList);
        JPanel excludePanel = createPanelForType(
            "Exclude Specific Repositories", RType.EXCLUDE, excludeList);

        // Main split pane
        final JSplitPane firstPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            parentDirectoryPanel, secondPanel);
        firstPane.setContinuousLayout(true);
        // Keep the same relative size
        firstPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ev) {
                JSplitPane pane = (JSplitPane)ev.getComponent();
                int width = pane.getWidth();
                firstPane.setDividerLocation(width / 3);
            }
        });

        // Second split pane
        final JSplitPane secondPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, individualPanel, excludePanel);
        secondPane.setContinuousLayout(true);
        // Keep the same relative size
        secondPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ev) {
                JSplitPane pane = (JSplitPane)ev.getComponent();
                int width = pane.getWidth();
                secondPane.setDividerLocation(width / 2);
            }
        });
        // Eliminate the border on the second pane, so all look the same
        secondPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Content pane
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(firstPanel, BorderLayout.CENTER);

        // First panel
        firstPanel.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        // gbc.insets = new Insets(0, 0, 0, 0);
        firstPanel.add(firstPane, gbc);

        // Second panel
        secondPanel.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        secondPanel.add(secondPane, gbc);
        secondPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // Main button panel
        JPanel mainButtonPanel = new JPanel();
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        firstPanel.add(mainButtonPanel, gbc);

        // Buttons
        button = new JButton();
        button.setText("Load Current");
        button.setToolTipText("Load the current manager values.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                loadFromManager();
            }
        });
        mainButtonPanel.add(button);

        button = new JButton();
        button.setText("Load File");
        button.setToolTipText("Load from a file.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                loadFromFile();
            }
        });
        mainButtonPanel.add(button);

        button = new JButton();
        button.setText("Load Stored");
        button.setToolTipText("Load the current stored preferences.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                loadFromPreferences();
            }
        });
        mainButtonPanel.add(button);

        button = new JButton();
        button.setText("Set Current");
        button.setToolTipText("Set the current values in the manager.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                setToManager();
            }
        });
        mainButtonPanel.add(button);

        button = new JButton();
        button.setText("Save File");
        button.setToolTipText("Save the changes in a file.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                saveToFile();
            }
        });
        mainButtonPanel.add(button);

        button = new JButton();
        button.setText("Save Stored");
        button.setToolTipText("Save the changes as preferences.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                saveToPreferences();
            }
        });
        mainButtonPanel.add(button);

        button = new JButton();
        button.setText("Done");
        button.setToolTipText("Close the dialog and do nothing.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                RepositoriesDialog.this.setVisible(false);
            }
        });
        mainButtonPanel.add(button);

        // // DEBUG
        // mainPanel.setBackground(Color.YELLOW);
        // parentDirectoryPanel.setBackground(Color.MAGENTA);
        // individualPanel.setBackground(Color.CYAN);
        // excludePanel.setBackground(Color.PINK);
        // mainButtonPanel.setBackground(Color.RED);

        pack();
    }

    /**
     * Creates a JPanel for the specified type.
     * 
     * @param title Title for the panel.
     * @param type The RType
     * @param list The list for the panel.
     * @return The created panel;
     */
    private JPanel createPanelForType(String title, final RType type,
        JList<String> list) {
        JButton button;
        JLabel label;

        // GridBag initialization
        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.fill = GridBagConstraints.NONE;
        GridBagConstraints gbc = null;

        // Panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        // Label
        label = new JLabel(title);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(label, gbc);

        // Scroll pane
        JScrollPane scrollPane = new JScrollPane(list);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(scrollPane, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel();
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(buttonPanel, gbc);

        // Button panel buttons
        button = new JButton();
        button.setText("Add...");
        button.setToolTipText("Add new items.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                add(type, ev);
            }
        });
        buttonPanel.add(button);

        button = new JButton();
        button.setText("Delete");
        button.setToolTipText("Delete selected items.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                delete(type, ev);
            }
        });
        buttonPanel.add(button);

        return panel;
    }

    private void initLists() {
        // Parent list renderer
        parentDirectoryList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList<?> list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
                String name = (String)value;
                // Set the text
                label.setText(name);
                return label;
            }
        });
        parentDirectoryList
            .addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent ev) {
                    if(ev.getValueIsAdjusting()) return;
                    // TODO
                }
            });

        // Individual list renderer
        individualList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList<?> list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
                String name = (String)value;
                // Set the text
                label.setText(name);
                return label;
            }
        });
        individualList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent ev) {
                if(ev.getValueIsAdjusting()) return;
                // TODO
            }
        });

        // Exclude list renderer
        excludeList.setCellRenderer(new DefaultListCellRenderer() {
            private static final long serialVersionUID = 1L;

            public Component getListCellRendererComponent(JList<?> list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
                JLabel label = (JLabel)super.getListCellRendererComponent(list,
                    value, index, isSelected, cellHasFocus);
                String name = (String)value;
                // Set the text
                label.setText(name);
                return label;
            }
        });
        excludeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent ev) {
                if(ev.getValueIsAdjusting()) return;
                // TODO
            }
        });

    }

    /**
     * Populates the list determined by the given type.
     * 
     * @param type
     */
    private void populate(RType type) {
        ArrayList<String> array;
        DefaultListModel<String> model;
        JList<String> list;
        if(type == RType.PARENT) {
            array = repositoryLocations.getParentDirectories();
            model = parentListModel;
            list = parentDirectoryList;
        } else if(type == RType.INDIVIDUAL) {
            array = repositoryLocations.getIndividualRepositories();
            model = individualListModel;
            list = individualList;
        } else if(type == RType.EXCLUDE) {
            array = repositoryLocations.getExcludeRepositories();
            model = excludeListModel;
            list = excludeList;
        } else {
            return;
        }
        list.setEnabled(false);
        model.removeAllElements();
        for(String name : array) {
            model.addElement(name);
        }
        list.validate();
        list.setEnabled(true);
    }

    /**
     * Add a directory to the list.
     * 
     * @param type
     * @param ev
     */
    private void add(RType type, java.awt.event.ActionEvent ev) {
        ArrayList<String> array;
        String[] dirs;
        if(type == RType.PARENT) {
            array = repositoryLocations.getParentDirectories();
        } else if(type == RType.INDIVIDUAL) {
            array = repositoryLocations.getIndividualRepositories();
        } else {
            array = repositoryLocations.getExcludeRepositories();
        }
        // TODO Make a better initial directory
        dirs = chooseDirectories(defaultAddPath);
        if(dirs == null) {
            return;
        }
        if(dirs.length == 0) {
            Utils.errMsg("No items selected");
            return;
        }
        for(String dir : dirs) {
            // Also change backslash to forward slash
            array.add(dir.replace('\\', '/'));
        }
        populate(type);
    }

    /**
     * Remove a directory from the list.
     * 
     * @param type
     * @param ev
     */
    private void delete(RType type, java.awt.event.ActionEvent ev) {
        ArrayList<String> array;
        DefaultListModel<String> model;
        JList<String> list;
        int[] selectedIndices;
        if(type == RType.PARENT) {
            array = repositoryLocations.getParentDirectories();
            model = parentListModel;
            list = parentDirectoryList;
        } else if(type == RType.INDIVIDUAL) {
            array = repositoryLocations.getIndividualRepositories();
            model = individualListModel;
            list = individualList;
        } else {
            array = repositoryLocations.getExcludeRepositories();
            model = excludeListModel;
            list = excludeList;
        }
        selectedIndices = list.getSelectedIndices();
        if(selectedIndices.length == 0) {
            Utils.errMsg("No items selected");
            return;
        }
        for(int i : selectedIndices) {
            array.remove(model.getElementAt(i));
        }
        populate(type);
    }

    /**
     * Brings up a JFileChooser to choose one or more directories.
     */
    private String[] chooseDirectories(String initialDirName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        if(initialDirName != null) {
            File dir = new File(initialDirName);
            chooser.setCurrentDirectory(dir);
            chooser.setSelectedFile(dir);
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Process the directory
            File[] dirs = chooser.getSelectedFiles();
            String[] dirNames = new String[dirs.length];
            for(int i = 0; i < dirs.length; i++) {
                dirNames[i] = dirs[i].getPath();
                if(i == 0) {
                    File parent = dirs[i].getParentFile();
                    if(parent != null) {
                        defaultAddPath = parent.getPath();
                    }
                    defaultAddPath = dirs[i].getParent();
                    if(parent != null) {

                    }
                }
            }
            return dirNames;
        } else {
            return null;
        }
    }

    /**
     * Brings up a JFileChooser to choose a file.
     */
    private File chooseJsonFile(String initialFileName) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        if(initialFileName != null) {
            File file = new File(initialFileName);
            chooser.setSelectedFile(file);
        }
        int result = chooser.showOpenDialog(this);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Process the directory
            File file = chooser.getSelectedFile();
            defaultJsonFilePath = file.getPath();
            return file;
        } else {
            return null;
        }
    }

    /**
     * Loads the repository locations from the current values in the manager.
     */
    public void loadFromManager() {
        if(manager == null) {
            return;
        }
        repositoryLocations = new RepositoryLocations(
            manager.getRepositoryLocations());
        populate(RType.PARENT);
        populate(RType.INDIVIDUAL);
        populate(RType.EXCLUDE);
    }

    /**
     * Loads the repository locations from a file with a JSON string.
     */
    public void loadFromFile() {
        File file = chooseJsonFile(defaultJsonFilePath);
        if(file == null) {
            return;
        }
        try {
            repositoryLocations.loadFromJsonFile(file);
        } catch(Exception ex) {
            Utils.excMsg("Error loading from preferences", ex);
            return;
        }
        populate(RType.PARENT);
        populate(RType.INDIVIDUAL);
        populate(RType.EXCLUDE);
    }

    /**
     * Loads the repository locations from the preference store.
     */
    public void loadFromPreferences() {
        try {
            repositoryLocations.loadFromPreferences();
        } catch(Exception ex) {
            Utils.excMsg("Error loading from preferences", ex);
            return;
        }
        populate(RType.PARENT);
        populate(RType.INDIVIDUAL);
        populate(RType.EXCLUDE);
    }

    /**
     * Sets the current values to the manager if they are valid.
     */
    public void setToManager() {
        if(manager == null) {
            return;
        }
        // Set a copy in the manager
        try {
            manager.setRepositoryLocations(
                new RepositoryLocations(repositoryLocations));
            manager.refresh();
        } catch(Exception ex) {
            Utils.excMsg("Error setting repositories in the manager", ex);
            return;
        }
    }

    /**
     * Sets the current values to the manager if they are valid.
     */
    public void saveToFile() {
        // If this is non null and there is a file, it is ok to overwrite.
        File file = chooseJsonFile(defaultJsonFilePath);
        if(file == null) {
            return;
        }
        if(file.exists()) {
            int res = JOptionPane.showConfirmDialog(this,
                "File exists:" + LS + file.getPath() + LS + "OK to overwrite?",
                "File Exists", JOptionPane.OK_CANCEL_OPTION);
            if(res != JOptionPane.OK_OPTION) return;
        }
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(file));
            String jsonString = repositoryLocations.getJsonString();
            out.write(jsonString);
        } catch(Exception ex) {
            Utils.excMsg("Error writing " + file.getPath(), ex);
        } finally {
            if(out != null) out.close();
            out = null;
        }
    }

    /**
     * Saves the current values to the preference store if they are valid.
     */
    public void saveToPreferences() {
        // Save to the preference store
        try {
            repositoryLocations.saveToPreferences(true);
        } catch(Exception ex) {
            Utils.excMsg("Error saving to preference store", ex);
            return;
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
