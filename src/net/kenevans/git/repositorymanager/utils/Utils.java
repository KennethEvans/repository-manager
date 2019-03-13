package net.kenevans.git.repositorymanager.utils;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * Utils
 * 
 * @author Kenneth Evans, Jr.
 */
public class Utils
{
    public static final String LS = System.getProperty("line.separator");

    /**
     * Generic method to get a file using a JFileChooser
     * 
     * @param defaultPath
     * @return the File or null if aborted.
     */
    public static File getOpenFile(String defaultPath) {
        File file = null;
        JFileChooser chooser = new JFileChooser();
        if(defaultPath != null) {
            chooser.setCurrentDirectory(new File(defaultPath));
        }
        int result = chooser.showOpenDialog(null);
        if(result == JFileChooser.APPROVE_OPTION) {
            // Save the selected path for next time
            defaultPath = chooser.getSelectedFile().getParentFile().getPath();
            // Process the file
            file = chooser.getSelectedFile();
        }
        return file;
    }

    /**
     * Error message dialog
     * 
     * @param msg
     */
    public static void errMsg(final String msg) {
        // Show it in a message box
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, msg, "Error",
                    JOptionPane.ERROR_MESSAGE);
                System.out.println(msg);
            }
        });
    }

    /**
     * Warning message dialog
     * 
     * @param msg
     */
    public static void warnMsg(final String msg) {
        // Show it in a message box
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, msg, "Warning",
                    JOptionPane.WARNING_MESSAGE);
                System.out.println(msg);
            }
        });
    }

    /**
     * Information message dialog
     * 
     * @param msg
     */
    public static void infoMsg(final String msg) {
        // Show it in a message box
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, msg, "Information",
                    JOptionPane.INFORMATION_MESSAGE);
                System.out.println(msg);
            }
        });
    }

    /**
     * Exception message dialog. Displays message plus the exception and
     * exception message.
     * 
     * @param msg
     * @param ex
     */
    public static void excMsg(String msg, Exception ex) {
        final String fullMsg = msg += LS + "Exception: " + ex + LS
            + ex.getMessage();
        // Show it in a message box
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, fullMsg, "Error",
                    JOptionPane.ERROR_MESSAGE);
                System.out.println(fullMsg);
            }
        });
    }

    /**
     * Exception message dialog. Displays message plus the error and error
     * message.
     * 
     * @param msg
     * @param ex
     */
    public static void excMsg(String msg, Throwable ex) {
        final String fullMsg = msg += LS + "Exception: " + ex + LS
            + ex.getMessage();
        // Show it in a message box
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, fullMsg, "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /**
     * Checks if the file for the given file name exists.
     * 
     * @param fileName
     * @return
     */
    public static boolean checkIfFileExists(String fileName) {
        boolean exists = false;
        if(fileName != null && !fileName.equals("")) {
            File myFile = new File(fileName);

            if(myFile.exists()) {
                exists = true;
            }
        }
        return exists;
    }

    /**
     * Get the extension of a file (without the dot).
     * 
     * @param file
     * @return
     */
    public static String getExtension(File file) {
        String ext = null;
        String s = file.getName();
        int i = s.lastIndexOf('.');
        if(i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    /**
     * Returns the contents of a file as a byte array.
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public byte[] loadFileBytes(File file) throws IOException {
        if(file == null) return null;
        if(!file.exists()) {
            throw new IOException("File does not exist");
        }
        int len = (int)file.length();
        if(len == 0) {
            throw new IOException("File is empty");
        }
        byte bytes[] = new byte[len]; // Has to be int here
        int nRead = 0;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            nRead = in.read(bytes);
            if(nRead != len) {
                throw new IOException(
                    "Only read " + nRead + " of " + len + " bytes");
            }
            return bytes;
        } finally {
            if(in != null) in.close();
        }
    }

    /**
     * Generates a timestamp.
     * 
     * @return String timestamp with the current time
     */
    public static String timeStamp() {
        Date now = new Date();
        final SimpleDateFormat defaultFormatter = new SimpleDateFormat(
            "MMM dd, yyyy HH:mm:ss.SSS");
        return defaultFormatter.format(now);
    }

    /**
     * Generates a timestamp given a pattern
     * 
     * @param pattern appropriate for SimpleDateFormat
     * @return String timestamp with the current time
     */
    public static String timeStamp(String pattern) {
        Date now = new Date();
        final SimpleDateFormat dateFormatter = new SimpleDateFormat(pattern);
        return dateFormatter.format(now);
    }

    /**
     * Displays a scrolled text dialog with the given message using a default
     * font.
     *
     * @param parent
     * @param message
     * @param title
     * @param width
     * @param height
     */
    public static void scrolledTextMsg(Frame parent, String message,
        String title, int width, int height) {
        String fontName = Font.SANS_SERIF;
        int fontStyle = Font.PLAIN;
        int fontSize = 12;
        scrolledTextMsg(parent, message, title, width, height, fontName,
            fontStyle, fontSize);
    }

    /**
     * Displays a scrolled text dialog with the given message. <br>
     * Some useful possibilities for the font:
     * <ul>
     * <li>Font.SANS_SERIF, Font.PLAIN, 12</li>
     * <li>Font.MONOSPACED, Font.BOLD, 12</li>
     * <li>Font.DIALOG, Font.PLAIN, 12</li>
     * </ul>
     * You can also use a named font e.g. "Consolas".
     * 
     * @param parent
     * @param message
     * @param title
     * @param width
     * @param height
     * @param fontName
     * @param fontStyle
     * @param fontSize
     */
    public static void scrolledTextMsg(Frame parent, String message,
        String title, int width, int height, String fontName, int fontStyle,
        int fontSize) {
        final JDialog dialog = new JDialog(parent);

        // Message
        JPanel jPanel = new JPanel();
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        Font font = new Font(fontName, fontStyle, fontSize);
        textArea.setFont(font);

        JScrollPane scrollPane = new JScrollPane(textArea);
        jPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.getContentPane().add(scrollPane);

        // Close button
        jPanel = new JPanel();
        JButton button = new JButton("OK");
        jPanel.add(button);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
            }

        });
        dialog.getContentPane().add(jPanel, BorderLayout.SOUTH);

        // Settings
        dialog.setTitle(title);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.setSize(width, height);
        // Has to be done after set size
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

}
