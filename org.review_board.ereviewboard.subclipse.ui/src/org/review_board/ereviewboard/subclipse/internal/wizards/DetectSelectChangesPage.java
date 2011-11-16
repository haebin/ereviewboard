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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.team.core.RepositoryProvider;
import org.review_board.ereviewboard.core.ReviewboardClientManager;
import org.review_board.ereviewboard.core.ReviewboardCorePlugin;
import org.review_board.ereviewboard.core.client.ReviewboardClient;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.core.model.RepositoryType;
import org.review_board.ereviewboard.subclipse.Activator;
import org.review_board.ereviewboard.subclipse.TraceLocation;
import org.review_board.ereviewboard.subclipse.internal.common.Const;
import org.review_board.ereviewboard.subclipse.internal.common.ReviewRequestContext;
import org.review_board.ereviewboard.subclipse.ui.util.SelectionTree;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
import org.tigris.subversion.subclipse.core.SVNTeamProvider;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.subclipse.ui.Policy;

/**
 * The <tt>DetectLocalChangesPage</tt> shows the local changes
 * 
 * <p>
 * It also allows selection of the resources to be included in the review
 * request.
 * </p>
 * 
 * @author Frederick Haebin Na
 * @author Robert Munteanu
 * 
 */
class DetectSelectChangesPage extends WizardPage {

	private List<IResource> allResourcesInProject = new LinkedList<IResource>();

	private IResource[] selectedResources;

	private ReviewRequestContext context;

	private Button includeAllButton;
	private Button postCommitButton;
	private Button preCommitButton;
	// private Button showCompareButton;

	// private CompareViewerSwitchingPane compareViewerPane;
	private Action includeAllAction;

	private boolean includeAll = false;
	// private boolean showCompare;

	// private SashForm verticalSash;
	// private SashForm horizontalSash;

	private SelectionTree resourceSelectionTree;
	
	private boolean initialUpdateAccess = false;

	public DetectSelectChangesPage(ReviewRequestContext context) {
		super("Select changes", "Select changes", null);
		setMessage(
				"Select the changes to submit for review. The ReviewBoard instance and the SVN repository have been auto-detected.",
				IMessageProvider.INFORMATION);
		this.context = context;
		initMetaData();
	}

