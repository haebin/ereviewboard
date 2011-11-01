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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.local.CreatePatchOperation;
import org.review_board.ereviewboard.subversive.Activator;
import org.review_board.ereviewboard.subversive.TraceLocation;


/**
 * The <tt>DiffCreator</tt> creates ReviewBoard-compatible diffs
 * 
 * <p>Once specific problem with svn diff is that moved files have an incorrect header.</p>
 * 
 * @see <a href="https://github.com/reviewboard/rbtools/blob/release-0.3.4/rbtools/postreview.py#L1731">post-review handling of svn renames</a>
 * @author Robert Munteanu
 */
public class DiffCreator {

    private static final String INDEX_MARKER = "Index:";
    

    public byte[] createDiff(IResource[] selectedFiles, File rootLocation) throws Exception {

        File tmpFile = null;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            tmpFile = File.createTempFile("ereviewboard", "diff");

            List<File> changes = new ArrayList<File>(selectedFiles.length);
            Map<String, String> copies = new HashMap<String, String>();
            for (IResource changedFile : selectedFiles) {
                changedFile.get
            	if (changedFile.getCopiedFromPathRelativeToProject() != null)
                    copies.put(changedFile.getPathRelativeToProject(), changedFile.getCopiedFromPathRelativeToProject());
                changes.add(changedFile.getFile());
            }
            
            
            IStatus status = new CreatePatchOperation(selectedFiles, tmpFile.getAbsolutePath(), true, true, true, true).run(new NullProgressMonitor()).getStatus();
            if (!status.isOK()) {
    			// #FIXME error handle
            	// #TODO error handle
            	
    			//String trace = ReportPartsFactory.getStackTrace(operationStatus);
    			//assertTrue(operationStatus.getMessage() + trace, false);
    		}
    		
    		
            //svnClient.createPatch(changes.toArray(new File[changes.size()]), rootLocation, tmpFile, false);

            List<String> patchLines = FileUtils.readLines(tmpFile);
            int replaceIndex = -1;
            String replaceFrom = null;
            String replaceTo = null;

            for (int i = 0; i < patchLines.size(); i++) {

                String line = patchLines.get(i);

                if ( line.toString().startsWith(INDEX_MARKER) ) {
                    String file = line.substring(INDEX_MARKER.length()).trim();
                    
                    
                    String copiedTo = copies.get(file);
                    if (copiedTo != null) {
                        Activator.getDefault().trace(TraceLocation.DIFF, "File " + file + " is copied to " + copiedTo + " .");
                        replaceIndex = i + 2;
                        replaceFrom = file;
                        replaceTo = copiedTo;
                    }
                } else if (i == replaceIndex) {
                    line = line.replace(replaceFrom, replaceTo);
                }

                outputStream.write(line.getBytes());
                outputStream.write('\n');
            }

            return outputStream.toByteArray();
        } finally {
            FileUtils.deleteQuietly(tmpFile);
        }
    }
}
