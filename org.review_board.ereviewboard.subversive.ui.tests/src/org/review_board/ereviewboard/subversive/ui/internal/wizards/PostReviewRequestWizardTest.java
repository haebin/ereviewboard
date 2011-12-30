package org.review_board.ereviewboard.subversive.ui.internal.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.svn.core.IConnectedProjectInformation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PostReviewRequestWizardTest {
	@Mock IProject project;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testFromProjectToSvnResource() { 
		IConnectedProjectInformation projectSvnResource = (IConnectedProjectInformation)RepositoryProvider.getProvider(project);
		//projectSvnResource.getRepositoryLocation().getRoot();
	}
}
