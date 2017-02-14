package net.kenevans.git.repositorymanager.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/*
 * Created on Feb 14, 2017
 * By Kenneth Evans, Jr.
 */

public class ScrolledHTMLDialog extends JDialog
{
    private static final long serialVersionUID = 1L;
    /**
     * The return value. It is always true.
     */
    private String DEFAULT_TITLE = "Information";
    private String DEFAULT_CONTENTS = "No contents";
    private URL contents;
    private Dimension editorSize = new Dimension(500, 600);
    private Dimension editorMinSize = new Dimension(25, 25);

    private JScrollPane editorScrollPane;
    JEditorPane editorPane;

    /**
     * Constructor
     */
    public ScrolledHTMLDialog(Component parent, URL contents) {
        super();
        this.contents = contents;
        init();
        this.setLocationRelativeTo(parent);
    }

    /**
     * This method initializes this dialog
     * 
     * @return void
     */
    private void init() {
        JButton button;
        this.setTitle(DEFAULT_TITLE);
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());

        GridBagConstraints gbcDefault = new GridBagConstraints();
        gbcDefault.insets = new Insets(2, 2, 2, 2);
        gbcDefault.fill = GridBagConstraints.BOTH;
        gbcDefault.anchor = GridBagConstraints.CENTER;
        GridBagConstraints gbc = null;
        int gridy = -1;

        // Container
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        contentPane.add(mainPanel, BorderLayout.CENTER);

        // Editor
        gridy++;
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = gridy;
        gbc.weighty = 1;
        gbc.weightx = 1;
        editorPane = new JEditorPane();
        editorPane.setEditable(false);
        editorPane.setText(DEFAULT_CONTENTS);
        if(contents != null) {
            try {
                editorPane.setPage(contents);
            } catch(Exception ex) {
                Utils.excMsg("Failed to set content", ex);
            }
        } else {
            Utils.errMsg("No content");
        }
        editorScrollPane = new JScrollPane(editorPane);
        editorScrollPane
            .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        editorScrollPane.setPreferredSize(editorSize);
        editorScrollPane.setMinimumSize(editorMinSize);
        mainPanel.add(editorScrollPane, gbc);

        // Button panel
        gridy++;
        JPanel buttonPanel = new JPanel();
        gbc = (GridBagConstraints)gbcDefault.clone();
        gbc.gridy = gridy;
        mainPanel.add(buttonPanel, gbc);

        button = new JButton();
        button.setText("OK");
        button.setToolTipText("Close the dialog.");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent ev) {
                ScrolledHTMLDialog.this.setVisible(false);
            }
        });
        buttonPanel.add(button);

        // // DEBUG
        // editorPane.setBackground(Color.RED);
        // editorScrollPane.setBackground(Color.BLUE);
        // buttonPanel.setBackground(Color.GREEN);
        // contentPane.setBackground(Color.MAGENTA);
        // mainPanel.setBackground(Color.YELLOW);

        pack();
    }

    /**
     * @return The value of contents.
     */
    public URL getContents() {
        return contents;
    }

    /**
     * @param contents The new value for contents.
     */
    public void setContents(URL contents) {
        this.contents = contents;
        if(editorPane != null && contents != null) {
            try {
                editorPane.setPage(contents);
                invalidate();
            } catch(Exception ex) {
                Utils.excMsg("Failed to set contents", ex);
            }
        }
    }

    /**
     * @return The value of editorSize.
     */
    public Dimension getEditorSize() {
        return editorSize;
    }

    /**
     * @param editorSize The new value for editorSize.
     */
    public void setEditorSize(Dimension editorSize) {
        this.editorSize = editorSize;
        if(editorScrollPane != null) {
            editorScrollPane.setPreferredSize(editorSize);
            invalidate();
        }
    }

    /**
     * @return The value of editorMinSize.
     */
    public Dimension getEditorMinSize() {
        return editorMinSize;
    }

    /**
     * @param editorMinSize The new value for editorMinSize.
     */
    public void setEditorMinSize(Dimension editorMinSize) {
        this.editorMinSize = editorMinSize;
        if(editorScrollPane != null) {
            editorScrollPane.setMinimumSize(editorMinSize);
            invalidate();
        }
    }

}
