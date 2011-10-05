package jenkins.plugins.junitcomparison;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

public class JCTestDataPublisher extends TestDataPublisher {

    @DataBoundConstructor
    public JCTestDataPublisher() {
    }

    @Override
    public TestResultAction.Data getTestData(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener, TestResult testResult) throws IOException, InterruptedException {
	return JCData.INSTANCE;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<TestDataPublisher> {

	@Override
	public String getDisplayName() {
	    return "Compare test results with other projects";
	}
    }

}
