package org.review_board.ereviewboard.subclipse.internal;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class DiffCreatorTest {
    @Test
    public void decodeBrokenEncodingFile() throws Exception {
        String fullResourceName = "/diffdata/brokenencoding.diff.txt";
        String str = IOUtils.toString(getClass().getResourceAsStream(fullResourceName), "ISO-8859-1");
        //String str = IOUtils.toString(getClass().getResourceAsStream(fullResourceName), "UTF-8");
        //str = new String(str.getBytes(), "UTF-8");
        System.out.println(str);
    }
    
    @Test
    public void decodeBrokenEncodingFile1() throws Exception {
        String fullResourceName = "/diffdata/ereviewboard.01.diff.txt";
        String str = IOUtils.toString(getClass().getResourceAsStream(fullResourceName));
        System.out.println(str);
        //String str = IOUtils.toString(getClass().getResourceAsStream(fullResourceName), "UTF-8");
        str = new String(str.getBytes("UTF-8"), "UTF-8");
        System.out.println(str);
    }
}
