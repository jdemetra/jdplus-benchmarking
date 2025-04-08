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
package jdplus.benchmarking.base.core.univariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import java.util.Map.Entry;
import jdplus.benchmarking.base.api.univariate.RawModelBasedDentonSpec;
import jdplus.toolkit.base.core.ssf.benchmarking.SsfCumulator;
import jdplus.benchmarking.base.core.benchmarking.univariate.BenchmarkingUtility;
import static jdplus.toolkit.base.api.data.AggregationType.Average;
import static jdplus.toolkit.base.api.data.AggregationType.First;
import static jdplus.toolkit.base.api.data.AggregationType.Last;
import static jdplus.toolkit.base.api.data.AggregationType.Sum;
import static jdplus.toolkit.base.api.data.AggregationType.UserDefined;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.normalizer.AbsMeanNormalizer;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.dk.DkToolkit;
import jdplus.toolkit.base.core.ssf.basic.Coefficients;
import jdplus.toolkit.base.core.ssf.basic.Loading;
import jdplus.toolkit.base.core.ssf.basic.Measurements;
import jdplus.toolkit.base.core.ssf.likelihood.DiffuseLikelihood;
import jdplus.toolkit.base.core.ssf.multivariate.IMultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.M2uAdapter;
import jdplus.toolkit.base.core.ssf.multivariate.MultivariateSsf;
import jdplus.toolkit.base.core.ssf.multivariate.SsfMatrix;
import jdplus.toolkit.base.core.ssf.univariate.DefaultSmoothingResults;
import jdplus.toolkit.base.core.ssf.univariate.ISsf;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.univariate.SsfData;

/**
 *
 * @author PALATEJ
 */
@lombok.experimental.UtilityClass
public class RawModelBasedDentonProcessor {

    public RawModelBasedDentonResults process(DoubleSeq aggregatedSeries, DoubleSeq indicator, int offset, RawModelBasedDentonSpec spec) {
        if (spec.getFixedBiRatios().isEmpty()) {
            return estimate(aggregatedSeries, indicator, offset, spec);
        } else {

        }
        return estimateWithConstraints(aggregatedSeries, indicator, offset, spec);
    }

