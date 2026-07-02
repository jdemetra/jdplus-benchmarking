module jdplus.benchmarking.base.r {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.jspecify;

    requires transitive jdplus.benchmarking.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;
    requires jdplus.benchmarking.base.core;
    requires jdplus.toolkit.base.r;

    exports jdplus.benchmarking.base.r;
    exports jdplus.benchmarking.base.r.util;
}