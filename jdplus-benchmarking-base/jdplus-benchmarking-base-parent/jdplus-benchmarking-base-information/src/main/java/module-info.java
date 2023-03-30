module jdplus.benchmarking.base.information {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.benchmarking.base.api;
    requires jdplus.toolkit.base.api;

    exports jdplus.benchmarking.base.information;
}