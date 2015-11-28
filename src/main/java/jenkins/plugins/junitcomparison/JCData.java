package jenkins.plugins.junitcomparison;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import hudson.model.AbstractBuild;
import hudson.tasks.junit.CaseResult;
import hudson.tasks.junit.ClassResult;
import hudson.tasks.junit.PackageResult;
import hudson.tasks.junit.TestAction;
import hudson.tasks.junit.TestObject;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JCData extends TestResultAction.Data {

    private BloomFilter<CharSequence> bloomFilter;

    private JCData() {
    }

    public JCData(TestResult testResult) {
        bloomFilter = BloomFilter.create(Funnels.stringFunnel(), testResult.getTotalCount(), 0.01);
        for (PackageResult pr : testResult.getChildren()) {
            bloomFilter.put(pr.getId());
            for (ClassResult cr : pr.getChildren()) {
                bloomFilter.put(cr.getId());
                for (CaseResult ccr : cr.getChildren()) {
                    bloomFilter.put(ccr.getId());
                }
            }
        }
    }

    @Override
    public List<TestAction> getTestAction(TestObject testObject) {
        AbstractBuild<?, ?> build = testObject.getOwner();
        TestAction result = null;
        if (build == build.getParent().getLastCompletedBuild()) {
            if (testObject instanceof TestResult && bloomFilter != null) {
                result = new BloomFilterTestAction(bloomFilter);
            } else if (testObject instanceof CaseResult) {
                result = new JCTestAction.Case(testObject);
            } else if (testObject instanceof PackageResult) {
                result = new JCTestAction.Package(testObject);
            } else if (testObject instanceof ClassResult) {
                result = new JCTestAction.Package(testObject);
            }
        }

        if (result != null) {
            return Arrays.asList(result);
        } else {
            return Collections.emptyList();
        }
    }

    public static class BloomFilterTestAction extends TestAction {

        private final BloomFilter<CharSequence> bloomFilter;

        public BloomFilterTestAction(BloomFilter<CharSequence> bloomFilter) {
            this.bloomFilter = bloomFilter;
        }

        public boolean mayHaveTest(TestObject test) {
            return bloomFilter == null || bloomFilter.mightContain(test.getId());
        }

        @Override
        public String getIconFileName() {
            return null;
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public String getUrlName() {
            return null;
        }
    }
}
