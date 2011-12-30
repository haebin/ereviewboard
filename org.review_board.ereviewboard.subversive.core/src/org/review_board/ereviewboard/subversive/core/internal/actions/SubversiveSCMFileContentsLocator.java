/*******************************************************************************
 * Copyright (c) 2011 Frederick Haebin Na and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Frederick Haebin Na - Subversive implementation
 *******************************************************************************/
package org.review_board.ereviewboard.subversive.core.internal.actions;

import java.io.ByteArrayOutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.svn.core.connector.ISVNConnector;
import org.eclipse.team.svn.core.connector.ISVNProgressMonitor;
import org.eclipse.team.svn.core.connector.SVNEntryRevisionReference;
import org.eclipse.team.svn.core.connector.SVNRevision;
import org.eclipse.team.svn.core.operation.SVNNullProgressMonitor;
import org.eclipse.team.svn.core.resource.IRepositoryLocation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.SVNUtility;
import org.review_board.ereviewboard.core.internal.scm.SCMFileContentsLocator;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.subversive.core.internal.Activator;

/**
 * 
 * @author Frederick Haebin Na
 *
 */
public class SubversiveSCMFileContentsLocator implements SCMFileContentsLocator {
	private static final int DEFAULT_BUFFER_SIZE = 2048;
    private Repository _codeRepository;
    private String _filePath;
    private String _revision;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    public void init(Repository codeRepository, String filePath, String revision) {

        _codeRepository = codeRepository;
        _filePath = filePath;
        _revision = revision;
    }

    public boolean isEnabled() {
        return _codeRepository != null && _codeRepository.getTool() == RepositoryType.Subversion;
    }

    public byte[] getContents(IProgressMonitor monitor) throws CoreException{
        
        if ( FileDiff.PRE_CREATION.equals(_revision) )
            return new byte[0];

        try {
        	SVNRemoteStorage storage = SVNRemoteStorage.instance();
            IRepositoryLocation location = storage.newRepositoryLocation();
            location.setUrl(_codeRepository.getPath());
                         
            SVNRevision revision = SVNRevision.fromNumber(Integer.parseInt((_revision)));
            SVNEntryRevisionReference reference = new SVNEntryRevisionReference(_codeRepository.getPath() + _filePath, SVNRevision.HEAD, revision);
            IRepositoryResource resource = storage.asRepositoryResource(location, reference, (ISVNProgressMonitor)monitor);
            ISVNConnector proxy = resource.getRepositoryLocation().acquireSVNProxy();
           
            ByteArrayOutputStream content = new ByteArrayOutputStream();
            try {
            	proxy.streamFileContent(SVNUtility.getEntryRevisionReference(resource), this.bufferSize, content, new SVNNullProgressMonitor());
    		} finally {
    		    resource.getRepositoryLocation().releaseSVNProxy(proxy);
    		}
            
            return content.toByteArray();
        } catch (Exception e) {
            throw toCoreException(e);
        }
    }

    private CoreException toCoreException(Exception e) {
        return new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed getting contents of " + _filePath + " @ " +_revision + " : " + e.getMessage(), e));
    }
}
