package org.embulk.test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.embulk.EmbulkSystemProperties;
import org.embulk.config.TaskReport;
import org.embulk.exec.BulkLoader;
import org.embulk.exec.ExecutionResult;
import org.embulk.exec.ResumeState;
import org.embulk.spi.ExecInternal;
import org.embulk.spi.ExecSessionInternal;
import org.embulk.spi.Schema;
import org.slf4j.Logger;

class TestingBulkLoader extends BulkLoader {
    public TestingBulkLoader(final EmbulkSystemProperties embulkSystemProperties) {
        super(embulkSystemProperties);
    }

    @Override
    protected LoaderState newLoaderState(Logger logger, ProcessPluginSet plugins) {
        return new TestingLoaderState(logger, plugins);
    }

    protected static class TestingLoaderState extends LoaderState {
        public TestingLoaderState(Logger logger, ProcessPluginSet plugins) {
            super(logger, plugins);
        }

        @Override
        public ExecutionResult buildExecuteResultWithWarningException(Throwable ex) {
            ExecutionResult result = super.buildExecuteResultWithWarningException(ex);
            return new TestingExecutionResult(result, buildResumeState(ExecInternal.sessionInternal()), ExecInternal.sessionInternal());
        }
    }

    static class TestingExecutionResult extends ExecutionResult implements TestingEmbulk.RunResult {
        private final Schema inputSchema;
        private final Schema outputSchema;
        private final List<TaskReport> inputTaskReports;
        private final List<TaskReport> outputTaskReports;

        public TestingExecutionResult(ExecutionResult orig,
                ResumeState resumeState, ExecSessionInternal session) {
            super(orig.getConfigDiff(), orig.isSkipped(), orig.getIgnoredExceptions());
            this.inputSchema = resumeState.getInputSchema();
            this.outputSchema = resumeState.getOutputSchema();
            this.inputTaskReports = buildReports(resumeState.getInputTaskReports(), session);
            this.outputTaskReports = buildReports(resumeState.getOutputTaskReports(), session);
        }

        private static List<TaskReport> buildReports(List<Optional<TaskReport>> optionalReports, ExecSessionInternal session) {
            ImmutableList.Builder<TaskReport> reports = ImmutableList.builder();
            for (Optional<TaskReport> report : optionalReports) {
                reports.add(report.or(session.newTaskReport()));
            }
            return reports.build();
        }

        @Override
        public Schema getInputSchema() {
            return inputSchema;
        }

        @Override
        public Schema getOutputSchema() {
            return outputSchema;
        }

        @Override
        public List<TaskReport> getInputTaskReports() {
            return inputTaskReports;
        }

        @Override
        public List<TaskReport> getOutputTaskReports() {
            return outputTaskReports;
        }
    }
}
