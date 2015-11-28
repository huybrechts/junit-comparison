package jenkins.plugins.logzipper;


import hudson.Extension;
import hudson.model.Run;
import hudson.model.listeners.RunListener;
import hudson.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

@Extension
public class LogZipperRunListener extends RunListener<Run<?,?>> {

	public static long MAX_SIZE = Long.getLong(LogZipperRunListener.class.getName() + ".MAX_SIZE", 25000000);

	private static final Logger LOGGER = Logger.getLogger(LogZipperRunListener.class.getName());

	@Override
	public void onFinalized(Run<?,?> run) {
		File logFile = run.getLogFile();
		if (logFile.exists() && logFile.length() > MAX_SIZE) {
			LOGGER.log(Level.INFO, String.format("gzipping %s since size %d > %d ", logFile, logFile.length(), MAX_SIZE));
			LogZipper.zip(logFile);
		}

        File attach = new File(run.getRootDir(), "junit-attachments");
        if (attach.exists()) {
            for (File f : attach.listFiles()) {
                if (f.isDirectory()) {
                    for (File g : f.listFiles()) {
                        if (g.getName().endsWith(".log")) {
                            LogZipper.zip(g);
                        }
                    }
                }
            }
        }
	}
}
