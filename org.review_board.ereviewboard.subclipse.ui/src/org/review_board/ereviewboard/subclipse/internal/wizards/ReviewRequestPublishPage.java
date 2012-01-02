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

import java.util.Collections;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.subclipse.Activator;
import org.review_board.ereviewboard.subclipse.internal.common.Const;
import org.review_board.ereviewboard.subclipse.internal.common.ReviewRequestContext;
import org.review_board.ereviewboard.ui.util.RealTimeAutoCompleteField;
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

	private RealTimeAutoCompleteField _toUserComboAutoCompleteField;
	private RealTimeAutoCompleteField _toGroupComboAutoCompleteField;
	private ReviewRequest reviewRequest = new ReviewRequest();
	// private Table table;
	private boolean populated = false;
	private ISVNLogMessage topLog = null;
	private final ReviewRequestContext _context;
	private int tableRowIndex = 0;

	private Button moreButton;
	private Button resetButton;
	private String labelReset = "Selection Reset";
	
	private Text _toUserText;
	private Text _toGroupText;
	private Text _summary;
	private Text _description;

	// private IPreferencesService service = Platform.getPreferencesService();

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
		_toUserText = toUserText;
		// get default in order to avoid sending queries to the server
		toUserText.setText(Platform.getPreferencesService().getRootNode().get("ereviewboard.previous.reviewer", ""));

		_toUserComboAutoCompleteField = new RealTimeAutoCompleteField(toUserText, new TextContentAdapter(),
				new String[] {}, true);
		toUserText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				String[] resultNames = null;
				try {
					JSONObject res = _context.getReviewboardClient().queryRealTime(
							toUserText.getText(),
							_context.getTaskRepository().getUrl() + "/api/users/?limit=10&fullname=1&timestamp="
									+ System.currentTimeMillis() + "&q=");
					JSONArray arr = res.getJSONArray("users");
					resultNames = new String[arr.length()];
					String korName = "";
					String nickName = "";
					String compId = "";
					for (int i = 0; i < arr.length(); i++) {
						JSONObject nameset = arr.getJSONObject(i);
						korName = nameset.getString("first_name").trim().equals("") ? "" : nameset
								.getString("first_name") + " ";
						nickName = nameset.getString("last_name").trim().equals("") ? "" : nameset
								.getString("last_name") + " ";
						compId = nameset.getString("username") + " ";
						resultNames[i] = korName + nickName + compId;
					}
				} catch (Exception ex) {
					// #TODO handle excepton properly
					ex.printStackTrace();
				}
				_toUserComboAutoCompleteField.setProposals(resultNames);

			}

			public void keyPressed(KeyEvent e) {
			}
		});

		newLabel(layout, "(Or Group):");
		final Text toGroupText = newText(layout);
		_toGroupText = toGroupText;
		// get default in order to avoid sending queries to the server
		toGroupText.setText(Platform.getPreferencesService().getRootNode().get("ereviewboard.previous.group", ""));
		_toGroupComboAutoCompleteField = new RealTimeAutoCompleteField(toGroupText, new TextContentAdapter(),
				new String[] {}, true);
		toGroupText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {
				String[] resultNames = null;
				try {
					JSONObject res = _context.getReviewboardClient().queryRealTime(
							toGroupText.getText(),
							_context.getTaskRepository().getUrl() + "/api/groups/?limit=10&displayname=1&timestamp="
									+ System.currentTimeMillis() + "&q=");
					JSONArray arr = res.getJSONArray("groups");
					resultNames = new String[arr.length()];
					String korName = "";
					String compId = "";
					for (int i = 0; i < arr.length(); i++) {
						JSONObject nameset = arr.getJSONObject(i);
						korName = nameset.getString("display_name").trim().equals("") ? "" : nameset
								.getString("display_name") + " ";
						compId = nameset.getString("name") + " ";
						resultNames[i] = korName + compId;
					}
				} catch (Exception ex) {
					// #TODO handle excepton properly
					ex.printStackTrace();
				}
				_toGroupComboAutoCompleteField.setProposals(resultNames);
			}

			public void keyPressed(KeyEvent e) {
			}
		});

		newLabel(layout, "Summary:");

		final Text summary = newText(layout);
		_summary = summary;
		summary.setText(Platform.getPreferencesService().getRootNode().get("ereviewboard.previous.summary", ""));

		newLabel(layout, "Description:");

		final Text description = newMultilineText(layout);
		_description = description;

		newLabel(layout, "");
		newLabel(layout, "Select commit logs to add to descripton. [ID], #ID will be parsed as bug IDs.");
		moreButton = new Button(layout, SWT.NONE);
		moreButton.setText("More");
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

		GridDataFactory.fillDefaults().span(2, 1).hint(500, 400).applyTo(_context.getLogsTable());
		TableColumn revisionColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		revisionColumn.setText("Rev");
		revisionColumn.setWidth(50);

		TableColumn commentColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		commentColumn.setText("Comment");
		commentColumn.setWidth(330);

		TableColumn dateColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		dateColumn.setText("Date");
		dateColumn.setWidth(120);

		TableColumn authorColumn = new TableColumn(_context.getLogsTable(), SWT.NONE);
		authorColumn.setText("Author");
		authorColumn.setWidth(80);

		_context.getLogsTable().setLinesVisible(true);
		_context.getLogsTable().setHeaderVisible(true);

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

		if (_context.getReviewRequest() != null) {
			reviewRequest = _context.getReviewRequest();
			ReviewRequest req = _context.getReviewRequest();
			toUserText.setText(req.getTargetPeopleText());
			toGroupText.setText(req.getTargetGroupsText());
			summary.setText(req.getSummary());
			description.setText(req.getDescription().split(Const.CONTENTS_DIV)[0]);
		}

		toUserText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				//reviewRequest.setTargetPeople(Collections.singletonList(toUserText.getText()));

				getContainer().updateButtons();
			}
		});

		// add event listeners after the above logic
		toGroupText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				//reviewRequest.setTargetGroups(Collections.singletonList(toGroupText.getText()));

				getContainer().updateButtons();
			}
		});

		summary.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				//reviewRequest.setSummary(summary.getText());

				getContainer().updateButtons();
			}
		});
		description.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {

				//reviewRequest.setDescription(description.getText());

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
		reviewRequest.setSummary(_summary.getText());
		reviewRequest.setDescription(_description.getText());
		reviewRequest.setTargetGroups(Collections.singletonList(_toGroupText.getText()));
		reviewRequest.setTargetPeople(Collections.singletonList(_toUserText.getText()));
		
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
			// _toUserComboAutoCompleteField.setProposals(getUsernames());
			// _toGroupComboAutoCompleteField.setProposals(getGroupNames());
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
			// BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					moreButton.setEnabled(false);
					resetButton.setText("Loading logs...");

					// resetButton.setFont(font)

					// resetButton.setEnabled(false);

					int logCnt = 0;
					try {
						ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(_context.getProject());
						ISVNClientAdapter svnClient = _context.getSvnRepositoryLocation().getSVNClient();

						ISVNLogMessage[] logs = null;
						SVNRevision topRevision = null;
						if (topLog == null) {
							SVNRevision latest = projectSvnResource.getLatestRemoteResource().getLastChangedRevision();
							ISVNLogMessage[] tempLogs = svnClient.getLogMessages(projectSvnResource.getUrl(), latest,
									latest, false);
							if (tempLogs == null || tempLogs.length == 0) {
								moreButton.setEnabled(true);
								resetButton.setText(labelReset);
								// resetButton.setEnabled(true);
								getContainer().updateButtons();

								return;
							}

							topLog = tempLogs[0];
							topRevision = SVNRevision.getRevision(topLog.getRevision().getNumber() + "");
						} else {
							// revision may not be serial. so, there is no
							// guarantee for the existence of rev-1.
							topRevision = topLog.getRevision();
							logCnt = -1;
						}

						// we are getting one more from the previous bottom log
						// if this is not the first page. (logCnt == -1)
						logs = svnClient.getLogMessages(projectSvnResource.getUrl(), SVNRevision.HEAD, topRevision,
								SVNRevision.getRevision("1"), false, false, Const.PAGING_LOG + (logCnt == -1 ? 1 : 0));

						long newRevision = -1;
						long oldRevision = -1;
						if (_context.getReviewType() == Const.REVIEW_POST_COMMIT) {
							newRevision = _context.getNewRevision();
							oldRevision = _context.getOldRevision();
						}

						int start = -1;
						int end = -1;
						for (ISVNLogMessage log : logs) {
							// if this is not the first page then skip the first
							// row since it is the dup row from the previous
							// set.
							if (logCnt == -1) {
								continue;
							}
							logCnt++;
							int idx = 0;
							TableItem item = new TableItem(_context.getLogsTable(), SWT.NONE);
							item.setData(log);
							item.setText(idx++, log.getRevision().toString());
							item.setText(idx++, log.getMessage());
							item.setText(idx++, Const.DATE_FORMAT.format(log.getDate()));
							item.setText(idx++, log.getAuthor());
							topLog = log;

							if (log.getRevision().getNumber() == newRevision)
								start = tableRowIndex;
							if (log.getRevision().getNumber() == oldRevision)
								end = tableRowIndex;

							tableRowIndex++;
						}

						if (start != -1) {
							// since revision are not serial, this could happen.
							if (end == -1) {
								end = start;
							}
							_context.getLogsTable().select(start, end);
						}
					} catch (Exception e) {
						setErrorMessage(getErrorMessage());
						Activator.getDefault().log(IStatus.ERROR, e.getMessage(), e);
						e.printStackTrace();
					}

					// if lest then page size then disable more.
					if (logCnt == Const.PAGING_LOG) {
						moreButton.setEnabled(true);
					}
					resetButton.setText(labelReset);
					// resetButton.setEnabled(true);
					getContainer().updateButtons();

				}
			});
		} catch (Exception e) {
			MessageDialog.openError(null, "Review Request", e.getMessage());
			// e.printStackTrace();
		}
	}

	public ReviewRequest getReviewRequest() {

		return reviewRequest;
	}
}
