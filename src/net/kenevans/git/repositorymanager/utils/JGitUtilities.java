package net.kenevans.git.repositorymanager.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;

/*
 * Created on Jun 18, 2016
 * By Kenneth Evans, Jr.
 */

public class JGitUtilities
{
    /**
     * List the counts for tracking information.
     * 
     * @param repository
     * @param branchName
     * @return
     * @throws IOException
     */
    public static List<Integer> getTrackingCounts(Repository repository,
        String branchName) throws IOException {
        BranchTrackingStatus trackingStatus = BranchTrackingStatus
            .of(repository, branchName);
        List<Integer> counts = new ArrayList<>();
        if(trackingStatus != null) {
            counts.add(trackingStatus.getAheadCount());
            counts.add(trackingStatus.getBehindCount());
        } else {
            counts.add(-1);
            counts.add(-1);
        }
        return counts;
    }

    public static List<Integer> calculateDivergence(Repository repository,
        Ref local, Ref tracking) throws IOException {
        RevWalk walk = new RevWalk(repository);
        List<Integer> counts = new ArrayList<>();
        try {
            RevCommit localCommit = walk.parseCommit(local.getObjectId());
            RevCommit trackingCommit = walk.parseCommit(tracking.getObjectId());
            walk.setRevFilter(RevFilter.MERGE_BASE);
            walk.markStart(localCommit);
            walk.markStart(trackingCommit);
            RevCommit mergeBase = walk.next();
            walk.reset();
            walk.setRevFilter(RevFilter.ALL);
            counts.add(RevWalkUtils.count(walk, localCommit, mergeBase));
            counts.add(RevWalkUtils.count(walk, trackingCommit, mergeBase));
        } finally {
            walk.dispose();
        }
        return counts;
    }

}
