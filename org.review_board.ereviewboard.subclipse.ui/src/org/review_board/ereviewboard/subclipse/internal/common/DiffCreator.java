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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IResource;
import org.tigris.subversion.subclipse.core.ISVNLocalResource;
import org.tigris.subversion.subclipse.core.resources.SVNWorkspaceRoot;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 * The <tt>DiffCreator</tt> creates ReviewBoard-compatible diffs
 * 
 * <p>
 * Once specific problem with svn diff is that moved files have an incorrect
 * header.
 * </p>
 * 
 * @see <a
 *      href="https://github.com/reviewboard/rbtools/blob/release-0.3.4/rbtools/postreview.py#L1731">post-review
 *      handling of svn renames</a>
 * @author Robert Munteanu
 */
public class DiffCreator {
	private static String charset = Const.DEFAULT_ENCODING;
	private static final int NORMAL = 1;

	private static final String CLOSE = "\\ No newline at end of file";
	private static final String ADDED = "+";
	private static final String SEPARATOR = "=================================================================== ";
	private static final String RANGE = "@@ -0,0 +1,%d @@";
	private static final String OLD_FILE = "--- %s\t(revision 0)";
	private static final String NEW_FILE = "+++ %s\t(working copy)";
	private static final String INDEX = "Index: ";
	private static final Pattern LINE_COUNT = Pattern.compile(Const.EOL);
	private static final String REVIEW_REQ = "The author requested a review for this file without any changes. Please, review this by expanding it.";
	private static final String COMMENT_LANG = "/* %s */" + Const.EOL;

	// private static final String COMMENT_MARKUP = "<!-- %s -->" + EOL;

	// private static final String INDEX_MARKER = "Index:";

	public static byte[] createPatch(ISVNClientAdapter svnClient, SVNUrl url, SVNRevision oldRev, SVNRevision newRev)
			throws Exception {
		File netDiffs = File.createTempFile("netDiffs", ".txt");
		svnClient.diff(url, oldRev, newRev, netDiffs, true);
		return FileUtils.readFileToByteArray(netDiffs);
	}

	public static byte[] createPatch(IResource[] resources, File root, ISVNClientAdapter svnClient, String charset)
			throws Exception {
		DiffCreator.charset = charset;
		File rawDiffs = File.createTempFile("rawDiffs", ".txt");

		rawDiffs.deleteOnExit();

		BufferedWriter rawDiffsWriter = null;
		try {
			rawDiffsWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rawDiffs, true), charset));
			String contents = "";
			for (IResource resource : resources) {
				ISVNLocalResource svnResource = SVNWorkspaceRoot.getSVNResourceFor(resource);
				// #FIXME need to skip binary files. text only!
				// resource.getResourceAttributes().
				contents = "";
				// wut about deleted one? no present
				if (!svnResource.getStatus().isManaged() || svnResource.getStatus().isAdded()) {
					// create a full diff //svnResource.getStatus().isCopied();
					contents = createFullDiff(resource);

				} else if (svnResource.getStatus().getStatusKind().toInt() == NORMAL || svnResource.getStatus().getTextStatus().toInt() == NORMAL) {
					// add change to files. (add nill comment or blank spaces in
					// order to make it as a review target file)

					// svnResource.
					File file = resource.getRawLocation().toFile();
					markFileForReview(file);
					try {
						// // #FIXME if you set -Dfile.encoding=UTF-8 you don't
						// // need below code.
						// if(svnClient.getClass().toString().endsWith("SvnKitClientAdapter")){
						// svnResource.setSvnProperty("svnkit:charset", charset,
						// false);
						// }

						contents = createDiff(svnClient, file);
						// // #FIXME if you set -Dfile.encoding=UTF-8 you don't
						// // need below code.
						// if(svnClient.getClass().toString().endsWith("SvnKitClientAdapter")){
						// svnResource.deleteSvnProperty("svnkit:charset",
						// false);
						// }
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						// marking, charset
						svnClient.revert(file, false);
					}
				} else {
					File file = resource.getRawLocation().toFile();
					
					// // #FIXME if you set -Dfile.encoding=UTF-8 you don't need
					// // below code.
					// ISVNProperty orgProp = null;
					// if(svnClient.getClass().toString().endsWith("SvnKitClientAdapter")){
					// // store org prop
					// orgProp = svnResource.getSvnProperty("svnkit:charset");
					// svnResource.setSvnProperty("svnkit:charset", charset,
					// false);
					// }

					contents = createDiff(svnClient, file);
					// // #FIXME if you set -Dfile.encoding=UTF-8 you don't need
					// // below code.
					// if(svnClient.getClass().toString().endsWith("SvnKitClientAdapter")){
					// svnResource.deleteSvnProperty("svnkit:charset", false);
					// // restore org prop of svnkit:charset
					// if(orgProp != null)
					// svnResource.setSvnProperty(orgProp.getName(),
					// orgProp.getValue(), false);
					// }
				}
				rawDiffsWriter.write(contents);
				// rawDiffsWriter.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(rawDiffsWriter);
		}

		return cleanDiff(rawDiffs, root);
	}

	public static void markFileForReview(File file) throws Exception {
		String contents = FileUtils.readFileToString(file, charset);
		StringBuffer buffer = new StringBuffer();

		// String extension =
		// file.getName().substring(file.getName().lastIndexOf(".")).toLowerCase();
		buffer.append(String.format(COMMENT_LANG, REVIEW_REQ));
		buffer.append(contents);
		FileUtils.writeStringToFile(file, buffer.toString(), charset);
	}

	public static String createDiff(ISVNClientAdapter svnClient, File file) throws Exception {
		File tempFile = File.createTempFile("fileDiff", ".txt");
		tempFile.deleteOnExit();
		svnClient.diff(file, tempFile, false);
		// if(svnClient.getClass().toString().endsWith("SvnKitClientAdapter")){
		return FileUtils.readFileToString(tempFile, charset);
		// } else {
		// return FileUtils.readFileToString(tempFile);
		// }
	}

	public static String createFullDiff(IResource resource) throws Exception {
		StringBuffer buffer = new StringBuffer();
		String filePath = resource.getRawLocation().toString();

		buffer.append(INDEX).append(filePath).append(Const.EOL);
		buffer.append(SEPARATOR).append(Const.EOL);
		buffer.append(String.format(OLD_FILE, filePath)).append(Const.EOL);
		buffer.append(String.format(NEW_FILE, filePath)).append(Const.EOL);

		String fileContents = FileUtils.readFileToString(resource.getRawLocation().toFile(), charset).trim();
		if (fileContents.length() == 0)
			return "";

		int lineCount = 1; // add 1 extra for the last line
		Matcher m = LINE_COUNT.matcher(fileContents);
		while (m.find())
			lineCount++;

		buffer.append(String.format(RANGE, lineCount)).append(Const.EOL);
		buffer.append(ADDED).append(m.replaceAll("\r\n+")).append(Const.EOL);

		buffer.append(CLOSE).append(Const.EOL);

		return buffer.toString();
	}

	public static byte[] cleanDiff(File rawDiffs, File root) throws Exception {
		File allDiffs = File.createTempFile("allDiffs", ".txt");
		allDiffs.deleteOnExit();

		// House keeping starts
		BufferedReader reader = null;
		BufferedWriter writer = null;
		String rootPath = root.getAbsolutePath().replaceAll("\\\\", "/") + "/";
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(rawDiffs), charset));
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(allDiffs), charset));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(INDEX)) {
					writer.write(line.replaceFirst(rootPath, "") + Const.EOL);
				} else if (line.startsWith("---") || line.startsWith("+++")) {
					String[] arr = line.split("\\(");
					arr[0] = arr[0].replaceFirst(rootPath, "");

					String tmpLine = arr[0] + "(";
					if (arr.length == 3)
						tmpLine = tmpLine + arr[2];
					else
						tmpLine = tmpLine + arr[1];
					writer.write(tmpLine + Const.EOL);
				} else {
					writer.write(line + Const.EOL);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(writer);
		}
		return FileUtils.readFileToByteArray(allDiffs);
	}

