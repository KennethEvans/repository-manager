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

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
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
 * returns after Cancel. It can save the values to the preference store or set
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
        PARENT, INDIVIDUAL,
    };

    private RepositoryManager manager;
    private RepositoryLocations repositoryLocations;

    private DefaultListModel<String> parentListModel = new DefaultListModel<>();
    private JList<String> parentList = new JList<>(parentListModel);
    private DefaultListModel<String> individualListModel = new DefaultListModel<>();
    private JList<String> individualList = new JList<>(individualListModel);

    /** The saved value of the last JSON file read or saved. */
    private String defaultFilePath;

    /**
     * Constructor
     */
    public RepositoriesDialog(Component parent, RepositoryManager manager) {
        super();
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
        JLabel label;
        JButton button;

        // Initialize the lists, which don't depend on anything here.
        initLists();

        // GridBag initialization
        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.anchor = GridBagConstraints.WEST;
        gbcDefault.fill = GridBagConstraints.NONE;
        GridBagConstraints gbc = null;

        JPanel mainPanel = new JPanel();
        JPanel parentPanel = new JPanel();
        JPanel individualPanel = new JPanel();

        final JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            parentPanel, individualPanel);

        JScrollPane parentScrollPane = new JScrollPane(parentList);
        JScrollPane individualScrollPane = new JScrollPane(individualList);

        JPanel mainButtonPanel = new JPanel();
        JPanel parentButtonPanel = new JPanel();
        JPanel individualButtonPanel = new JPanel();

        // // DEBUG
        // mainPanel.setBackground(Color.YELLOW);
        // parentPanel.setBackground(Color.MAGENTA);
        // individualPanel.setBackground(Color.CYAN);
        // mainButtonPanel.setBackground(Color.RED);
        // parentButtonPanel.setBackground(Color.GREEN);
        // individualButtonPanel.setBackground(Color.BLUE);

        // Content pane
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);

        // Main split pane
        mainPane.setContinuousLayout(true);
        // Keep the divider in the middle
        mainPane.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent ev) {
                JSplitPane pane = (JSplitPane)ev.getComponent();
                int width = pane.getWidth();
                mainPane.setDividerLocation(width / 2);
            }
        });
        if(false) {
            mainPane.setOneTouchExpandable(true);
        }

        // Main panel
        mainPanel.setLayout(new GridBagLayout());
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(mainPane, gbc);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(mainButtonPanel, gbc);

        // Parent panel
        parentPanel.setLayout(new GridBagLayout());
        label = new JLabel("Directories Containing Several Repositories");
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        parentPanel.add(label, gbc);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        parentPanel.add(parentScrollPane, gbc);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        parentPanel.add(parentButtonPanel, gbc);

        // Individual panel
        individualPanel.setLayout(new GridBagLayout());
        label = new JLabel("Specific Repositories");
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        individualPanel.add(label, gbc);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        individualPanel.add(individualScrollPane, gbc);
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        gbc.weightx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        individualPanel.add(individualButtonPanel, gbc);

        // Parent button panel
        button = new JButton();
        button.setText("Add...");
        button.setToolTipText("Add new items.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                add(RType.PARENT, ev);
            }
        });
        parentButtonPanel.add(button);

        button = new JButton();
        button.setText("Delete");
        button.setToolTipText("Delete selected items.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                delete(RType.PARENT, ev);
            }
        });
        parentButtonPanel.add(button);

        // Individual button panel
        button = new JButton();
        button.setText("Add...");
        button.setToolTipText("Add new items.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                add(RType.INDIVIDUAL, ev);
            }
        });
        individualButtonPanel.add(button);

        button = new JButton();
        button.setText("Delete");
        button.setToolTipText("Delete selected items.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                delete(RType.INDIVIDUAL, ev);
            }
        });
        individualButtonPanel.add(button);

        // Main button panel
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
        button.setText("Cancel");
        button.setToolTipText("Close the dialog and do nothing.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                RepositoriesDialog.this.setVisible(false);
            }
        });
        mainButtonPanel.add(button);

        pack();
    }

    private void initLists() {
        // Parent list renderer
        parentList.setCellRenderer(new DefaultListCellRenderer() {
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
        parentList.addListSelectionListener(new ListSelectionListener() {
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
            list = parentList;
        } else {
            array = repositoryLocations.getIndividualRepositories();
            model = individualListModel;
            list = individualList;
        }
        list.setEnabled(false);
        model.removeAllElements();
        for(String name : array) {
            model.addElement(name);
        }
        list.validate();
        list.setEnabled(true);
    }

    private void add(RType type, java.awt.event.ActionEvent ev) {
        ArrayList<String> array;
        String[] dirs;
        if(type == RType.PARENT) {
            array = repositoryLocations.getParentDirectories();
        } else {
            array = repositoryLocations.getIndividualRepositories();
        }
        // TODO Make a better initial directory
        dirs = chooseDirectories(null);
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

    private void delete(RType type, java.awt.event.ActionEvent ev) {
        ArrayList<String> array;
        DefaultListModel<String> model;
        JList<String> list;
        int[] selectedIndices;
        if(type == RType.PARENT) {
            array = repositoryLocations.getParentDirectories();
            model = parentListModel;
            list = parentList;
        } else {
            array = repositoryLocations.getIndividualRepositories();
            model = individualListModel;
            list = individualList;
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
            }
            return dirNames;
        } else {
            return null;
        }
    }

    /**
     * Brings up a JFileChooser to choose a file.
     */
    private File chooseFile(String initialFileName) {
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
            defaultFilePath = file.getPath();
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
    }

    /**
     * Loads the repository locations from a file with a JSON string.
     */
    public void loadFromFile() {
        File file = chooseFile(defaultFilePath);
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
        File file = chooseFile(defaultFilePath);
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
     * currently it is always successful and returns only on Cancel.
     * 
     * @return
     */
    public boolean showDialog() {
        setVisible(true);
        dispose();
        return ok;
    }

}
