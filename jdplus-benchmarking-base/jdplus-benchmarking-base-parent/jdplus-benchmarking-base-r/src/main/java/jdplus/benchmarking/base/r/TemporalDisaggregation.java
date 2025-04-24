/*
 * Copyright 2022 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.r;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.benchmarking.base.api.univariate.ModelBasedDentonSpec;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationIResults;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationISpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jdplus.benchmarking.base.api.univariate.ADLSpec;
import static jdplus.benchmarking.base.api.univariate.ADLSpec.DEF_EPS;
import static jdplus.benchmarking.base.api.univariate.ADLSpec.DEF_RESCALE;
import static jdplus.benchmarking.base.api.univariate.ADLSpec.builder;
import jdplus.benchmarking.base.api.univariate.AlgorithmSpec;
import jdplus.benchmarking.base.api.univariate.EstimationSpec;
import jdplus.benchmarking.base.api.univariate.ModelSpec;
import jdplus.benchmarking.base.api.univariate.RawDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.RawInterpolationSpec;
import jdplus.benchmarking.base.api.univariate.ResidualsModel;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationSpec;
import jdplus.benchmarking.base.api.univariate.TemporalInterpolationSpec;
import jdplus.benchmarking.base.api.univariate.TsEstimationSpec;
import jdplus.benchmarking.base.core.univariate.ADLProcessor;
import jdplus.benchmarking.base.core.univariate.ADLResults;
import jdplus.benchmarking.base.core.univariate.ModelBasedDentonProcessor;
import jdplus.benchmarking.base.core.univariate.ModelBasedDentonResults;
import jdplus.benchmarking.base.core.univariate.ProcessorI;
import jdplus.benchmarking.base.core.univariate.RawInterpolationProcessor;
import jdplus.benchmarking.base.core.univariate.RawDisaggregationProcessor;
import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationResults;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationProcessor;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.benchmarking.base.core.univariate.TemporalInterpolationProcessor;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.timeseries.TimeSelector;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.experimental.UtilityClass
public class TemporalDisaggregation {

    public TemporalDisaggregationIResults processI(TsData y, TsData indicator, String model, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho) {
        TemporalDisaggregationISpec spec = TemporalDisaggregationISpec.builder()
                .constant(true)
                .residualsModel(ResidualsModel.valueOf(model))
                .aggregationType(AggregationType.valueOf(aggregation))
                .observationPosition(obspos)
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .truncatedRho(truncatedRho)
                .build();
        return ProcessorI.process(y, indicator, spec);
    }

    public ModelBasedDentonResults processModelBasedDenton(TsData y, TsData indicator, int differencing, String aggregation, int obspos, String[] odates, double[] ovar, String[] fdates, double[] fval) {
        ModelBasedDentonSpec.Builder builder = ModelBasedDentonSpec.builder()
                .aggregationType(AggregationType.valueOf(aggregation));
        if (odates != null && ovar != null) {
            if (odates.length != ovar.length) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < odates.length; ++i) {
                builder.shockVariance(LocalDate.parse(odates[i], DateTimeFormatter.ISO_DATE), ovar[i]);
            }
        }
        if (fdates != null && fval != null) {
            if (fdates.length != fval.length) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < fdates.length; ++i) {
                builder.fixedBiRatio(LocalDate.parse(fdates[i], DateTimeFormatter.ISO_DATE), fval[i]);
            }
        }
        return ModelBasedDentonProcessor.process(y, indicator, builder.build());
    }

    public TemporalDisaggregationResults process(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String model, int freq, int nExt, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {
        AggregationType type = AggregationType.valueOf(aggregation);
        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();
        if (type == AggregationType.Average || type == AggregationType.Sum) {
            TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .algorithmSpec(aspec)
                    .average(type == AggregationType.Average)
                    .defaultPeriod(freq)
                    .build();
            if (indicators == null) {
                return TemporalDisaggregationProcessor.process(y, 0, nExt, spec);
            } else {
                for (int i = 0; i < indicators.length; ++i) {
                    indicators[i] = indicators[i].cleanExtremities();
                }
                return TemporalDisaggregationProcessor.process(y, indicators, spec);
            }
        } else {
            if (type == AggregationType.First) {
                obspos = 0;
            } else if (type == AggregationType.Last) {
                obspos = -1;
            }
            TemporalInterpolationSpec spec = TemporalInterpolationSpec.builder()
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .algorithmSpec(aspec)
                    .observationPosition(obspos)
                    .defaultPeriod(freq)
                    .build();
            if (indicators == null) {
                return TemporalInterpolationProcessor.process(y, 0, nExt, spec);
            } else {
                for (int i = 0; i < indicators.length; ++i) {
                    indicators[i] = indicators[i].cleanExtremities();
                }
                return TemporalInterpolationProcessor.process(y, indicators, spec);
            }
        }
    }

    public TemporalDisaggregationResults processDisaggregation(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String model, boolean average, double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {
        if (indicators == null) {
            throw new IllegalArgumentException("Indicators should not be null");
        }
        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .average(average)
                .build();
        for (int i = 0; i < indicators.length; ++i) {
            indicators[i] = indicators[i].cleanExtremities();
        }
        return TemporalDisaggregationProcessor.process(y, indicators, spec);
    }

    public TemporalDisaggregationResults processDisaggregation(TsData y, boolean constant, boolean trend,
            String model, int freq, boolean average,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs, int nbackcasts, int nforecasts) {
        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();
        TemporalDisaggregationSpec spec = TemporalDisaggregationSpec.builder()
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .average(average)
                .defaultPeriod(freq)
                .build();
        return TemporalDisaggregationProcessor.process(y, nbackcasts, nforecasts, spec);
    }

    public TemporalDisaggregationResults processInterpolation(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String model, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {

        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();
        if (indicators == null) {
            throw new IllegalArgumentException("Indicators should not be null");
        }

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();
        TemporalInterpolationSpec spec = TemporalInterpolationSpec.builder()
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .observationPosition(obspos)
                .build();
        return TemporalInterpolationProcessor.process(y, indicators, spec);

    }

    public TemporalDisaggregationResults processInterpolation(TsData y, boolean constant, boolean trend,
            String model, int freq, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs, int nbackcasts, int nforecasts) {

        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        TsEstimationSpec espec = TsEstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();

        TemporalInterpolationSpec spec = TemporalInterpolationSpec.builder()
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .observationPosition(obspos)
                .defaultPeriod(freq)
                .build();
        return TemporalInterpolationProcessor.process(y, nbackcasts, nforecasts, spec);

    }

    public RawTemporalDisaggregationResults processRaw(double[] y, boolean constant, boolean trend,
            String model, int frequencyRatio, int nbcasts, int nfcasts, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {

        AggregationType type = AggregationType.valueOf(aggregation);

        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();

        if (type == AggregationType.Average || type == AggregationType.Sum) {

            RawDisaggregationSpec spec = RawDisaggregationSpec.builder(frequencyRatio)
                    .frequencyRatio(frequencyRatio)
                    .average(type == AggregationType.Average)
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .algorithmSpec(aspec)
                    .build();
            return RawDisaggregationProcessor.process(DoubleSeq.of(y), nbcasts, nfcasts, spec);
        } else {
            RawInterpolationSpec spec = RawInterpolationSpec.builder(frequencyRatio)
                    .firstObservationPosition(obspos)
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .algorithmSpec(aspec)
                    .build();
            return RawInterpolationProcessor.process(DoubleSeq.of(y), nbcasts, nfcasts, spec);
        }
    }

    public RawTemporalDisaggregationResults processRaw(double[] y, boolean constant, boolean trend, Matrix indicators, int startOffset,
            String model, int frequencyRatio, String aggregation, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {

        if (indicators == null) {
            indicators = Matrix.empty();
        }
        if (startOffset < 0) {
            throw new IllegalArgumentException("Start offset should be non-negative");
        }
        FastMatrix X = FastMatrix.of(indicators);
        if (DoubleSeq.of(X.getStorage()).anyMatch(z -> !Double.isFinite(z))) {
            throw new IllegalArgumentException("indicators can't contain missing values");
        }
        AggregationType type = AggregationType.valueOf(aggregation);

        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();

        if (type == AggregationType.Average || type == AggregationType.Sum) {

            RawDisaggregationSpec spec = RawDisaggregationSpec.builder(frequencyRatio)
                    .frequencyRatio(frequencyRatio)
                    .average(type == AggregationType.Average)
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .algorithmSpec(aspec)
                    .build();
            return RawDisaggregationProcessor.process(DoubleSeq.of(y), X, startOffset, spec);
        } else {
            RawInterpolationSpec spec = RawInterpolationSpec.builder(frequencyRatio)
                    .firstObservationPosition(obspos)
                    .modelSpec(mspec)
                    .estimationSpec(espec)
                    .algorithmSpec(aspec)
                    .build();
            return RawInterpolationProcessor.process(DoubleSeq.of(y), X, startOffset, spec);
        }
    }

    public RawTemporalDisaggregationResults processRawDisaggregation(double[] y, boolean constant, boolean trend, Matrix indicators, int startOffset,
            String model, int frequencyRatio, boolean average,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {

        if (indicators == null) {
            indicators = Matrix.empty();
        }
        if (startOffset < 0) {
            throw new IllegalArgumentException("Start offset should be non-negative");
        }
        FastMatrix X = FastMatrix.of(indicators);
        if (DoubleSeq.of(X.getStorage()).anyMatch(z -> !Double.isFinite(z))) {
            throw new IllegalArgumentException("indicators can't contain missing values");
        }

        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();

        RawDisaggregationSpec spec = RawDisaggregationSpec.builder(frequencyRatio)
                .frequencyRatio(frequencyRatio)
                .average(average)
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .build();
        return RawDisaggregationProcessor.process(DoubleSeq.of(y), X, startOffset, spec);
    }

    public RawTemporalDisaggregationResults processRawDisaggregation(double[] y, boolean constant, boolean trend,
            String model, int frequencyRatio, boolean average,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs, int nbackcasts, int nforecasts) {

        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();

        RawDisaggregationSpec spec = RawDisaggregationSpec.builder(frequencyRatio)
                .frequencyRatio(frequencyRatio)
                .average(average)
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .build();
        return RawDisaggregationProcessor.process(DoubleSeq.of(y), nbackcasts, nforecasts, spec);
    }

    public RawTemporalDisaggregationResults processRawInterpolation(double[] y, boolean constant, boolean trend, Matrix indicators, int startOffset,
            String model, int frequencyRatio, int obspos,
            double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs) {

        if (indicators == null) {
            indicators = Matrix.empty();
        }
        if (startOffset < 0) {
            throw new IllegalArgumentException("Start offset should be non-negative");
        }
        FastMatrix X = FastMatrix.of(indicators);
        if (DoubleSeq.of(X.getStorage()).anyMatch(z -> !Double.isFinite(z))) {
            throw new IllegalArgumentException("indicators can't contain missing values");
        }
        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .residualsModel(ResidualsModel.valueOf(model))
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();

        RawInterpolationSpec spec = RawInterpolationSpec.builder(frequencyRatio)
                .firstObservationPosition(obspos)
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .build();
        return RawInterpolationProcessor.process(DoubleSeq.of(y), X, startOffset, spec);
    }

    public RawTemporalDisaggregationResults processRawInterpolation(double[] y, boolean constant, boolean trend,
            int frequencyRatio, int obspos, double rho, boolean fixedrho, double truncatedRho, boolean zeroinit,
            String algorithm, boolean diffuseregs, int nbackcasts, int nforecasts) {

        ModelSpec mspec = ModelSpec.builder()
                .constant(constant)
                .trend(trend)
                .parameter(fixedrho ? Parameter.fixed(rho) : Parameter.initial(rho))
                .diffuseRegressors(diffuseregs)
                .zeroInitialization(zeroinit)
                .build();

        EstimationSpec espec = EstimationSpec.builder()
                .truncatedParameter(truncatedRho <= -1 ? null : truncatedRho)
                .build();

        AlgorithmSpec aspec = AlgorithmSpec.builder()
                .algorithm(SsfInitialization.valueOf(algorithm))
                .rescale(true)
                .build();

        RawInterpolationSpec spec = RawInterpolationSpec.builder(frequencyRatio)
                .firstObservationPosition(obspos)
                .modelSpec(mspec)
                .estimationSpec(espec)
                .algorithmSpec(aspec)
                .build();
        return RawInterpolationProcessor.process(DoubleSeq.of(y), nbackcasts, nforecasts, spec);
    }

    public ADLResults processADL(TsData y, boolean constant, boolean trend, TsData[] indicators,
            String aggregation, double phi, boolean fixedphi, double truncatedPhi, String xar) {
        if (indicators == null) {
            return null;
        }
        ADLSpec spec = builder()
                .estimationSpan(TimeSelector.all())
                .aggregationType(AggregationType.valueOf(aggregation))
                .mean(constant)
                .trend(trend)
                .xar(ADLSpec.XAR.valueOf(xar))
                .phi(fixedphi ? Parameter.fixed(phi) : (Double.isFinite(phi) ? Parameter.initial(phi) : Parameter.undefined()))
                .truncation(truncatedPhi <= -1 ? null : truncatedPhi)
                .estimationPrecision(DEF_EPS)
                .rescale(DEF_RESCALE)
                //                .algorithm(SsfInitialization.valueOf(algorithm))
                .build();
        for (int i = 0; i < indicators.length; ++i) {
            indicators[i] = indicators[i].cleanExtremities();
        }
        return ADLProcessor.process(y, indicators, spec);

    }
}
