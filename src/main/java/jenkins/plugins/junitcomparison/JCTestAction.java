package jenkins.plugins.junitcomparison;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.Run;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.JUnitResultArchiver;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestDataPublisher;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResultAction;
import hudson.util.DescribableList;
import hudson.util.EditDistance;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;

@ExportedBean
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

    @ExportedBean
    public class RelatedTest {

        public final TestObject testObject;

        @Exported
        public CaseResult.Status status;

        @Exported
        public String url;

        public RelatedTest(TestObject testObject) {
            this.testObject = testObject;
        }

        @Exported
        public CaseResult.Status getStatus() {
            if (testObject instanceof CaseResult) {
                return ((CaseResult) testObject).getStatus();
            } else {
                return null;
            }
        }

        @Exported
        public String getUrl() {
            return Hudson.getInstance().getRootUrl() + testObject.getOwner().getUrl()+ "testReport" + testObject.getUrl();
        }

        @Exported
        public boolean isSimilar() {
            return testObject instanceof CaseResult && isSimilar((CaseResult) testObject, (CaseResult) object);
        }

        public boolean isSimilar(CaseResult a, CaseResult b) {
            int similarity = Integer.getInteger(getClass().getName() + ".similarity", 7);

            String errorA = a.getErrorDetails();
            String errorB = b.getErrorDetails();
            if (errorA == null || errorB == null) return false;

            int margin = Math.max(errorA.length(), errorB.length()) / similarity;

            return EditDistance.editDistance(errorA, errorB) < margin;
        }
    }


    @Exported(inline=true)
    public List<RelatedTest> getRelatedTests() {
        List<RelatedTest> result = new ArrayList<RelatedTest>();
        outer:
        for (AbstractProject project : Hudson.getInstance().getAllItems(AbstractProject.class)) {
            JUnitResultArchiver archiver = (JUnitResultArchiver) project.getPublishersList().get(JUnitResultArchiver.class);
            if (archiver == null) continue;


            List<? extends TestDataPublisher> testDataPublishers = archiver.getTestDataPublishers();
            if (testDataPublishers == null) continue;

            JCTestDataPublisher p = null;
            for (TestDataPublisher t: testDataPublishers) {
                if (t instanceof JCTestDataPublisher) {
                    p = (JCTestDataPublisher) t;
                    break;
                }
            }

            if (p == null) continue;

            if (object.getOwner().getProject() == project) continue;

            Run<?, ?> build = project.getLastCompletedBuild();
            if (build == null) continue;

            TestResultAction action = build.getAction(TestResultAction.class);
            if (action == null) continue;

            for (TestAction ta: action.getActions(object.getTestResult())) {
                if (ta instanceof JCData.BloomFilterTestAction) {
                    if (!((JCData.BloomFilterTestAction) ta).mayHaveTest(object)) {
                        continue outer;
                    }
                }
            }

            TestObject relatedTest = action.getResult().findCorrespondingResult(object.getId());
            if (relatedTest != null) result.add(new RelatedTest(relatedTest));
        }

        return result;
    }

    @ExportedBean
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
