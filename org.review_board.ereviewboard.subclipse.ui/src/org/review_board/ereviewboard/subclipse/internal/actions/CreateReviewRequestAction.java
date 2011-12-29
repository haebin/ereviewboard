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
package org.review_board.ereviewboard.subclipse.internal.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.subclipse.internal.common.Const;
import org.review_board.ereviewboard.subclipse.internal.common.WindowUtility;
import org.review_board.ereviewboard.subclipse.internal.wizards.ReviewRequestWizard;

/**
 * @author Robert Munteanu
 */
public class CreateReviewRequestAction implements IActionDelegate {

	private IProject currentProject;

	public void run(IAction action) {
		if (currentProject == null)
			return;

		if (ReviewboardCorePlugin.getDefault().getConnector().getClientManager().getAllClientUrl().isEmpty()) {
			// #TODO Need to check more things.
			// for other actions as well. (some people may have uninstalled svn
			// client and such..)
			MessageDialog
					.openWarning(
							null,
							"Review Request", "No [ReviewBoard Task Repositories] are defined. Please add one using the [Task Repositories View]."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		ReviewRequestWizard wizard = new ReviewRequestWizard(currentProject);
		
		int ok  = WindowUtility.open(wizard, Const.DEFAULT_WIDTH, Const.DEFAULT_WIDTH);
	}

	public void selectionChanged(IAction action, ISelection selection) {

		if (selection instanceof IStructuredSelection) {

			IStructuredSelection sel = (IStructuredSelection) selection;

			if (sel.getFirstElement() instanceof IProject) {
				currentProject = (IProject) sel.getFirstElement();

				return;
			}
		}

		currentProject = null;
	}
}
