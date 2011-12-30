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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.swt.widgets.TableItem;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.review_board.ereviewboard.subclipse.Activator;
import org.review_board.ereviewboard.subclipse.TraceLocation;
import org.review_board.ereviewboard.subclipse.internal.common.Const;
import org.review_board.ereviewboard.subclipse.internal.common.DiffCreator;
import org.review_board.ereviewboard.subclipse.internal.common.ReviewRequestContext;
import org.review_board.ereviewboard.ui.util.ReviewboardImages;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.core.util.Assert;
import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * @author Frederick Haebin Na
 * @author Robert Munteanu
 * 
 */
public class ReviewRequestWizard extends Wizard {
	public static final int TEXT_WIDTH = 500;

	private final ReviewRequestContext _context = new ReviewRequestContext();
	private DetectSelectChangesPage _detectLocalChangesPage;
	private ReviewRequestPublishPage _publishReviewRequestPage;
	private ReviewRequestUpdatePage _updateReviewRequestPage;

	public ReviewRequestWizard(IProject project) {
		_context.setProject(project);
		setWindowTitle("Create new review request");
		setDefaultPageImageDescriptor(ReviewboardImages.WIZARD_CREATE_REQUEST);
		setNeedsProgressMonitor(true);
	}

	public ReviewRequestWizard(IProject project, ReviewRequest reviewRequest) {
		_context.setProject(project);
		_context.setReviewRequest(reviewRequest);
		setWindowTitle("Update review request");
		setDefaultPageImageDescriptor(ReviewboardImages.WIZARD_CREATE_REQUEST);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		_detectLocalChangesPage = new DetectSelectChangesPage(_context);
		addPage(_detectLocalChangesPage);

		_publishReviewRequestPage = new ReviewRequestPublishPage(_context);
		addPage(_publishReviewRequestPage);

		if (_context.getReviewRequest() != null) {
			_updateReviewRequestPage = new ReviewRequestUpdatePage();
			addPage(_updateReviewRequestPage);
		}
	}

