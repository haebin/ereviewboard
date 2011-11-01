package org.review_board.ereviewboard.subversive;

import java.util.ResourceBundle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {
	public static Test suite() {
		ResourceBundle bundle = TestPlugin.instance().getResourceBundle();
		boolean workbenchEnabled = "true".equals(bundle.getString("UI.WorkbenchEnabled"));

		TestSuite suite = new TestSuite("SVN Tests");

		
		//suite.addTestSuite(SVNTeamMoveDeleteHookTest.class);
		if (workbenchEnabled) {
			//suite.addTestSuite(RepositoryViewMenuEnablementTest.class);
			
		}
		//suite.addTestSuite(PLC312Test.class);
		return suite;
	}

	public void testAll() {
		AllTests.suite().run(this.createResult());
	}

}