	public void createControl(Composite parent) {
		// horizontalSash = new SashForm(parent, SWT.HORIZONTAL);
		// horizontalSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		// true));

		// verticalSash = new SashForm(horizontalSash, SWT.VERTICAL);

		// GridLayout gridLayout = new GridLayout();
		// gridLayout.marginHeight = 0;
		// gridLayout.marginWidth = 0;
		// verticalSash.setLayout(gridLayout);
		// verticalSash.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Composite cTop = new Composite(horizontalSash, SWT.NULL);
		// GridLayout topLayout = new GridLayout();
		// topLayout.marginHeight = 0;
		// topLayout.marginWidth = 0;
		// cTop.setLayout(topLayout);
		// cTop.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Composite cBottom = new Composite(verticalSash, SWT.NULL);
		// GridLayout bottomLayout = new GridLayout();
		// bottomLayout.marginHeight = 0;
		// bottomLayout.marginWidth = 0;
		// cBottom.setLayout(bottomLayout);
		// cBottom.setLayoutData(new GridData(GridData.FILL_BOTH));

		// **************************************************

		Composite body = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(body);

		Composite layout = new Composite(body, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(layout);

		Label rbRepositoryLabel = new Label(layout, SWT.NONE);
		rbRepositoryLabel.setText("Reviewboard repository :");

		Label foundRbRepositoryLabel = new Label(layout, SWT.NONE);
		foundRbRepositoryLabel.setText(context.getTaskRepository().getRepositoryLabel());
		foundRbRepositoryLabel.setToolTipText(context.getTaskRepository().getRepositoryUrl());

		Label svnRepositoryLabel = new Label(layout, SWT.NONE);
		svnRepositoryLabel.setText("SVN repository :");

		Label foundSvnRepositoryLabel = new Label(layout, SWT.NONE);
		foundSvnRepositoryLabel.setText(context.getReviewboardRepository().getName());
		foundSvnRepositoryLabel.setToolTipText(context.getReviewboardRepository().getPath());
		if (context.getReviewRequest() != null) {
			Label reviewRequestLabel = new Label(layout, SWT.NONE);
			reviewRequestLabel.setText("Review request :");

			Label reviewRequestName = new Label(layout, SWT.NONE);
			reviewRequestName.setText(context.getReviewRequest().getSummary());
		}

		// GridLayoutFactory.swtDefaults().numColumns(2).applyTo(layout);
		// bottom
		// Composite controls = new Composite(ctop, SWT.NONE);
		// GridLayoutFactory.swtDefaults().numColumns(2).applyTo(controls);
		//
		preCommitButton = new Button(layout, SWT.RADIO);
		preCommitButton.setText("Pre-Commit Review");
		preCommitButton.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				setPreCommitReview();
				getContainer().updateButtons();
			}

		});

		postCommitButton = new Button(layout, SWT.RADIO);
		postCommitButton.setText("Post-Commit Review");
		postCommitButton.addMouseListener(new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
			}

			public void mouseDown(MouseEvent e) {
			}

			public void mouseUp(MouseEvent e) {
				setPostCommitReview();
				getContainer().updateButtons();
			}
		});

		// Composite resource = new Composite(layout, SWT.NONE);
		// GridDataFactory.fillDefaults().span(2, 1).hint(500, 400).grab(true,
		// true).applyTo(layout);

		addResourcesArea(body);
		// compareViewerPane = new CompareViewerSwitchingPane(horizontalSash,
		// SWT.BORDER | SWT.FLAT) {
		// protected Viewer getViewer(Viewer oldViewer, Object input) {
		// CompareConfiguration cc = new CompareConfiguration();
		// cc.setLeftEditable(false);
		// cc.setRightEditable(false);
		// return CompareUI.findContentViewer(oldViewer, input, this, cc);
		// }
		// };
		// compareViewerPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
		// true, true));

		// int vWeight1 = 20;
		// int vWeight2 = 80;
		//
		// int hWeight1 = 35;
		// int hWeight2 = 65;
		//
		// if (showCompare) {
		// showCompareButton.setSelection(true);
		// } else {
		// //horizontalSash.setMaximizedControl(verticalSash);
		// }
		//
		// //verticalSash.setWeights(new int[] { vWeight1, vWeight2 });
		// horizontalSash.setWeights(new int[] { hWeight1, hWeight2 });

		// this is pretty important. if you don't do it properly this page will
		// remain in the next page.
		// setControl(horizontalSash);
		setControl(body);

		if (context.getReviewRequest() != null) {
			String[] parts = context.getReviewRequest().getDescription().split(Const.INFO_POST_COMMIT);
			// if it was a post-commit review
			if (parts.length > 1) {
				setPostCommitReview();
				// parse old, new rev.
				String strOldNew = parts[1].split("\\.")[0];
				String[] arrOldNew = strOldNew.split(" to ");

				String oldRev = arrOldNew[0].trim();
				String newRev = arrOldNew[1].trim();

				context.setOldRevision(Long.parseLong(oldRev));
				context.setNewRevision(Long.parseLong(newRev));
				initialUpdateAccess = true;
			}
		}
	}

	private void setEnableRecursively(Control ctrl, boolean enabled) {
		if (ctrl instanceof Composite) {
			Composite comp = (Composite) ctrl;
			comp.setEnabled(enabled);
			for (Control c : comp.getChildren())
				setEnableRecursively(c, enabled);
		} else {
			ctrl.setEnabled(enabled);
		}
	}

	private void addResourcesArea(Composite composite) {
		SelectionTree.IToolbarControlCreator toolbarControlCreator = new SelectionTree.IToolbarControlCreator() {
			public void createToolbarControls(ToolBarManager toolbarManager) {

				toolbarManager.add(new ControlContribution("Show All") { //$NON-NLS-1$
							protected Control createControl(Composite parent) {
								includeAllButton = new Button(parent, SWT.CHECK);
								includeAllButton.setText("Show all files"); //$NON-NLS-1$
								includeAllButton.setSelection(includeAll);
								includeAllButton.addSelectionListener(new SelectionListener() {
									public void widgetSelected(SelectionEvent e) {
										includeAll = includeAllButton.getSelection();
										includeAllAction.setChecked(includeAll);
										toggleIncludeAll();
									}

									public void widgetDefaultSelected(SelectionEvent e) {
									}
								});
								return includeAllButton;
							}
						});
				toolbarManager.add(new Separator());
				// toolbarManager.add(new ControlContribution("showCompare") {
				// protected Control createControl(Composite parent) {
				// showCompareButton = new Button(parent, SWT.TOGGLE |
				// SWT.FLAT);
				//						showCompareButton.setImage(SVNUIPlugin.getImage(ISVNUIConstants.IMG_SYNCPANE)); //$NON-NLS-1$
				// showCompareButton.setToolTipText(Policy.bind("CommitDialog.showCompare"));
				// showCompareButton.setSelection(showCompare);
				// showCompareButton.addSelectionListener(new
				// SelectionListener() {
				// public void widgetSelected(SelectionEvent e) {
				// showComparePane(!showCompare);
				// }
				//
				// public void widgetDefaultSelected(SelectionEvent e) {
				// }
				// });
				// return showCompareButton;
				// }
				// });
			}

			public int getControlCount() {
				return 1;
			}
		};

		// resourceSelectionTree = new ResourceSelectionTree(
		// composite,
		// SWT.NONE,
		//				Policy.bind("GenerateSVNDiff.Changes"), resourcesToCommit, statusMap, null, true, toolbarControlCreator, syncInfoSet); //$NON-NLS-1$

		resourceSelectionTree = new SelectionTree(
				composite,
				SWT.NONE,
				Policy.bind("GenerateSVNDiff.Changes"), allResourcesInProject.toArray(new IResource[0]), /*new HashMap(),*/null, true, toolbarControlCreator, null); //$NON-NLS-1$
		// if (!resourceSelectionTree.showIncludeUnversionedButton())
		// includeAllButton.setVisible(false);

		resourceSelectionTree.setCustomOptions(getCustomOptions());
		// resourceSelectionTree.getTreeViewer().getTree().
		// resourceSelectionTree
		// .setRemoveFromViewValidator(new
		// ResourceSelectionTree.IRemoveFromViewValidator() {
		// public boolean canRemove(ArrayList resourceList,
		// IStructuredSelection selection) {
		// return removalOk(resourceList, selection);
		// }
		//
		// public String getErrorMessage() {
		// return removalError;
		//						//				return Policy.bind("CommitDialog.unselectedPropChangeChildren"); //$NON-NLS-1$ 	
		// }
		// });
		resourceSelectionTree.getTreeViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				selectedResources = resourceSelectionTree.getSelectedResources();
				getContainer().updateButtons();
			}
		});
		((CheckboxTreeViewer) resourceSelectionTree.getTreeViewer()).addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				selectedResources = resourceSelectionTree.getSelectedResources();
				getContainer().updateButtons();
			}
		});
		// resourceSelectionTree.getTreeViewer().addDoubleClickListener(new
		// IDoubleClickListener() {
		// public void doubleClick(DoubleClickEvent event) {
		// IStructuredSelection sel = (IStructuredSelection)
		// event.getSelection();
		// Object sel0 = sel.getFirstElement();
		// if (sel0 instanceof IFile) {
		// final ISVNLocalResource localResource =
		// SVNWorkspaceRoot.getSVNResourceFor((IFile) sel0);
		// try {
		//
		// setCompareInput(new SVNLocalCompareInput(localResource,
		// SVNRevision.BASE, true));
		// //showComparePane(true);
		// showCompareButton.setSelection(true);
		//
		// } catch (SVNException e1) {
		// }
		// }
		// }
		// });

		// if (!includeAll) {
		// resourceSelectionTree.removeUnversioned();
		// // ################################################################
		// // #TODO remove unmodifed files.
		// }

		// resourceSelectionTree.addUnversioned();
		// #FIXME this is stupid. make the diff set as the default.
		// #FIXME don't do this. this is stupid.
		resourceSelectionTree.removeUnmodified();
		this.includeAll = false;
		selectedResources = resourceSelectionTree.getSelectedResources();
	}

	private void toggleIncludeAll() {
		// resourceSelectionTree.removeUnversioned()
		// always show unversioned.
		// resourceSelectionTree.addUnversioned();
		// resourceSelectionTree.removeUnversioned();
		if (includeAll) {
			resourceSelectionTree.addUnmodified();
			// resourceSelectionTree.getTreeViewer().getTree();
			// ################################################################
			// #TODO remove unmodifed files.

		} else {
			resourceSelectionTree.removeUnmodified();
			// ################################################################
			// #TODO add unmodified
		}
		selectedResources = resourceSelectionTree.getSelectedResources();
		getContainer().updateButtons();
	}

	// public void showComparePane(boolean showCompare) {
	// this.showCompare = showCompare;
	// if (showCompare) {
	// horizontalSash.setMaximizedControl(null);
	// } else {
	// //horizontalSash.setMaximizedControl(verticalSash);
	// }
	//
	// }

	private Action[] getCustomOptions() {
		includeAllAction = new Action("Show all files", SWT.TOGGLE) {
			public void run() {
				includeAll = !includeAll;
				includeAllButton.setSelection(includeAll);
				toggleIncludeAll();
			}
		};
		includeAllAction.setChecked(includeAll);

		Action[] customOptionArray = new Action[] { includeAllAction };
		return customOptionArray;
	}

	// private void setCompareInput(final SVNLocalCompareInput input) {
	// try {
	// input.run(null);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// compareViewerPane.setInput(input.getCompareResult());
	// }

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		// if there are no local changes then show publish review request
		// directly.
		if ((selectedResources.length == 0 && !postCommitButton.getSelection()) || (context.getNewRevision() != -1 && initialUpdateAccess)) {
			setPostCommitReview();
			getWizard().getContainer().showPage(getNextPage());
			initialUpdateAccess = false;
		}

		// else if(context.getReviewType() == Const.REVIEW_POST_COMMIT) {
		// setPostCommitReview();
		// }
	}

	private void setPostCommitReview() {
		postCommitButton.setSelection(true);
		context.setReviewType(Const.REVIEW_POST_COMMIT);
		setEnableRecursively(resourceSelectionTree, false);
	}

	private void setPreCommitReview() {
		preCommitButton.setSelection(true);
		context.setReviewType(Const.REVIEW_PRE_COMMIT);
		setEnableRecursively(resourceSelectionTree, true);
	}

	private void initMetaData() {
		SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider.getProvider(context.getProject(),
				SVNProviderPlugin.getTypeId());

		Assert.isNotNull(svnProvider, "No " + SVNTeamProvider.class.getSimpleName() + " for " + context.getProject());

		ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(context.getProject());

		ReviewboardClientManager clientManager = ReviewboardCorePlugin.getDefault().getConnector().getClientManager();

		ReviewboardClient rbClient = null;
		Repository reviewBoardRepository = null;
		TaskRepository taskRepository = null;

		context.setSvnRepositoryLocation(projectSvnResource.getRepository());
		// setSvnRepositoryLocation(projectSvnResource.getRepository());

		Activator.getDefault().trace(TraceLocation.MAIN,
				"Local repository is " + context.getSvnRepositoryLocation().getRepositoryRoot().toString());

		List<String> clientUrls = clientManager.getAllClientUrl();
		if (clientUrls.isEmpty()) {
			// setMessage(
			// "No Reviewboard repositories are defined. Please add one using the Task Repositories view.",
			// IMessageProvider.WARNING);
			MessageDialog
					.openWarning(
							getShell(),
							"Review Request", "No Reviewboard repositories are defined. Please add one using the Task Repositories view."); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		boolean hasSvnRepos = false;

		for (String clientUrl : clientUrls) {

			TaskRepository repositoryCandidate = TasksUi.getRepositoryManager().getRepository(
					ReviewboardCorePlugin.REPOSITORY_KIND, clientUrl);

			if (repositoryCandidate == null) {
				Activator.getDefault().log(IStatus.WARNING, "No repository for clientUrl " + clientUrl + " skipping.");
				continue;
			}

			Activator.getDefault().trace(TraceLocation.MAIN,
					"Checking repository candidate " + repositoryCandidate.getRepositoryLabel());

			ReviewboardClient client = clientManager.getClient(repositoryCandidate);

			Activator.getDefault().trace(TraceLocation.MAIN, "Got reviewboardClient " + client);

			try {
				client.updateRepositoryData(false, new NullProgressMonitor());
			} catch (Exception e) {
				// #FIXME handle error
				e.printStackTrace();
			}

			Activator.getDefault().trace(
					TraceLocation.MAIN,
					"Refreshed repository data , got " + client.getClientData().getRepositories().size()
							+ " repositories.");

			for (Repository repository : client.getClientData().getRepositories()) {

				Activator.getDefault().trace(TraceLocation.MAIN,
						"Considering repository of type " + repository.getTool() + " and path " + repository.getPath());

				if (repository.getTool() != RepositoryType.Subversion)
					continue;

				hasSvnRepos = true;

				if (context.getSvnRepositoryLocation().getRepositoryRoot().toString().equals(repository.getPath())) {
					reviewBoardRepository = repository;
					taskRepository = repositoryCandidate;
					rbClient = client;
					break;
				}
			}
		}

		if (!hasSvnRepos) {
			setMessage("No Subversion repositories are defined in the configured ReviewBoard servers. Please add the correspoding repositories to ReviewBoard.");
			return;
		}

		context.setReviewboardClient(rbClient);
		context.setReviewboardRepository(reviewBoardRepository);
		context.setTaskRepository(taskRepository);

		try {
			context.getProject().accept(new IResourceProxyVisitor() {
				public boolean visit(IResourceProxy proxy) {
					switch (proxy.getType()) {
					case IResource.FILE:
						if (!proxy.getName().toString().startsWith("."))
							allResourcesInProject.add(proxy.requestResource());
						return false;
					case IResource.FOLDER:
						if (proxy.getName().toString().startsWith("."))
							return false;
					}
					return true;
				}
			}, IResource.NONE);

			// ChangedFileFinder changeFinder = new
			// ChangedFileFinder(projectSvnResource, getSvnRepositoryLocation()
			// .getSVNClient());
			// modifiedResourcesInProject = changeFinder.findChangedFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && context.getTaskRepository() != null
				&& context.getReviewboardRepository() != null
				&& (getSelectedFiles().length > 0 || postCommitButton.getSelection());
	}

	public IResource[] getSelectedFiles() {
		return selectedResources;
	}

	public boolean isPostCommitReview() {
		return postCommitButton.getSelection();
	}

}
