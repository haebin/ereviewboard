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
package org.review_board.ereviewboard.subclipse.internal.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.review_board.ereviewboard.core.model.ReviewGroup;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.core.model.User;
import org.review_board.ereviewboard.subclipse.Activator;
import org.review_board.ereviewboard.subclipse.internal.common.Const;
import org.review_board.ereviewboard.subclipse.internal.common.ReviewRequestContext;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;

/**
 * @author Frederick Haebin Na
 * @author Robert Munteanu
 * 
 */
class ReviewRequestPublishPage extends WizardPage {

	private AutoCompleteField _toUserComboAutoCompleteField;
	private AutoCompleteField _toGroupComboAutoCompleteField;
	private ReviewRequest reviewRequest = new ReviewRequest();
	// private Table table;
	private boolean populated = false;
	private ISVNLogMessage topLog = null;
	private final ReviewRequestContext _context;
	private int tableRowIndex = 0;
	
	private Button moreButton;
	private Button resetButton;
	private String labelReset = "Selection Reset";

	public ReviewRequestPublishPage(ReviewRequestContext context) {

		super("Publish review request", "Publish review request", null);

		setMessage(
				"Fill in the review request details. Description, summary and a target person or a target group are required. In case of post-commit review, you need to select at least one revision.",
				IMessageProvider.INFORMATION);

		_context = context;

	}

