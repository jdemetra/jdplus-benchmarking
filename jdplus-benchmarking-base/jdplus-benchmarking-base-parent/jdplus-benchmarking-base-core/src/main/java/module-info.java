import jdplus.benchmarking.base.api.benchmarking.multivariate.MultivariateCholette;
import jdplus.benchmarking.base.api.benchmarking.univariate.Cholette;
import jdplus.benchmarking.base.api.benchmarking.univariate.CubicSpline;
import jdplus.benchmarking.base.api.benchmarking.univariate.Denton;
import jdplus.benchmarking.base.api.benchmarking.univariate.GrowthRatePreservation;
import jdplus.benchmarking.base.api.benchmarking.univariate.RawDenton;
import jdplus.benchmarking.base.api.calendarization.Calendarization;
import jdplus.benchmarking.base.core.benchmarking.multivariate.MultivariateCholetteProcessor;
import jdplus.benchmarking.base.core.benchmarking.univariate.CholetteProcessor;
import jdplus.benchmarking.base.core.benchmarking.univariate.CubicSplineProcessor;
import jdplus.benchmarking.base.core.benchmarking.univariate.DentonProcessor;
import jdplus.benchmarking.base.core.benchmarking.univariate.GRPProcessor;
import jdplus.benchmarking.base.core.benchmarking.univariate.RawDentonProcessor;
import jdplus.benchmarking.base.core.calendarization.CalendarizationProcessor;
import jdplus.toolkit.base.api.information.InformationExtractor;

module jdplus.benchmarking.base.core {

    requires static lombok;
    requires static nbbrd.design;
    requires static nbbrd.service;
    requires static org.checkerframework.checker.qual;

    requires transitive jdplus.benchmarking.base.api;
    requires jdplus.toolkit.base.api;
    requires jdplus.toolkit.base.core;

    exports jdplus.benchmarking.base.core.benchmarking.extractors;
    exports jdplus.benchmarking.base.core.benchmarking.multivariate;
    exports jdplus.benchmarking.base.core.benchmarking.univariate;
    exports jdplus.benchmarking.base.core.calendarization;
    exports jdplus.benchmarking.base.core.univariate;

    provides MultivariateCholette.Processor with
            MultivariateCholetteProcessor;

    provides InformationExtractor with
            jdplus.benchmarking.base.core.benchmarking.extractors.TemporalDisaggregationExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.RawTemporalDisaggregationExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.TemporalDisaggregationIExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.ADLExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.MarginalLikelihoodExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.ModelBasedDentonExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.RawModelBasedDentonExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.BenchmarkingResultsExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.CalendarizationExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.ResidualsDiagnosticsExtractor,
            jdplus.benchmarking.base.core.benchmarking.extractors.RawResidualsDiagnosticsExtractor;

    provides Calendarization.Processor with
            CalendarizationProcessor;

    provides CubicSpline.Processor with
            CubicSplineProcessor;

    provides Cholette.Processor with
            CholetteProcessor;

    provides Denton.Processor with
            DentonProcessor;

    provides RawDenton.Processor with
            RawDentonProcessor;
    
    provides GrowthRatePreservation.Processor with
            GRPProcessor;

}