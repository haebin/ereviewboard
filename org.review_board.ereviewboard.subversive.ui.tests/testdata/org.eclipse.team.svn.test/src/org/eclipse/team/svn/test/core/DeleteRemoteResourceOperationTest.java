/*******************************************************************************
 * Copyright (c) 2005-2008 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package org.eclipse.team.svn.test.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.team.svn.core.operation.DeleteRemoteResourceOperation;
import org.eclipse.team.svn.core.operation.common.IActionOperation;
import org.eclipse.team.svn.core.resource.IRepositoryResource;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.test.TestPlugin;

/**
 * DeleteRemoteResourceOperation test
 * 
 * @author Alexander Gurov
 */
public abstract class DeleteRemoteResourceOperationTest extends AbstractOperationTestCase {

	protected IActionOperation getOperation() {
		IProject prj = this.getProject();
		
		IFile file = prj.getFile(TestPlugin.instance().getResourceBundle().getString("File.AdditionTest"));
		
		IRepositoryResource remote = SVNRemoteStorage.instance().asRepositoryResource(file);
		
		return new DeleteRemoteResourceOperation(new IRepositoryResource[] {remote}, "test delete");
	}

}
