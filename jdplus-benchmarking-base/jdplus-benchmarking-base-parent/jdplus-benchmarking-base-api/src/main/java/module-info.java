import jdplus.benchmarking.base.api.benchmarking.multivariate.MultivariateCholette;
import jdplus.benchmarking.base.api.benchmarking.univariate.Cholette;
import jdplus.benchmarking.base.api.benchmarking.univariate.CubicSpline;
import jdplus.benchmarking.base.api.benchmarking.univariate.Denton;
import jdplus.benchmarking.base.api.benchmarking.univariate.GrowthRatePreservation;
import jdplus.benchmarking.base.api.benchmarking.univariate.RawDenton;
import jdplus.benchmarking.base.api.calendarization.Calendarization;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregation;

module jdplus.benchmarking.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.api;

    exports jdplus.benchmarking.base.api.benchmarking;
    exports jdplus.benchmarking.base.api.benchmarking.multivariate;
    exports jdplus.benchmarking.base.api.benchmarking.univariate;
    exports jdplus.benchmarking.base.api.calendarization;
    exports jdplus.benchmarking.base.api.univariate;

    uses Denton.Processor;
    uses RawDenton.Processor;
    uses CubicSpline.Processor;
    uses MultivariateCholette.Processor;
    uses Calendarization.Processor;
    uses Cholette.Processor;
    uses TemporalDisaggregation.Processor;
    uses GrowthRatePreservation.Processor;
}