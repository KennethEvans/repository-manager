package net.kenevans.git.repositorymanager.model;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.errors.RepositoryNotFoundException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

import net.kenevans.git.repositorymanager.utils.JGitUtilities;
import net.kenevans.git.repositorymanager.utils.Utils;

/**
 * RepositoryModel is a model for Git repository data.
 * 
 * @author Kenneth Evans, Jr.
 */
public class RepositoryModel implements IConstants
{
    private static final String TEST_REPOSITORY = "C:/eclipseWorkspaces/Work/JGit Examples";
    private File file;
    private boolean isClean;
    private boolean isAhead;
    private boolean isBehind;
    private boolean isNotTracking;
    private boolean isNotFound;
    private boolean isNoRemoteBranches;
    private static final String[] CVS_HEADINGS = new String[] {"Name", "Clean",
        "Added", "Changed", "Conflicting", "Conflicting Stage State", "Ignored",
        "Missing", "Modified", "Removed:", "Untracked", "Untracked Folders",
        "Branches", "Remotes", "Tracking Branch", "Remote Tracking", "Ahead",
        "Behind",};
    public static final String COMMA = ",";
    public static final String ITEM_DELIMITER = "\n";

    public RepositoryModel(String fileName) {
        if(fileName == null) {
            return;
        }
        file = new File(fileName);
    }

    public RepositoryModel(File file) {
        this.file = file;
    }

