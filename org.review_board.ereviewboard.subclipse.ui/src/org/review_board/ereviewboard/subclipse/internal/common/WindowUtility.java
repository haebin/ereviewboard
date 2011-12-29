/*
 * @(#)WindowUtility.java $version 2011. 12. 29.
 *
 * Copyright 2007 NHN Corp. All rights Reserved. 
 * NHN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package org.review_board.ereviewboard.subclipse.internal.common;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * @author nhn
 */
public class WindowUtility {
	public static int open(Wizard wizard, final int width, final int height) {
		IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		WizardDialog dialog = new WizardDialog(win.getShell(), wizard) {
			@Override
			protected void configureShell(Shell newShell) {
				super.configureShell(newShell);
				newShell.setSize(width, height);
				Point point = newShell.getParent().getLocation();
				Point size = newShell.getParent().getSize();

				point.x = point.x + size.x / 2;
				point.y = point.y + size.y / 2;

				point.x = point.x - 320;
				point.y = point.y - 300;
				newShell.setLocation(point);
			}
		};
		dialog.create();
		return dialog.open();
	}
}
