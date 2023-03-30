import jdplus.benchmarking.base.workspace.BenchmarkingHandlers;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;

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

    exports jdplus.benchmarking.base.workspace;

    provides FamilyHandler with
            BenchmarkingHandlers.DocDenton,
            BenchmarkingHandlers.DocCholette,
            BenchmarkingHandlers.DocTemporalDisaggregation;
}