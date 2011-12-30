package org.review_board.ereviewboard.subversive;

import java.io.File;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.svn.core.operation.remote.CreateFolderOperation;
import org.eclipse.team.svn.core.resource.ISVNStorage;
import org.eclipse.team.svn.core.svnstorage.SVNRemoteStorage;
import org.eclipse.team.svn.core.utility.FileUtility;
import org.eclipse.team.svn.core.utility.SVNUtility;

public class BasicTestCase extends TestWorkflow {
    
    public void setUp() throws Exception {
        //super.setUp();
        //new ShareNewProjectOperationTest() {}.testOperation();
        //new AddOperationTest() {}.testOperation();
        //new CommitOperationTest() {}.testOperation();
    }
    
    
    public void testSvn() throws Exception {
    	/*
    	ResourceBundle bundle = new TestPlugin().getResourceBundle();
		
		SVNRemoteStorage storage = SVNRemoteStorage.instance();
		HashMap preferences = new HashMap();
		preferences.put(ISVNStorage.PREF_STATE_INFO_LOCATION, TestPlugin.instance().getStateLocation());
		storage.initialize(preferences);
		
		this.location = storage.newRepositoryLocation();
		this.location.setUrl(bundle.getString("Repository.URL"));
		this.location.setTrunkLocation(bundle.getString("Repository.Trunk"));
		this.location.setBranchesLocation(bundle.getString("Repository.Branches"));
		this.location.setTagsLocation(bundle.getString("Repository.Tags"));
		this.location.setStructureEnabled(true);
		this.location.setLabel(bundle.getString("Repository.Label"));
		this.location.setUsername(bundle.getString("Repository.Username"));
		this.location.setPassword(bundle.getString("Repository.Password"));
		this.location.setPasswordSaved("true".equals(bundle.getString("Repository.SavePassword")));
		
		storage.addRepositoryLocation(this.location);
		this.location = storage.getRepositoryLocation(this.location.getId());
		
		this.deleteRepositoryNode(SVNUtility.getProposedTrunk(this.location));
		this.deleteRepositoryNode(SVNUtility.getProposedBranches(this.location));
		this.deleteRepositoryNode(SVNUtility.getProposedTags(this.location));
		
		CreateFolderOperation op = new CreateFolderOperation(this.location.getRoot(), this.location.getTrunkLocation(), "create trunk");
		op.run(new NullProgressMonitor());
		op = new CreateFolderOperation(this.location.getRoot(), this.location.getBranchesLocation(), "create branches");
		op.run(new NullProgressMonitor());
		op = new CreateFolderOperation(this.location.getRoot(),  this.location.getTagsLocation(), "createTags");
		op.run(new NullProgressMonitor());
	
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		
		root.delete(true, true, null);
		
		String demoDataLocation = TestPlugin.instance().getLocation() + bundle.getString("DemoData.Location") + "/";
		
		String prj1Name = bundle.getString("Project1.Name");
		String prj2Name = bundle.getString("Project2.Name");

		FileUtility.copyAll(root.getLocation().toFile(), new File(demoDataLocation + prj1Name), new NullProgressMonitor());
		FileUtility.copyAll(root.getLocation().toFile(), new File(demoDataLocation + prj2Name), new NullProgressMonitor());

		IProject prj = root.getProject(prj1Name);
		prj.create(null);
		prj.open(null);
		FileUtility.removeSVNMetaInformation(prj, new NullProgressMonitor());
		prj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		SVNRemoteStorage.instance().refreshLocalResources(new IResource[] {prj}, IResource.DEPTH_INFINITE);
		
		prj = root.getProject(prj2Name);
		prj.create(null);
		prj.open(null);
		FileUtility.removeSVNMetaInformation(prj, new NullProgressMonitor());
		prj.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		SVNRemoteStorage.instance().refreshLocalResources(new IResource[] {prj}, IResource.DEPTH_INFINITE);
		*/
    }
    
    
}
