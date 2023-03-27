module jdplus.benchmarking.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.benchmarking.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;

    exports jdplus.benchmarking.extractors;
    exports jdplus.benchmarking.multivariate;
    exports jdplus.benchmarking.univariate;
    exports jdplus.calendarization;
    exports jdplus.tempdisagg.univariate;

    provides demetra.benchmarking.multivariate.MultivariateCholette.Processor with
            jdplus.benchmarking.multivariate.MultivariateCholetteProcessor;

    provides demetra.information.InformationExtractor with
            jdplus.benchmarking.extractors.TemporalDisaggregationExtractor,
            jdplus.benchmarking.extractors.TemporalDisaggregationIExtractor,
            jdplus.benchmarking.extractors.ModelBasedDentonExtractor,
            jdplus.benchmarking.extractors.BenchmarkingResultsExtractor,
            jdplus.benchmarking.extractors.CalendarizationExtractor,
            jdplus.benchmarking.extractors.ResidualsDiagnosticsExtractor;

    provides demetra.calendarization.Calendarization.Processor with
            jdplus.calendarization.CalendarizationProcessor;

    provides demetra.benchmarking.univariate.CubicSpline.Processor with
            jdplus.benchmarking.univariate.CubicSplineProcessor;

    provides demetra.benchmarking.univariate.Cholette.Processor with
            jdplus.benchmarking.univariate.CholetteProcessor;

    provides demetra.benchmarking.univariate.Denton.Processor with
            jdplus.benchmarking.univariate.DentonProcessor;

    provides demetra.benchmarking.univariate.GrowthRatePreservation.Processor with
            jdplus.benchmarking.univariate.GRPProcessor;
}