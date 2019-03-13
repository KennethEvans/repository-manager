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
    private boolean isNonTrackingAhead;
    private boolean isNonTrackingBehind;
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
        isNonTrackingAhead = false;
        isNonTrackingBehind = false;
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

            // Tracking branch
            String trackingBranch = new BranchConfig(repository.getConfig(),
                repository.getBranch()).getTrackingBranch();

            // Loop over local branches
            String remoteName;
            List<Integer> counts;
            call = git.branchList().call();
            if(call.size() > 0) {
                for(Ref refLocal : call) {
                    call1 = git.branchList().setListMode(ListMode.REMOTE)
                        .call();
                    if(call1.size() > 0) {
                        for(Ref refRemote : call1) {
                            remoteName = refRemote.getName();
                            counts = JGitUtilities.calculateDivergence(
                                repository, refLocal, refRemote);
                            // Check if it is the tracking branch
                            if(trackingBranch != null && remoteName != null
                                && remoteName.equals(trackingBranch)) {
                                if(counts.get(0) > 0) {
                                    isAhead = true;
                                }
                                if(counts.get(1) > 0) {
                                    isBehind = true;
                                }
                                if(counts.get(0) < 0 || counts.get(1) < 0) {
                                    isNotTracking = true;
                                }
                            } else {
                                if(counts.get(0) > 0) {
                                    isNonTrackingAhead = true;
                                }
                                if(counts.get(1) > 0) {
                                    isNonTrackingBehind = true;
                                }
                                if(counts.get(0) < 0 || counts.get(1) < 0) {
                                    isNotTracking = true;
                                }
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
                if(!first) {
                    sb.append(ITEM_DELIMITER);
                }
                sb.append(remoteName + " " + url);
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
        sb.append(getFilePath() + COMMA + LS);
        try {
            try {
                git = Git.open(file);
            } catch(RepositoryNotFoundException ex) {
                String msg = "Repository not found";
                sb.append(msg + LS);
                return sb.toString();
            }
            repository = git.getRepository();

            // Status
            if(doStatus) {
                status = git.status().call();
                isClean = status.isClean();
                sb.append("Clean: " + status.isClean() + LS);
                if(full || !isClean) {
                    sb.append("Added: " + status.getAdded() + LS);
                    sb.append("Changed: " + status.getChanged() + LS);
                    sb.append("Conflicting: " + status.getConflicting() + LS);
                    sb.append("ConflictingStageState: "
                        + status.getConflictingStageState() + LS);
                    sb.append("IgnoredNotInIndex: "
                        + status.getIgnoredNotInIndex() + LS);
                    sb.append("Missing: " + status.getMissing() + LS);
                    sb.append("Modified: " + status.getModified() + LS);
                    sb.append("Removed: " + status.getRemoved() + LS);
                    sb.append("Untracked: " + status.getUntracked() + LS);
                    sb.append("UntrackedFolders: "
                        + status.getUntrackedFolders() + LS);
                }
            }

            // Branches
            if(doBranchTracking && full) {
                sb.append("Branches" + LS);
                call = git.branchList().setListMode(ListMode.ALL).call();
                for(Ref ref : call) {
                    sb.append(tab2 + ref.getName() + LS);
                }
            }

            // Remotes
            if(doRemotes && full) {
                sb.append("Remotes" + LS);
                Config config = repository.getConfig();
                Set<String> remotes = config.getSubsections("remote");
                if(remotes.size() == 0) {
                    sb.append(tab2 + "None" + LS);
                }
                for(String remoteName : remotes) {
                    String url = config.getString("remote", remoteName, "url");
                    sb.append(tab2 + remoteName + " (" + url + ")" + LS);
                }
            }

            // Complete branch tracking
            if(doBranchTracking) {
                sb.append("Tracking" + LS);
                if(full) {
                    // Tracking branch
                    String trackingBranch = new BranchConfig(
                        repository.getConfig(), repository.getBranch())
                            .getTrackingBranch();
                    sb.append(tab2 + "Tracking Branch: " + trackingBranch + LS);
                }
                // Loop over local branches
                List<Integer> counts;
                call = git.branchList().call();
                if(call.size() == 0) {
                    sb.append(tab2 + "No local branches" + LS);
                } else {
                    for(Ref refLocal : call) {
                        call1 = git.branchList().setListMode(ListMode.REMOTE)
                            .call();
                        if(call1.size() == 0) {
                            sb.append(tab2 + "No remote branches" + LS);
                            continue;
                        }
                        for(Ref refRemote : call1) {
                            counts = JGitUtilities.calculateDivergence(
                                repository, refLocal, refRemote);
                            sb.append(tab2 + "For " + refLocal.getName()
                                + " and " + refRemote.getName() + LS);
                            if(counts == null) {
                                sb.append(tab4 + "Not found" + LS);
                            } else if(counts.size() != 2) {
                                sb.append(tab4 + "Wrong size : " + counts.size()
                                    + LS);
                            } else {
                                sb.append(tab4 + "Commits ahead : "
                                    + counts.get(0) + " Commits behind : "
                                    + counts.get(1) + LS);
                            }
                        }
                    }
                }
            }
            sb.append(LS);
        } catch(Exception ex) {
            String msg = "Error getting status";
            Utils.excMsg(msg, ex);
            sb.append(msg + LS);
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
     * @return The value of isNonTrackingAhead.
     */
    public boolean isNonTrackingAhead() {
        return isNonTrackingAhead;
    }

    /**
     * @return The value of isNonTrackingBehind.
     */
    public boolean isNonTrackingBehind() {
        return isNonTrackingBehind;
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
