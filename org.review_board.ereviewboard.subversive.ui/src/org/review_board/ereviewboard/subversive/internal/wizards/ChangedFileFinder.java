/*******************************************************************************
 * Copyright (c) 2011 Robert Munteanu and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.subversive.internal.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.SVNChangeStatus;
import org.eclipse.team.svn.core.connector.SVNEntry.Kind;
import org.eclipse.team.svn.core.connector.SVNEntryStatus;
import org.eclipse.team.svn.core.resource.ILocalResource;
import org.eclipse.team.svn.core.svnstorage.SVNLocalResource;
import org.review_board.ereviewboard.subversive.Activator;
import org.review_board.ereviewboard.subversive.TraceLocation;


/**
 * Finds local changes for a specified <tt>location</tt>
 * 
 * <p>
 * Does not handle {@link SVNStatusKind#UNVERSIONED unversioned} files.
 * </p>
 * 
 * @author Robert Munteanu
 * 
 */
public class ChangedFileFinder {

    private final IPath _location;
    private final ISVNConnector _svnClient;
    private IPath _baseUrl;

    public ChangedFileFinder(ILocalResource projectSvnResource, ISVNConnector svnClient) {
        _location = projectSvnResource.getResource().getLocation();
        _baseUrl = projectSvnResource.getResource().getLocation();
        _svnClient = svnClient;
    }

    public List<ChangedFile> findChangedFiles() throws Exception {
    	
    	SVNChangeStatus[] statuses = _svnClient.getStatus(_location.toFile(), true, false);
    	
        List<ChangedFile> changedFiles = new ArrayList<ChangedFile>(statuses.length);

        for (ISVNStatus svnStatus : statuses) {
            
            Activator.getDefault().trace(TraceLocation.MAIN, "Considering file " + svnStatus.getFile() + 
                    " with text status " + svnStatus.getTextStatus() + 
                    " , prop status " + svnStatus.getPropStatus() + 
                    " , conflict descriptor " + svnStatus.getConflictDescriptor() + " .");

            // can't generate diffs based on unversioned files
            if ( SVNEntryStatus.UNVERSIONED.equals(svnStatus.getTextStatus()) )
                continue;

            // skip all forms of conflicts
            if ( SVNEntryStatus.CONFLICTED.equals(svnStatus.getTextStatus()) )
                continue;
            
            if ( SVNEntryStatus.CONFLICTED.equals(svnStatus.getPropStatus()) )
                continue;
            
            if ( svnStatus.getConflictDescriptor() != null )
                continue;
            
            // only consider files
            if (!Kind.FILE == (svnStatus.getNodeKind()))
                continue;

            boolean copied = svnStatus.isCopied();
            String relativePath = svnStatus.getUrlString().substring(_baseUrl.toString().length() + 1);
            if (!copied) {
                changedFiles.add(new ChangedFile(svnStatus.getFile(), svnStatus.getTextStatus(), relativePath));
            } else {
                ISVNInfo info = _svnClient.getInfoFromWorkingCopy(svnStatus.getFile());
                String copiedFromRelativePath = info.getCopyUrl().toString().substring(_baseUrl.toString().length() + 1);
                changedFiles.add(new ChangedFile(svnStatus.getFile(), svnStatus.getTextStatus(), relativePath, copiedFromRelativePath));
            }
        }
		
        return changedFiles;
    }
}
