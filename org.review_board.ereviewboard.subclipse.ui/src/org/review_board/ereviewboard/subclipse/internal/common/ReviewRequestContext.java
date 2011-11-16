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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.widgets.Table;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.ReviewRequest;
import org.tigris.subversion.subclipse.core.ISVNRepositoryLocation;

/**
 * Holds information needed by all pages in the wizard
 * 
 * @author Frederick Haebin Na
 * @author Robert Munteanu
 * 
 */
public class ReviewRequestContext {

	private ReviewRequest reviewRequest;

	private ReviewboardClient reviewboardClient;
	private Repository reviewboardRepository;

	private IProject project;
	private ISVNRepositoryLocation svnRepositoryLocation;

	private TaskRepository taskRepository;
	private Table logsTable;

	private int reviewType = Const.REVIEW_PRE_COMMIT;

	private long oldRevision = -1;

	private long newRevision = -1;

	public long getOldRevision() {
		return oldRevision;
	}

	public void setOldRevision(long oldRevision) {
		this.oldRevision = oldRevision;
	}

	public long getNewRevision() {
		return newRevision;
	}

	public void setNewRevision(long newRevision) {
		this.newRevision = newRevision;
	}

	public int getReviewType() {
		return reviewType;
	}

	public void setReviewType(int reviewType) {
		this.reviewType = reviewType;
	}

	public Table getLogsTable() {
		return logsTable;
	}

	public void setLogsTable(Table logsTable) {
		this.logsTable = logsTable;
	}

	public IProject getProject() {
		return project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public ReviewRequest getReviewRequest() {
		return reviewRequest;
	}

	public void setReviewRequest(ReviewRequest reviewRequest) {
		this.reviewRequest = reviewRequest;
	}

	public ReviewboardClient getReviewboardClient() {
		Assert.isLegal(reviewboardClient != null);
		return reviewboardClient;
	}

	public ISVNRepositoryLocation getSvnRepositoryLocation() {
		return svnRepositoryLocation;
	}

	public TaskRepository getTaskRepository() {
		return taskRepository;
	}

	public void setSvnRepositoryLocation(ISVNRepositoryLocation svnRepositoryLocation) {
		this.svnRepositoryLocation = svnRepositoryLocation;
	}

	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public void setReviewboardClient(ReviewboardClient reviewboardClient) {
		this.reviewboardClient = reviewboardClient;
	}

	public Repository getReviewboardRepository() {
		return reviewboardRepository;
	}

	public void setReviewboardRepository(Repository reviewboardRepository) {
		this.reviewboardRepository = reviewboardRepository;
	}
}