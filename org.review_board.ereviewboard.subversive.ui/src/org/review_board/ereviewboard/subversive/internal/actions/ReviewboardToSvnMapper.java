package org.review_board.ereviewboard.subversive.internal.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.SVNTeamProvider;
import org.review_board.ereviewboard.core.ReviewboardDiffMapper;
import org.review_board.ereviewboard.core.model.FileDiff;
import org.review_board.ereviewboard.core.model.Repository;

/**
 * The <tt>ReviewboardToSvnMapper</tt> maps between various Reviewboard items
 * and their SVN correspondents
 * 
 * @author Robert Munteanu
 * 
 */
public class ReviewboardToSvnMapper {

	public IProject findProjectForRepository(Repository codeRepository,
			TaskRepository taskRepository, ReviewboardDiffMapper diffMapper) {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		List<IProject> candidates = new ArrayList<IProject>();

		for (IProject project : workspace.getRoot().getProjects()) {
			SVNTeamProvider svnProvider = (SVNTeamProvider) RepositoryProvider
					.getProvider(project, SVNTeamProvider.getProvider(project)
							.getID());

			if (svnProvider == null)
				continue;

			// ISVNLocalResource projectSvnResource =
			// SVNWorkspaceRoot.getSVNResourceFor(project);
			// String svnRepositoryPath =
			// projectSvnResource.getRepository().getRepositoryRoot().toString();
			try {
			if (codeRepository.getPath().equals(
					project.getDescription().getLocationURI().toString()))
				candidates.add(project);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (candidates.isEmpty())
			return null;

		if (candidates.size() == 1)
			return candidates.get(0);

		// multiple choice - use the latest diff revision to match based on
		// files
		for (IProject project : candidates) {

			Integer latestDiffRevisionId = diffMapper.getLatestDiffRevisionId();

			if (latestDiffRevisionId == null)
				break;

			// ISVNLocalResource projectSvnResource = SVNWorkspaceRoot.getSVNResourceFor(project);
			// SVNUrlUtils.getRelativePath(projectSvnResource.getRepository().getRepositoryRoot(), projectSvnResource.getUrl(), true);
			String projectRelativePath = project.getFullPath().toString();

			for (FileDiff fileDiff : diffMapper
					.getFileDiffs(latestDiffRevisionId.intValue()))
				if (!fileDiff.getDestinationFile().startsWith(
						projectRelativePath))
					break;

			return project;

		}
		return null;
	}

}
