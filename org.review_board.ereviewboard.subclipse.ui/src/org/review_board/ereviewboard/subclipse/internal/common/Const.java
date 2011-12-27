package org.review_board.ereviewboard.subclipse.internal.common;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class Const {
	public static final String DEFAULT_ENCODING = "UTF-8";

	public static final String EOL = "\r\n";
	public static final String BULLET = "# ";

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

	public static final Pattern[] PATTERN_BUGID = { Pattern.compile("\\[([^\\].]*)\\]"),  Pattern.compile("#([a-zA-Z0-9]+)") };

	public static final int PAGING_LOG = 15;

	public static final int REVIEW_PRE_COMMIT = 0;
	public static final int REVIEW_POST_COMMIT = 1;

	public static final String COLUMN_DIV = "\t";
	public static final String CONTENTS_DIV = Const.EOL + Const.EOL + Const.EOL + "--------------------------------------------------"
			+ Const.EOL;
	public static final String INFO_POST_COMMIT = "Post-commit review for diff from revision ";
}
