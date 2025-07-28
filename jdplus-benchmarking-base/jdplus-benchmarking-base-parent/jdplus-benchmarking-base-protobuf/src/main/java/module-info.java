module jdplus.benchmarking.base.protobuf {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.benchmarking.base.api;

    exports jdplus.benchmarking.base.protobuf;
}