    public void calculateState() {
        Repository repository;
        List<Ref> call, call1;
        Status status;
        Git git;
        isClean = false;
        isAhead = false;
        isBehind = false;
        isNotTracking = false;
        isNotFound = false;
        isNoRemoteBranches = false;
        try {
            try {
                git = Git.open(file);
            } catch(RepositoryNotFoundException ex) {
                isNotFound = true;
                return;
            }
            status = git.status().call();
            isClean = status.isClean();
            repository = git.getRepository();

            // Loop over local branches
            List<Integer> counts;
            call = git.branchList().call();
            if(call.size() > 0) {
                for(Ref refLocal : call) {
                    call1 = git.branchList().setListMode(ListMode.REMOTE)
                        .call();
                    if(call1.size() > 0) {
                        for(Ref refRemote : call1) {
                            counts = JGitUtilities.calculateDivergence(
                                repository, refLocal, refRemote);
                            if(counts.get(0) > 0) {
                                isAhead = true;
                            }
                            if(counts.get(1) > 0) {
                                isBehind = true;
                            }
                            if(counts.get(0) < 0 || counts.get(1) < 0) {
                                isNotTracking = true;
                            }
                        }
                    } else {
                        isNoRemoteBranches = true;
                    }
                }
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets the status and branch tracking of the repository locations.
     * 
     */
    private String getCVSSummary() {
        StringBuilder sb = new StringBuilder();
        Repository repository;
        Status status;
        Git git = null;
        List<Ref> call, call1;
        String url;

        // Name
        sb.append(getFilePath() + COMMA);
        try {
            try {
                git = Git.open(file);
            } catch(RepositoryNotFoundException ex) {
                String msg = "Repository not found";
                sb.append(LS + msg);
                return sb.toString();
            }
            repository = git.getRepository();

            // Status
            status = git.status().call();
            sb.append(status.isClean() + COMMA);
            sb.append(status.getAdded().size() + COMMA);
            sb.append(status.getChanged().size() + COMMA);
            sb.append(status.getConflicting().size() + COMMA);
            sb.append(status.getConflictingStageState().size() + COMMA);
            sb.append(status.getIgnoredNotInIndex().size() + COMMA);
            sb.append(status.getMissing().size() + COMMA);
            sb.append(status.getModified().size() + COMMA);
            sb.append(status.getRemoved().size() + COMMA);
            sb.append(status.getUntracked().size() + COMMA);
            sb.append(status.getUntrackedFolders().size() + COMMA);

            // Branches
            sb.append("\"");
            call = git.branchList().setListMode(ListMode.ALL).call();
            boolean first = true;
            for(Ref ref : call) {
                sb.append((first ? "" : ITEM_DELIMITER) + ref.getName());
                first = false;
            }
            sb.append("\"" + COMMA);

            // Remotes
            sb.append("\"");
            Config config = repository.getConfig();
            Set<String> remotes = config.getSubsections("remote");
            first = true;
            for(String remoteName : remotes) {
                url = config.getString("remote", remoteName, "url");
                if(url == null) continue;
                sb.append((first ? "" : ITEM_DELIMITER) + url);
                first = false;
            }
            sb.append("\"" + COMMA);

            // Tracking branch
            String trackingBranch = new BranchConfig(repository.getConfig(),
                repository.getBranch()).getTrackingBranch();
            if(trackingBranch == null) {
                sb.append("None" + COMMA);
            } else {
                sb.append(trackingBranch + COMMA);
            }

            // Remote tracking
            String remote = "", ahead = "", behind = "";
            List<Integer> counts;
            call = git.branchList().call();
            if(call.size() != 0) {
                for(Ref refLocal : call) {
                    call1 = git.branchList().setListMode(ListMode.REMOTE)
                        .call();
                    if(call1.size() == 0) {
                        continue;
                    }
                    first = true;
                    for(Ref refRemote : call1) {
                        if(refRemote == null) continue;
                        counts = JGitUtilities.calculateDivergence(repository,
                            refLocal, refRemote);
                        if(first) {
                            first = false;
                        } else {
                            remote += ITEM_DELIMITER;
                            ahead += ITEM_DELIMITER;
                            behind += ITEM_DELIMITER;
                        }
                        remote += refLocal.getName() + " "
                            + refRemote.getName();
                        if(counts != null && counts.size() == 2) {
                            ahead += counts.get(0);
                            behind += counts.get(1);
                        }
                    }
                }
            }
            sb.append("\"" + remote + "\"" + COMMA);
            sb.append("\"" + ahead + "\"" + COMMA);
            sb.append("\"" + behind + "\"" + COMMA);
        } catch(Exception ex) {
            String msg = "Error getting CSV values";
            Utils.excMsg(msg, ex);
            sb.append(LS + msg);
            return sb.toString();
        }
        sb.append(LS);
        return sb.toString();
    }

    /**
     * Gets the status and branch tracking of the repository locations.
     * 
     * @param doStatus Show the status or not.
     * @param doBranchTracking Show the tracking or not.
     * @param doRemotes Show the remotes or not for full.
     * @param full For the status show the full status even if clean. Otherwise
     *            just show clean and tracking.
     */
    private String getStatus(boolean doStatus, boolean doBranchTracking,
        boolean doRemotes, boolean full) {
        String tab2 = "  ";
        String tab4 = "    ";
        StringBuilder sb = new StringBuilder();
        Repository repository;
        Status status;
        Git git = null;
        List<Ref> call, call1;
        boolean isClean;
        Utils.appendLine(sb, getFilePath(), COMMA);
        try {
            try {
                git = Git.open(file);
            } catch(RepositoryNotFoundException ex) {
                String msg = "Repository not found";
                Utils.appendLine(sb, msg);
                return sb.toString();
            }
            repository = git.getRepository();

            // Status
            if(doStatus) {
                status = git.status().call();
                isClean = status.isClean();
                Utils.appendLine(sb, "Clean: " + status.isClean());
                if(full || !isClean) {
                    Utils.appendLine(sb, "Added: " + status.getAdded());
                    Utils.appendLine(sb, "Changed: " + status.getChanged());
                    Utils.appendLine(sb,
                        "Conflicting: " + status.getConflicting());
                    Utils.appendLine(sb, "ConflictingStageState: "
                        + status.getConflictingStageState());
                    Utils.appendLine(sb,
                        "IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
                    Utils.appendLine(sb, "Missing: " + status.getMissing());
                    Utils.appendLine(sb, "Modified: " + status.getModified());
                    Utils.appendLine(sb, "Removed: " + status.getRemoved());
                    Utils.appendLine(sb, "Untracked: " + status.getUntracked());
                    Utils.appendLine(sb,
                        "UntrackedFolders: " + status.getUntrackedFolders());
                }
            }

            // Branches
            if(doBranchTracking && full) {
                Utils.appendLine(sb, "Branches");
                call = git.branchList().setListMode(ListMode.ALL).call();
                for(Ref ref : call) {
                    Utils.appendLine(sb, tab2 + ref.getName());
                }
            }

            // Remotes
            if(doRemotes && full) {
                Utils.appendLine(sb, "Remotes");
                Config config = repository.getConfig();
                Set<String> remotes = config.getSubsections("remote");
                if(remotes.size() == 0) {
                    Utils.appendLine(sb, tab2 + "None");
                }
                for(String remoteName : remotes) {
                    String url = config.getString("remote", remoteName, "url");
                    Utils.appendLine(sb, tab2 + remoteName + " (" + url + ")");
                }
            }

            // Complete branch tracking
            if(doBranchTracking) {
                Utils.appendLine(sb, "Tracking");
                if(full) {
                    // Tracking branch
                    String trackingBranch = new BranchConfig(
                        repository.getConfig(), repository.getBranch())
                            .getTrackingBranch();
                    Utils.appendLine(sb,
                        tab2 + "Tracking Branch: " + trackingBranch);
                }
                // Loop over local branches
                List<Integer> counts;
                call = git.branchList().call();
                if(call.size() == 0) {
                    Utils.appendLine(sb, tab2 + "No local branches");
                } else {
                    for(Ref refLocal : call) {
                        call1 = git.branchList().setListMode(ListMode.REMOTE)
                            .call();
                        if(call1.size() == 0) {
                            Utils.appendLine(sb, tab2 + "No remote branches");
                            continue;
                        }
                        for(Ref refRemote : call1) {
                            counts = JGitUtilities.calculateDivergence(
                                repository, refLocal, refRemote);
                            Utils.appendLine(sb,
                                tab2 + "For " + refLocal.getName() + " and "
                                    + refRemote.getName());
                            if(counts == null) {
                                Utils.appendLine(sb, tab4 + "Not found");
                            } else if(counts.size() != 2) {
                                Utils.appendLine(sb,
                                    tab4 + "Wrong size : " + counts.size());
                            } else {
                                Utils.appendLine(sb,
                                    tab4 + "Commits ahead : " + counts.get(0)
                                        + " Commits behind : " + counts.get(1));
                            }
                        }
                    }
                }
            }
            Utils.appendLS(sb);
        } catch(Exception ex) {
            String msg = "Error getting status";
            Utils.excMsg(msg, ex);
            Utils.appendLine(sb, msg);
            return sb.toString();
        }
        return sb.toString();
    }

    public static String sysInfo() {
        String info = "";
        String[] properties = {"user.dir", "java.version", "java.home",
            "java.vm.version", "java.vm.vendor", "java.ext.dirs"};
        String property;
        for(int i = 0; i < properties.length; i++) {
            property = properties[i];
            info += property + ": "
                + System.getProperty(property, "<not found>") + LS;
        }
        info += getClassPath("  ");
        return info;
    }

    public static String getClassPath(String tabs) {
        String info = "";
        String classPath = System.getProperty("java.class.path", "<not found>");
        String[] paths = classPath.split(File.pathSeparator);
        for(int i = 0; i < paths.length; i++) {
            info += tabs + i + " " + paths[i] + LS;
        }
        return info;
    }

    /**
     * Gets info about this repository.
     * 
     * @return
     */
    public String getInfo() {
        String info = "";
        info += getStatus(true, true, true, true);
        return info;
    }

    /**
     * Gets info about this repository in a CSV form.
     * 
     * @return
     */
    public String getCVSInfo() {
        String info = "";
        info += getCVSSummary();
        return info;
    }

    /**
     * Gets headings for info about this repository in a CSV form.
     * 
     * @return
     */
    public static String[] getCVSHeadings() {
        return CVS_HEADINGS;
    }

    /**
     * @return The value of fileName.
     */
    public String getFilePath() {
        return file.getPath();
    }

    /**
     * @return The value of isClean.
     */
    public boolean isClean() {
        return isClean;
    }

    /**
     * @return The value of isAhead.
     */
    public boolean isAhead() {
        return isAhead;
    }

    /**
     * @return The value of isBehind.
     */
    public boolean isBehind() {
        return isBehind;
    }

    /**
     * @return The value of isNotTracking.
     */
    public boolean isNotTracking() {
        return isNotTracking;
    }

    /**
     * @return The value of isNotFound.
     */
    public boolean isNotFound() {
        return isNotFound;
    }

    /**
     * @return The value of isNoRemoteBranches.
     */
    public boolean isNoRemoteBranches() {
        return isNoRemoteBranches;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Starting " + RepositoryModel.class.getName());
        System.out.println(TEST_REPOSITORY);
        RepositoryModel app = new RepositoryModel(TEST_REPOSITORY);
        // System.out.println(app.getInfo());
        // DEBUG
        // System.out.println();
        // System.out.println("Classpath");
        // System.out.println(getClassPath(" "));
        // System.out.println();
        // DEBUG
        System.out.println();

        System.out.println(app.getStatus(true, true, true, true));
        System.out.println("All Done");
    }

}
