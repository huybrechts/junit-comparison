package jenkins.plugins.logzipper;

import hudson.model.Run;
import hudson.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

public class LogZipper {

	private static final Logger LOGGER = Logger.getLogger(LogZipper.class.getName());

	public static void zip(File logFile) {
			OutputStream os = null;
			try {
				File zippedLogFile = new File(logFile.getParentFile(), logFile.getName() + ".gz");
				LOGGER.fine("zipping " + logFile);
				os = new GZIPOutputStream(new FileOutputStream(zippedLogFile));
				IOUtils.copy(logFile, os);
				if (!logFile.delete()) {
					LOGGER.log(Level.WARNING, "failed to delete " + logFile);
					zippedLogFile.delete();
				}
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "failed to gzip " + logFile, e);
			} finally {
				IOUtils.closeQuietly(os);
			}
	}


}