	@Override
	public boolean performFinish() {

		try {
			getContainer().run(false, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					monitor.beginTask("Posting review request", 4);

					SubMonitor sub;
					try {
						ISVNRepositoryLocation svnRepository = _context.getSvnRepositoryLocation();
						ReviewboardClient rbClient = _context.getReviewboardClient();
						Repository reviewBoardRepository = _context.getReviewboardRepository();
						SVNUrl projectUrl = SVNWorkspaceRoot.getSVNResourceFor(_context.getProject()).getUrl();

						// ----------------------------------------------------------------------------------------------------
						sub = SubMonitor.convert(monitor, "Creating patch", 1);
						byte[] diffContent = null;
						if (_detectLocalChangesPage.getSelectedFiles().length == 0) {
							TableItem[] logs = _context.getLogsTable().getSelection();
							long top = 0;
							long end = 0;
							switch (logs.length) {
							case 0:
								// #FIXME proper error
								throw new Exception(
										"Since it is post-commit review, you need to select at least one revision.");
							case 1:
								top = ((ISVNLogMessage) logs[0].getData()).getRevision().getNumber();
								end = top - 1;
								break;
							default:
								top = ((ISVNLogMessage) logs[0].getData()).getRevision().getNumber();
								end = ((ISVNLogMessage) logs[logs.length - 1].getData()).getRevision().getNumber();
								break;
							}

							SVNRevision newRev = SVNRevision.getRevision(top + "");
							SVNRevision oldRev = SVNRevision.getRevision(end + "");
							diffContent = DiffCreator.createPatch(svnRepository.getSVNClient(), projectUrl, oldRev,
									newRev);
						} else {
							diffContent = DiffCreator.createPatch(_detectLocalChangesPage.getSelectedFiles(), _context
									.getProject().getLocation().toFile(), svnRepository.getSVNClient(), _context
									.getProject().getDefaultCharset());
						}
						sub.done();

						// ----------------------------------------------------------------------------------------------------
						ReviewRequest reviewRequest;
						if (_context.getReviewRequest() == null) {
							// Initial creation

							sub = SubMonitor.convert(monitor, "Creating initial review request", 1);
							reviewRequest = rbClient.createReviewRequest(reviewBoardRepository, sub);
							sub.done();

							Activator.getDefault().trace(TraceLocation.MAIN,
									"Created review request with id " + reviewRequest.getId());
						} else {
							// Update
							reviewRequest = _context.getReviewRequest();
						}

						String basePath = projectUrl.toString().substring(
								svnRepository.getRepositoryRoot().toString().length());
						Activator.getDefault().trace(TraceLocation.MAIN, "Detected base path " + basePath);

						sub = SubMonitor.convert(monitor, "Posting diff patch", 1);
						rbClient.createDiff(reviewRequest.getId(), basePath, diffContent, monitor);
						sub.done();

						Activator.getDefault().trace(TraceLocation.MAIN, "Diff created.");

						// ----------------------------------------------------------------------------------------------------
						ReviewRequest reviewRequestForUpdate;
						String changeDescription = null;
						if (_context.getReviewRequest() == null) {
							reviewRequestForUpdate = _publishReviewRequestPage.getReviewRequest();
							reviewRequestForUpdate.setId(reviewRequest.getId());
							
							// #TODO check working status.
							// new request. save prev.
							// save!
							Platform.getPreferencesService().getRootNode().put("ereviewboard.previous.reviewer", reviewRequestForUpdate.getTargetPeopleText());
							Platform.getPreferencesService().getRootNode().put("ereviewboard.previous.group", reviewRequestForUpdate.getTargetGroupsText());
							Platform.getPreferencesService().getRootNode().put("ereviewboard.previous.summary", reviewRequestForUpdate.getSummary());
							
						} else {
							reviewRequestForUpdate = _context.getReviewRequest();
							changeDescription = _updateReviewRequestPage.getChangeDescription();
						}
						reviewRequestForUpdate.setBranch(projectUrl.getLastPathSegment());
						processLogMessagesForDescription(reviewRequestForUpdate);

						// ----------------------------------------------------------------------------------------------------
						sub = SubMonitor.convert(monitor, "Publishing review request", 1);
						rbClient.updateReviewRequest(reviewRequestForUpdate, true, changeDescription, monitor);
						sub.done();

						// ----------------------------------------------------------------------------------------------------
						TasksUiUtil.openTask(_context.getTaskRepository(), String.valueOf(reviewRequest.getId()));
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			((WizardPage) getContainer().getCurrentPage()).setErrorMessage("Failed creating new review request : "
					+ e.getCause().getMessage());
			e.getCause().printStackTrace();
			return false;
		} catch (InterruptedException e) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @param request
	 */
	private void processLogMessagesForDescription(ReviewRequest request) {
		ArrayList<String> bugsClosed = new ArrayList<String>();
		Matcher m = null;

		Assert.isNotNull(_context.getLogsTable());
		TableItem[] items = _context.getLogsTable().getSelection();
		// #FIXME do more validations
		// no return!!! because post commit review message!
		// if(items.length == 0) {
		// return;
		// }
		String desc = request.getDescription();
		String[] descParts = desc.split(Const.CONTENTS_DIV);

		StringBuffer contents = new StringBuffer();
		// omit previous log messages
		contents.append(descParts[0]);

		// parse contents for [id]
		parseBugIds(bugsClosed, descParts[0]);
		
		contents.append(Const.CONTENTS_DIV);
		if (_context.getReviewType() == Const.REVIEW_POST_COMMIT) {
			contents.append(Const.INFO_POST_COMMIT);

			long newRev = ((ISVNLogMessage) items[0].getData()).getRevision().getNumber();
			long oldRev = ((ISVNLogMessage) items[items.length - 1].getData()).getRevision().getNumber();
			if (items.length == 1)
				oldRev = newRev - 1;
			contents.append(oldRev + " to " + newRev + ".");
			contents.append(Const.EOL);
		}

		String itemLog;
		for (TableItem item : items) {
			ISVNLogMessage log = (ISVNLogMessage) item.getData();
			contents.append(Const.BULLET);
			contents.append(log.getRevision().toString());
			contents.append(Const.COLUMN_DIV);
			contents.append(Const.DATE_FORMAT.format(log.getDate()));
			contents.append(Const.COLUMN_DIV);
			contents.append(log.getAuthor());
			contents.append(Const.EOL);

			itemLog = log.getMessage().trim();
			parseBugIds(bugsClosed, itemLog);
			
			contents.append(itemLog);
			contents.append(Const.EOL);
		}

		request.setDescription(contents.toString());
		request.setBugsClosed(new ArrayList(new HashSet(bugsClosed)));
	}
 
	public void parseBugIds(ArrayList<String> bugsClosed, String str) {
		for(Pattern pattern: Const.PATTERN_BUGID) {
			Matcher m = pattern.matcher(str);
			while (m.find()) {
				bugsClosed.add(m.group(1).trim());
			}
		}
	}
}
