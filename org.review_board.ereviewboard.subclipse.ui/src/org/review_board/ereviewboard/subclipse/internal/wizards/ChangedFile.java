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
package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.io.File;
import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

/**
 * The <tt>ChangedFile</tt> represents a file which has local changes
 * 
 * @author Robert Munteanu
 */
class ChangedFile {

	private final File file;

	private final SVNStatusKind statusKind;

	private final String relativePath;

	private final String copiedFromRelativePath;

	public ChangedFile(File file, SVNStatusKind statusKind, String relativePath) {

		this(file, statusKind, relativePath, null);
	}

	public Object clone() {
		return null;
	}

	public ChangedFile(File file, SVNStatusKind statusKind, String relativePath, String copiedFromRelativePath) {

		this.file = file;
		this.statusKind = statusKind;
		this.relativePath = relativePath;
		this.copiedFromRelativePath = copiedFromRelativePath;
	}

	public File getFile() {

		return file;
	}

	public SVNStatusKind getStatusKind() {

		return statusKind;
	}

	/**
	 * Returns the path of the changed file relative to the parent project's
	 * location in the SVN repository
	 * 
	 * <p>
	 * For instance, if the project is located at
	 * <tt>http://svn.example.com/project</tt> and the resource at
	 * <tt>http://svn.example.com/project/dir/file.txt</tt> , the relativePath
	 * is <tt>dir/file.txt</tt>. Note that there are not leading slashes
	 * </p>
	 * 
	 * @return the path of the changed file relative to the parent project's
	 *         location in the SVN repository
	 */
	public String getPathRelativeToProject() {

		return relativePath;
	}

	/**
	 * 
	 * @return the relative path of the file this file was copied from, or
	 *         <code>null</code>
	 * @see #getPathRelativeToProject()
	 */
	public String getCopiedFromPathRelativeToProject() {

		return copiedFromRelativePath;
	}
}