	public void createControl(Composite parent) {
		Composite layout = new Composite(parent, SWT.NONE);

		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(layout);

		newLabel(layout, "Reviewer:");

		final Text toUserText = newText(layout);

		_toUserComboAutoCompleteField = new AutoCompleteField(toUserText, new TextContentAdapter(), new String[] {});

		

		newLabel(layout, "(Or Group):");

		final Text toGroupText = newText(layout);

		_toGroupComboAutoCompleteField = new AutoCompleteField(toGroupText, new TextContentAdapter(), new String[] {});

		

		newLabel(layout, "Summary:");

		final Text summary = newText(layout);
		
		
		newLabel(layout, "Description:");

		final Text description = newMultilineText(layout);

		

		newLabel(layout, "");
		newLabel(layout, "Select commit logs to add to descripton. '[ID]' will be parsed as bug IDs.");
		moreButton = new Button(layout, SWT.NONE);
		moreButton.setText("More ▶");
		moreButton.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				more();
			}

		});

		resetButton = new Button(layout, SWT.NONE);
		resetButton.setText(labelReset);
		resetButton.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				_context.getLogsTable().deselectAll();
				getContainer().updateButtons();
			}

		});

		// newLabel(layout,
		// "Select related commit logs to add to description.");

		_context.setLogsTable(new Table(layout, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI
				| SWT.FULL_SELECTION /* | SWT.CHECK */));
		_context.getLogsTable().addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				// #FIXME in this way, we can make sure that the selection is
				// always in serial range.
				if ((e.stateMask & SWT.CTRL) != 0) {
					// _context.getLogsTable().deselect(e.item.);
					_context.getLogsTable().deselectAll();
					return;
				}
				// TODO Auto-generated method stub
				getContainer().updateButtons();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				getContainer().updateButtons();
			}

		});
		_context.getLogsTable().setLinesVisible(true);
		_context.getLogsTable().setHeaderVisible(true);

		GridDataFactory.fillDefaults().span(2, 1).hint(500, 400).grab(true, true).applyTo(_context.getLogsTable());

		TableColumn revisionColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		revisionColumn.setText("Revision");

		TableColumn commentColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		commentColumn.setText("Comment");

		TableColumn dateColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		dateColumn.setText("Date");

		TableColumn authorColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		authorColumn.setText("Author");

		// newLabel(layout, "Branch:");
		//
		// final Text branch = newText(layout);
		// branch.addModifyListener(new ModifyListener() {
		//
		// public void modifyText(ModifyEvent e) {
		//
		// reviewRequest.setBranch(branch.getText());
		//
		// getContainer().updateButtons();
		// }
		// });

		// newLabel(layout, "Testing done:");
		//
		// final Text testingDone = newMultilineText(layout);
		//
		// testingDone.addModifyListener(new ModifyListener() {
		//
		// public void modifyText(ModifyEvent e) {
		//
		// reviewRequest.setTestingDone(testingDone.getText());
		//
		// getContainer().updateButtons();
		// }
		// });
		setControl(layout);
		
		if(_context.getReviewRequest() != null) {
			reviewRequest = _context.getReviewRequest();
			ReviewRequest req = _context.getReviewRequest();
			toUserText.setText(req.getTargetPeopleText());
			toGroupText.setText(req.getTargetGroupsText());
			summary.setText(req.getSummary());
			description.setText(req.getDescription().split(Const.CONTENTS_DIV)[0]);
		}
		
		toUserText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				reviewRequest.setTargetPeople(Collections.singletonList(toUserText.getText()));

				getContainer().updateButtons();
			}
		});
		
		// add event listeners after the above logic
		toGroupText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				reviewRequest.setTargetGroups(Collections.singletonList(toGroupText.getText()));

				getContainer().updateButtons();
			}
		});
		
		summary.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				reviewRequest.setSummary(summary.getText());

				getContainer().updateButtons();
			}
		});
		description.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				reviewRequest.setDescription(description.getText());

				getContainer().updateButtons();
			}
		});
		
		
	}

	private void newLabel(Composite layout, String text) {
		Label descriptionLabel = new Label(layout, SWT.NONE);
		descriptionLabel.setText(text);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BEGINNING).applyTo(descriptionLabel);
	}

	private Text newText(Composite layout) {
		final Text toUserText = new Text(layout, SWT.BORDER);
		GridDataFactory.swtDefaults().hint(ReviewRequestWizard.TEXT_WIDTH, SWT.DEFAULT).applyTo(toUserText);
		return toUserText;
	}

	private Text newMultilineText(Composite layout) {

		final Text description = new Text(layout, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.swtDefaults().hint(ReviewRequestWizard.TEXT_WIDTH, 60).applyTo(description);
		return description;
	}

	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && checkValid();
	}

	private boolean checkValid() {

		if (reviewRequest.getSummary() == null || reviewRequest.getSummary().length() == 0) {
			return false;
		}

		if ((reviewRequest.getDescription() == null || reviewRequest.getDescription().length() == 0)
				&& (_context.getLogsTable().getSelection().length == 0)) {
			return false;
		}

		if (_context.getReviewType() == Const.REVIEW_POST_COMMIT && _context.getLogsTable().getSelection().length == 0) {
			return false;
		}

		if (reviewRequest.getTargetGroups().isEmpty() && reviewRequest.getTargetPeople().isEmpty()) {
			return false;
		}

		return true;

	}

	@Override
	public void setVisible(boolean visible) {

		if (visible) {
			_toUserComboAutoCompleteField.setProposals(getUsernames());
			_toGroupComboAutoCompleteField.setProposals(getGroupNames());
			populate();
		}

		super.setVisible(visible);
	}

	private void populate() {
		if (populated)
			return;

		more();
		populated = true;
	}

	private void more() {
		try {
			//BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					moreButton.setEnabled(false);
					//resetButton.setEnabled(false);
					
					try {
						ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(_context.getProject());
						ISVNClientAdapter svnClient = _context.getSvnRepositoryLocation().getSVNClient();

						ISVNLogMessage[] logs = null;
						SVNRevision topRevision = null;
						if (topLog == null) {
							topLog = svnClient.getLogMessages(projectSvnResource.getUrl(), SVNRevision.HEAD,
									SVNRevision.HEAD, false)[0];
							topRevision = SVNRevision.getRevision(topLog.getRevision().getNumber() + "");
						} else {
							topRevision = SVNRevision.getRevision((topLog.getRevision().getNumber() - 1) + "");
						}
						
						resetButton.setText("Loading logs...");
						logs = svnClient.getLogMessages(projectSvnResource.getFile(), topRevision,
								SVNRevision.getRevision((topLog.getRevision().getNumber() - Const.PAGING_LOG) + ""),
								false);
						
						long newRevision = -1;
						long oldRevision = -1;
						if(_context.getReviewType() == Const.REVIEW_POST_COMMIT) {
							newRevision = _context.getNewRevision();
							oldRevision = _context.getOldRevision();
						}
						
						int start = 0;
						int end = 0;
						for (ISVNLogMessage log : logs) {
							int idx = 0;
							TableItem item = new TableItem(_context.getLogsTable(), SWT.NONE);
							item.setData(log);
							item.setText(idx++, log.getRevision().toString());
							item.setText(idx++, log.getMessage());
							item.setText(idx++, Const.DATE_FORMAT.format(log.getDate()));
							item.setText(idx++, log.getAuthor());
							topLog = log;
							
							if(log.getRevision().getNumber() == newRevision)
								start = tableRowIndex;
							if(log.getRevision().getNumber() == oldRevision)
								end = tableRowIndex;
							
							tableRowIndex++;
						}

						for (int i = 0; i < _context.getLogsTable().getColumnCount(); i++)
							_context.getLogsTable().getColumn(i).pack();
						
						if(start != -1)
							_context.getLogsTable().select(start, end);
					} catch (Exception e) {
						setErrorMessage(getErrorMessage());
						Activator.getDefault().log(IStatus.ERROR, e.getMessage(), e);
						e.printStackTrace();
					}
					
					moreButton.setEnabled(true);
					resetButton.setText(labelReset);
					//resetButton.setEnabled(true);
					getContainer().updateButtons();
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String[] getUsernames() {

		List<String> usernames = new ArrayList<String>();
		for (User user : _context.getReviewboardClient().getClientData().getUsers())
			usernames.add(user.getUsername());

		return usernames.toArray(new String[usernames.size()]);
	}

	private String[] getGroupNames() {

		List<String> groupNames = new ArrayList<String>();
		for (ReviewGroup group : _context.getReviewboardClient().getClientData().getGroups())
			groupNames.add(group.getName());

		return groupNames.toArray(new String[groupNames.size()]);
	}

	public ReviewRequest getReviewRequest() {

		return reviewRequest;
	}
}
