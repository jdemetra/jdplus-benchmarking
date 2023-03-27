module jdplus.benchmarking.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.benchmarking.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.benchmarking.base.core;
    requires jdplus.toolkit.base.r;

    exports demetra.benchmarking.r;
}