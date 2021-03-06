/*******************************************************************************
 * Copyright (c) 2011 Frederick Haebin Na and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Frederick Haebin Na - implementation of main features
 *     Robert Munteanu - initial API and implementation
 *******************************************************************************/
package org.review_board.ereviewboard.subclipse.internal.common;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.review_board.ereviewboard.subclipse.Activator;
import org.review_board.ereviewboard.subclipse.TraceLocation;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

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
	private final ISVNClientAdapter _svnClient;
	private SVNUrl _baseUrl;

	public ChangedFileFinder(ISVNLocalResource projectSvnResource, ISVNClientAdapter svnClient) {

		_location = projectSvnResource.getResource().getLocation();
		_baseUrl = projectSvnResource.getUrl();
		_svnClient = svnClient;
	}

	public List<IResource> findChangedFiles() throws SVNClientException {
		ISVNStatus[] statuses = _svnClient.getStatus(_location.toFile(), true, false);
		List<IResource> changedFiles = new ArrayList<IResource>(statuses.length);
		for (ISVNStatus svnStatus : statuses) {
			Activator.getDefault().trace(
					TraceLocation.MAIN,
					"Considering file " + svnStatus.getFile() + " with text status " + svnStatus.getTextStatus()
							+ " , prop status " + svnStatus.getPropStatus() + " , conflict descriptor "
							+ svnStatus.getConflictDescriptor() + " .");

			// can't generate diffs based on unversioned files
			if (SVNStatusKind.UNVERSIONED.equals(svnStatus.getTextStatus()))
				continue;

			// skip all forms of conflicts
			if (SVNStatusKind.CONFLICTED.equals(svnStatus.getTextStatus()))
				continue;

			if (SVNStatusKind.CONFLICTED.equals(svnStatus.getPropStatus()))
				continue;

			if (svnStatus.getConflictDescriptor() != null)
				continue;

			// only consider files
			if (!SVNNodeKind.FILE.equals(svnStatus.getNodeKind()))
				continue;
			
			boolean copied = svnStatus.isCopied();
			String relativePath = svnStatus.getUrlString().substring(_baseUrl.toString().length() + 1);
			if (!copied) {
				changedFiles.add((IResource)new ChangedFile(svnStatus.getFile(), svnStatus.getTextStatus(), relativePath));
			} else {
				ISVNInfo info = _svnClient.getInfoFromWorkingCopy(svnStatus.getFile());
				String copiedFromRelativePath = info.getCopyUrl().toString()
						.substring(_baseUrl.toString().length() + 1);
				changedFiles.add((IResource)new ChangedFile(svnStatus.getFile(), svnStatus.getTextStatus(), relativePath,
						copiedFromRelativePath));
			}
		}

		return changedFiles;

	}
}
