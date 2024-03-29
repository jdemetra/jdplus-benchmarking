module jdplus.benchmarking.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.benchmarking.base.api;

    exports jdplus.benchmarking.base.protobuf;
}