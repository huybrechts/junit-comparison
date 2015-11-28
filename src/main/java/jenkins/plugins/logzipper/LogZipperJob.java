package jenkins.plugins.logzipper;

import hudson.Extension;
import hudson.model.AsyncPeriodicWork;
import hudson.model.Hudson;
import hudson.model.Job;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;


@Extension
public class LogZipperJob extends AsyncPeriodicWork {
	public static long AGE = Long.getLong(LogZipperJob.class.getName() + ".AGE", 30 * DAY);

    private static final Logger LOG = Logger.getLogger(LogZipper.class.getName());

	public static volatile boolean DISABLE = false;

	public LogZipperJob() {
		super("Daily log zipper");
	}

	@Override
	protected void execute(TaskListener taskListener) throws IOException, InterruptedException {
		for (Job<?,?> job: Hudson.getInstance().getItems(Job.class)) {
			for (Run<?,?> run: job.getBuilds()) {
                if (Thread.interrupted()) throw new InterruptedException();

				if (DISABLE) return; //escape hatch in case of runtime issues

				if (!run.isLogUpdated() && isTooOld(run.getTime())) {
					taskListener.getLogger().print("Handling " + run);
					final File logFile = run.getLogFile();
					if (logFile.exists() && logFile.getName().equals("log")) {
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
		}
	}

	@Override
	public long getRecurrencePeriod() {
		return AGE >= 7 * DAY ? DAY : HOUR;
	}

	public static boolean isTooOld(Date date) {
        long age = System.currentTimeMillis() - date.getTime();
        return age > AGE && age < 3 * AGE;
        // we don't look at builds that are more than thrice the AGE, assuming they have been processed already.
	}
}