    private RawModelBasedDentonResults estimate(DoubleSeq aggregationConstraint, DoubleSeq highFreqSeries, int offset, RawModelBasedDentonSpec spec) {
        int ratio = spec.getFrequencyRatio();

        DoubleSeq aggregationConstraintc, naggregationConstraint;
        switch (spec.getAggregationType()) {
            case Sum -> {
                aggregationConstraintc = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint, ratio, offset);
                naggregationConstraint = BenchmarkingUtility.highFreqConstraints(highFreqSeries, aggregationConstraintc, ratio, offset);
            }
            case Average -> {
                aggregationConstraintc = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint, ratio, offset);
                naggregationConstraint = BenchmarkingUtility.highFreqConstraints(highFreqSeries, aggregationConstraintc, ratio, offset).times(ratio);
            }
            case Last -> {
                aggregationConstraintc = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint, ratio, offset);
                naggregationConstraint = BenchmarkingUtility.highFreqConstraintsByPosition(highFreqSeries, aggregationConstraintc, ratio - 1, ratio, offset);
            }
            case First -> {
                aggregationConstraintc = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint, ratio, offset);
                naggregationConstraint = BenchmarkingUtility.highFreqConstraintsByPosition(highFreqSeries, aggregationConstraintc, 0, ratio, offset);
            }
            case UserDefined -> {
                aggregationConstraintc = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint, ratio, offset);
                naggregationConstraint = BenchmarkingUtility.highFreqConstraintsByPosition(highFreqSeries, aggregationConstraintc, spec.getObservationPosition(), ratio, offset);
            }
            default ->
                throw new IllegalArgumentException();
        }

        DataBlock x = DataBlock.of(highFreqSeries);
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        double fx = normalizer.normalize(x);
        ISsf ssf;
        ISsfLoading loading = Loading.regression(x);
        if (spec.getShockVariances().isEmpty()) {
            StateComponent c = Coefficients.timeVaryingCoefficients(DoubleSeq.of(1));
            StateComponent cc = SsfCumulator.of(c, loading, ratio, 0);
            ssf = Ssf.of(cc, SsfCumulator.defaultLoading(loading, ratio, 0));
        } else {
            double[] stde = new double[highFreqSeries.length()];
            for (int i = 0; i < stde.length; ++i) {
                stde[i] = 1;
            }
            for (Entry<Integer, Double> entry : spec.getShockVariances().entrySet()) {
                int idx = entry.getKey();
                if (idx >= 0 && idx < stde.length) {
                    stde[idx] = Math.sqrt(entry.getValue());
                }
            }
            StateComponent c = Coefficients.timeVaryingCoefficient(DoubleSeq.of(stde), 1);
            StateComponent cc = SsfCumulator.of(c, loading, ratio, 0);
            ssf = Ssf.of(cc, SsfCumulator.defaultLoading(loading, ratio, 0));
        }

        DefaultSmoothingResults srslts = DkToolkit.smooth(ssf, new SsfData(naggregationConstraint), true, true);
        DoubleSeq biratios = srslts.getComponent(1).times(fx);
        DoubleSeq ebiratios = srslts.getComponentVariance(1).fn(q -> q < 0 ? 0 : Math.sqrt(q) * fx);

        // Not optimal
        DiffuseLikelihood ll = DkToolkit.likelihood(ssf, new SsfData(naggregationConstraint), true, true);
        DoubleSeq e = ll.e();
        DoubleSeq aggregate = BenchmarkingUtility.aggregate(highFreqSeries, ratio, offset, spec.getAggregationType(), spec.getObservationPosition());
        DoubleSeq lbi = DoublesMath.divide(aggregationConstraintc, aggregate.range(0, aggregationConstraintc.length()));

        return RawModelBasedDentonResults.builder()
                .target(naggregationConstraint)
                .indicator(highFreqSeries)
                .aggregatedBiRatios(lbi)
                .disaggregatedSeries(DoublesMath.multiply(biratios, highFreqSeries))
                .stdevDisaggregatedSeries(DoublesMath.multiply(ebiratios, highFreqSeries.fn(z -> Math.abs(z))))
                .biRatios(biratios)
                .stdevBiRatios(ebiratios)
                .likelihood(ll.stats(0, 0))
                .residuals(e)
                .build();
    }

    private RawModelBasedDentonResults estimateWithConstraints(DoubleSeq aggregationConstraint, DoubleSeq highFreqSeries, int offset, RawModelBasedDentonSpec spec) {
        int ratio = spec.getFrequencyRatio();

        DoubleSeq naggregationConstraint;
        switch (spec.getAggregationType()) {
            case Sum, Average ->
                naggregationConstraint = BenchmarkingUtility.constraints(highFreqSeries, aggregationConstraint, ratio, offset);
            case Last ->
                naggregationConstraint = BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, ratio, offset, ratio - 1);
            case First ->
                naggregationConstraint = BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, ratio, offset, 0);
            case UserDefined ->
                naggregationConstraint = BenchmarkingUtility.constraintsByPosition(highFreqSeries, aggregationConstraint, ratio, offset, spec.getObservationPosition());
            default ->
                throw new IllegalArgumentException();
        }

        DataBlock x = DataBlock.of(highFreqSeries);
        AbsMeanNormalizer normalizer = new AbsMeanNormalizer();
        double fx = normalizer.normalize(x);
        ISsfLoading loading = Loading.regression(x);
        ISsfLoading cloading = SsfCumulator.defaultLoading(loading, ratio, offset);
        ISsfLoading floading = Loading.fromPosition(1);
        int n = naggregationConstraint.length();
        FastMatrix M = FastMatrix.make(n, 2);
        M.column(1).copy(naggregationConstraint);
        M.column(0).set(Double.NaN);
        spec.getFixedBiRatios().forEach((Integer d, Double v) -> {
            int del = offset + d;
            if (del >= 0 && del < M.getRowsCount()) {
                M.set(del, 0, v / fx);
            }
        });
        SsfMatrix Q = new SsfMatrix(M, 1);
        StateComponent cc;
        if (spec.getShockVariances().isEmpty()) {
            StateComponent c = Coefficients.timeVaryingCoefficients(DoubleSeq.of(1));
            cc = SsfCumulator.of(c, loading, ratio, 0);
        } else {
            double[] stde = new double[highFreqSeries.length()];
            for (int i = 0; i < stde.length; ++i) {
                stde[i] = 1;
            }
            for (Entry<Integer, Double> entry : spec.getShockVariances().entrySet()) {
                int idx = entry.getKey();
                if (idx >= 0 && idx < stde.length) {
                    stde[idx] = Math.sqrt(entry.getValue());
                }
            }
            StateComponent c = Coefficients.timeVaryingCoefficient(DoubleSeq.of(stde), 1);
            cc = SsfCumulator.of(c, loading, ratio, 0);
        }
        IMultivariateSsf ssf = new MultivariateSsf(cc, Measurements.of(new ISsfLoading[]{floading, cloading}, null));

        DefaultSmoothingResults srslts = DkToolkit.smooth(M2uAdapter.of(ssf), M2uAdapter.of(Q), true, true);
        DoubleSeq biratios = DoubleSeq.of(srslts.getComponent(1).extract(0, n, 2).times(fx).toArray());
        DoubleSeq ebiratios = DoubleSeq.of(srslts.getComponentVariance(1).extract(0, n, 2).fn(q -> q < 0 ? 0 : Math.sqrt(q) * fx).toArray());

        // Not optimal
        DiffuseLikelihood ll = DkToolkit.likelihood(M2uAdapter.of(ssf), M2uAdapter.of(Q), true, true);
        DoubleSeq e = ll.e();
        DoubleSeq aggregate = BenchmarkingUtility.aggregate(highFreqSeries, ratio, offset, spec.getAggregationType(), spec.getObservationPosition());
        DoubleSeq lbi = DoublesMath.divide(aggregationConstraint, aggregate);

        return RawModelBasedDentonResults.builder()
                .target(naggregationConstraint)
                .indicator(highFreqSeries)
                .aggregatedBiRatios(lbi)
                .disaggregatedSeries(DoublesMath.multiply(biratios, highFreqSeries))
                .stdevDisaggregatedSeries(DoublesMath.multiply(ebiratios, highFreqSeries.fn(z -> Math.abs(z))))
                .biRatios(biratios)
                .stdevBiRatios(ebiratios)
                .likelihood(ll.stats(0, 0))
                .residuals(e)
                .build();
    }

}
