module jdplus.benchmarking.base.api {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires jdplus.toolkit.base.api;

    exports demetra.benchmarking;
    exports demetra.benchmarking.multivariate;
    exports demetra.benchmarking.univariate;
    exports demetra.calendarization;
    exports demetra.tempdisagg.univariate;

    uses demetra.benchmarking.univariate.Denton.Processor;
    uses demetra.benchmarking.univariate.CubicSpline.Processor;
    uses demetra.benchmarking.multivariate.MultivariateCholette.Processor;
    uses demetra.calendarization.Calendarization.Processor;
    uses demetra.benchmarking.univariate.Cholette.Processor;
    uses demetra.tempdisagg.univariate.TemporalDisaggregation.Processor;
    uses demetra.benchmarking.univariate.GrowthRatePreservation.Processor;
}