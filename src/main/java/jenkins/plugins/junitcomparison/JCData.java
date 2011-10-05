package jenkins.plugins.junitcomparison;

import hudson.model.AbstractBuild;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResultAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JCData extends TestResultAction.Data {

    static final JCData INSTANCE = new JCData();

    @Override
    public List<TestAction> getTestAction(TestObject testObject) {
	AbstractBuild<?, ?> build = testObject.getOwner();
	if (build == build.getParent().getLastCompletedBuild()) {
	    JCTestAction result = (testObject instanceof CaseResult) ? new JCTestAction.Case(testObject) : new JCTestAction.Package(testObject);
	    return Arrays.<TestAction>asList(result);
	} else {
	    return Collections.emptyList();
	}
    }

    public Object readResolve() {
	return INSTANCE;
    }

}
