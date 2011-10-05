package jenkins.plugins.junitcomparison;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResultAction;

import java.util.ArrayList;
import java.util.List;

public class JCTestAction extends TestAction {

    private TestObject object;

    public JCTestAction(TestObject object) {
	this.object = object;
    }

    public String getIconFileName() {
	return null;
    }

    public String getDisplayName() {
	return null;
    }

    public String getUrlName() {
	return "comparison";
    }

    public List<TestObject> getRelatedTests() {
	List<TestObject> result = new ArrayList<TestObject>();
	for (AbstractProject project : Hudson.getInstance().getAllItems(AbstractProject.class)) {
	    JUnitResultArchiver archiver = (JUnitResultArchiver) project.getPublishersList().get(JUnitResultArchiver.class);
	    if (archiver == null) continue;

	    if (archiver.getTestDataPublishers().get(JCTestDataPublisher.class) == null) continue;

	    if (object.getOwner().getProject() == project) continue;

	    Run<?, ?> build = project.getLastCompletedBuild();
	    if (build == null) continue;

	    TestResultAction action = build.getAction(TestResultAction.class);
	    if (action == null) continue;

	    TestObject relatedTest = action.getResult().findCorrespondingResult(object.getId());
	    if (relatedTest != null) result.add(relatedTest);
	}

	return result;
    }

    public static class Case extends JCTestAction {
	public Case(TestObject object) {
	    super(object);
	}
    }
    public static class Package extends JCTestAction {
	public Package(TestObject object) {
	    super(object);
	}
    }

}