// #TODO figure out why we need to handle copied resources. -_ -;; duh
//	public byte[] createDiffOld(IResource[] selectedFiles, File rootLocation, ISVNClientAdapter svnClient)
//			throws Exception {
//
//		File tmpFile = null;
//		try {
//			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//			// tmpFile = File.createTempFile("ereviewboard", "diff");
//			tmpFile = new File("C:/ereviewboard.diff");
//			// #FIXME
//			// boolean all = true;
//			// if(all) {
//			// createFullPatch(svnClient, selectedFiles, rootLocation, tmpFile,
//			// false);
//			//
//			// return FileUtils.readFileToByteArray(tmpFile);
//			// } else {
//			List<File> changes = new ArrayList<File>(selectedFiles.length);
//			Map<String, String> copies = new HashMap<String, String>();
//			for (IResource changed : selectedFiles) {
//				// #FIXME handle me please
//				// if (changedFile.getCopiedFromPathRelativeToProject() != null)
//				// copies.put(changedFile.getPathRelativeToProject(),
//				// changedFile.getCopiedFromPathRelativeToProject());
//				File changedFile = changed.getRawLocation().toFile();
//				changes.add(changedFile);
//			}
//
//			svnClient.createPatch(changes.toArray(new File[0]), rootLocation, tmpFile, false);
//
//			List<String> patchLines = FileUtils.readLines(tmpFile);
//			int replaceIndex = -1;
//			String replaceFrom = null;
//			String replaceTo = null;
//
//			for (int i = 0; i < patchLines.size(); i++) {
//
//				String line = patchLines.get(i);
//
//				if (line.toString().startsWith(INDEX_MARKER)) {
//					String file = line.substring(INDEX_MARKER.length()).trim();
//
//					String copiedTo = copies.get(file);
//					if (copiedTo != null) {
//						Activator.getDefault().trace(TraceLocation.DIFF,
//								"File " + file + " is copied to " + copiedTo + " .");
//						replaceIndex = i + 2;
//						replaceFrom = file;
//						replaceTo = copiedTo;
//					}
//				} else if (i == replaceIndex) {
//					line = line.replace(replaceFrom, replaceTo);
//				}
//
//				outputStream.write(line.getBytes());
//				outputStream.write('\n');
//			}
//			// }
//			return outputStream.toByteArray();
//		} finally {
//			// FileUtils.deleteQuietly(tmpFile);
//		}
//	}
}
