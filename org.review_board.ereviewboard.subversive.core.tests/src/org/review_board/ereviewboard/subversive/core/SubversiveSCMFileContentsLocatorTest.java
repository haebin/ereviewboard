/*******************************************************************************
 * Copyright (c) 2011 Frederick Haebin Na and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Frederick Haebin Na
 *******************************************************************************/
package org.review_board.ereviewboard.subversive.core;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.review_board.ereviewboard.core.model.Repository;
import org.review_board.ereviewboard.subversive.core.internal.actions.SubversiveSCMFileContentsLocator;

/**
 * Test case for Subverisve. **PLEASE USE Run as JUnit Plug-in Test to run tests.**
 * 
 * @author Frederick Haebin Na
 */
public class SubversiveSCMFileContentsLocatorTest {
    private SubversiveSCMFileContentsLocator locator = new SubversiveSCMFileContentsLocator(); 
    
    @Test
    public void getFileContents() throws Exception {
        String fileContents = null;
        String fullResourceName = "/filedata/AllTests.txt";
        InputStream in = getClass().getResourceAsStream(fullResourceName);
        
        if ( in == null )
            throw new IOException("No resource : " + fullResourceName);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        try {
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;

            while ((line = bufferedReader.readLine()) != null)
                stringBuilder.append(line).append("\r\n");
            bufferedReader.close();

            fileContents = stringBuilder.toString();
        } finally {
            bufferedReader.close();
        }
        
        Repository codeRepository = new Repository();
        codeRepository.setPath("http://dev.eclipse.org/svnroot/technology/org.eclipse.subversive");
        String filePath = "/trunk/org.eclipse.team.svn.tests/src/org/eclipse/team/svn/tests/AllTests.java";
        String revision = "20000";
        
        locator.init(codeRepository, filePath, revision);
        byte[] res = locator.getContents(null);
        assertThat(fileContents, is(new String(res)));
    }
    
    @Test
    public void testSplit() {
        String tmp = " a (b) (c)";
        String[] arr = tmp.split("\\(");
        
        assertThat(arr.length, is(3));
        assertThat(arr[0], is(" a "));
        assertThat(arr[1], is("b) "));
        assertThat(arr[2], is("c)"));
    }
    
    @Test
    public void testRegex() throws Exception {
        String contents = "  AA\r\n BB\r\nCC  ";
        System.out.println(contents.replaceAll("\r\n", "\r\n+"));
        
        String filePath = File.createTempFile("test", "test").getCanonicalPath().replaceAll("\\\\", "/");
        System.out.println(filePath);
        
    }
}
