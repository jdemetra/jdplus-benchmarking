module jdplus.benchmarking.base.workspace {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.benchmarking.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.benchmarking.base.information;
    requires jdplus.toolkit.base.information;
    requires jdplus.toolkit.base.workspace;
    requires jdplus.benchmarking.base.core;

    exports demetra.benchmarking.workspace;

    provides demetra.workspace.file.spi.FamilyHandler with
            demetra.benchmarking.workspace.BenchmarkingHandlers.DocDenton,
            demetra.benchmarking.workspace.BenchmarkingHandlers.DocCholette,
            demetra.benchmarking.workspace.BenchmarkingHandlers.DocTemporalDisaggregation;
}