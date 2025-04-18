/*
 * Copyright 2025 JDemetra+.
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.RawDisaggregationSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.normalizer.AbsMeanNormalizer;
import jdplus.toolkit.base.core.data.normalizer.DataNormalizer;
import jdplus.toolkit.base.core.data.transformation.Cumulator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import lombok.NonNull;
import org.checkerframework.checker.index.qual.NonNegative;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
public class RawDisaggregationModelBuilder {

    // original series
    private final DoubleSeq y;
    // original series transformed to the highest-frequency 
    private DoubleSeq ho;
    // (rescaled) series transformed to the highest-frequency  (same as ho but with rescaling)
    private DataBlock hy;

    // (rescaled) regressors and cumulated regressors 
    private final FastMatrix X;
    private FastMatrix Xc;

    // offset between the start of X, hY and the start of the first highfreq period of y in the case of disaggregation
    // or between the position of the first X, hY and the position of the first y in the case of interpolation 
    private final int startOffset;

    // range (relative to hy) used to estimate the regression.
    // Only complete periods are considered,
    private int estimationStart, estimationEnd;

    /**
     * Scaling factor for y
     */
    private double yfactor;
    /**
     * Scaling factors for X
     */
    private double[] xfactors;

    // Ratio between high-frequency and low-frequency
    private final int ratio;
    private final boolean average;

    public static RawDisaggregationModelBuilder of(@NonNull DoubleSeq y, @NonNull FastMatrix regressors, @NonNegative int startOffset, @NonNull RawDisaggregationSpec spec) {
        if (regressors.isEmpty() && startOffset != 0) {
            throw new IllegalArgumentException();
        }
        if (regressors.isEmpty() && !spec.getModelSpec().isConstant() && !spec.getModelSpec().isTrend()) {
            return new RawDisaggregationModelBuilder(y, spec, 0, 0);
        } else {
            return new RawDisaggregationModelBuilder(y, regressors, startOffset, spec);
        }
    }

    public RawDisaggregationModelBuilder(@NonNull DoubleSeq y, @NonNull FastMatrix regressors, @NonNegative int startOffset, @NonNull RawDisaggregationSpec spec) {
        this.y = y;
        this.startOffset = startOffset;
        ratio = spec.getFrequencyRatio();
        if (ratio == 0) {
            throw new IllegalArgumentException("Disaggregation ratio should be specified");
        }
        if (regressors.isEmpty()) {
            throw new IllegalArgumentException("At least one regressor should be specified");
        }
        average = spec.getAggregationType() == AggregationType.Average;
        hy = buildHY(regressors.getRowsCount());
        X = buildX(regressors, y.length() * ratio, 0, 0, spec.getModelSpec().isConstant(), spec.getModelSpec().isTrend());
        buildXc();

        estimationStart = startOffset + spec.getEstimationSpec().getEstimationRange().getStart() * ratio;
        int nxy = (X.getRowsCount() - startOffset) / ratio;
        int ny = Math.min(y.length(), nxy);
        ny = spec.getEstimationSpec().getEstimationRange().isEmpty() ? ny : Math.min(ny, spec.getEstimationSpec().getEstimationRange().size());
        estimationEnd = estimationStart + ny * ratio;

        scale(spec.getAlgorithmSpec().isRescale() ? new AbsMeanNormalizer() : null);
    }

    public RawDisaggregationModelBuilder(@NonNull DoubleSeq y, @NonNull RawDisaggregationSpec spec, int nBackcasts, int nForecasts) {
        this.y = y;
        startOffset = nBackcasts;
        average = spec.getAggregationType() == AggregationType.Average;
        ratio = spec.getFrequencyRatio();
        if (ratio == 0) {
            throw new IllegalArgumentException("Disaggregation ratio should be specified");
        }
        int nhy = y.length() * ratio + nBackcasts + nForecasts;
        hy = buildHY(nhy);
        X = buildX(nhy, spec.getModelSpec().isConstant(), spec.getModelSpec().isTrend());
        buildXc();

        estimationStart = nBackcasts + spec.getEstimationSpec().getEstimationRange().getStart() * ratio;

        int ny = spec.getEstimationSpec().getEstimationRange().isEmpty() ? y.length() : Math.min(y.length(), spec.getEstimationSpec().getEstimationRange().getEnd());
        estimationEnd = nBackcasts + ny * ratio;

        if (estimationEnd <= estimationStart) {
            throw new IllegalArgumentException("Not enough data");
        }

        scale(spec.getAlgorithmSpec().isRescale() ? new AbsMeanNormalizer() : null);
    }

    RawDisaggregationModel build() {
        return new RawDisaggregationModel(this);
    }

    private DataBlock buildHY(int nx) {
        int ny = y.length();
        double[] dy = new double[nx];
        for (int i = 0; i < dy.length; ++i) {
            dy[i] = Double.NaN;
        }

        DoubleSeqCursor reader = y.cursor();
        for (int j = startOffset + ratio - 1, i = 0; i < ny && j<nx; ++i, j += ratio) {
            dy[j] = reader.getAndNext();
        }
        return DataBlock.of(dy);
    }

    private static FastMatrix buildX(FastMatrix regressors, int length, int nBackcasts, int nForecasts, boolean constant, boolean trend) {
        if (!constant && !trend) {
            return regressors.deepClone();
        }
        int nx = regressors.isEmpty() ? 0 : regressors.getColumnsCount();
        int n = nx;
        if (constant) {
            ++n;
        }
        if (trend) {
            ++n;
        }
        int m = regressors.isEmpty() ? length : regressors.getRowsCount();

        FastMatrix all = FastMatrix.make(m, n);
        DataBlockIterator xcols = all.columnsIterator();
        if (constant) {
            xcols.next().set(1);
        }
        if (trend) {
            xcols.next().set(i -> i);
        }
        if (nx > 0) {
            DataBlockIterator rcols = regressors.columnsIterator();
            while (rcols.hasNext()) {
                xcols.next().copy(rcols.next());
            }
        }
        return all;
    }

    private static FastMatrix buildX(int length, boolean constant, boolean trend) {
        int n = 0;
        if (constant) {
            ++n;
        }
        if (trend) {
            ++n;
        }
        if (n == 0) {
            return FastMatrix.EMPTY;
        }

        FastMatrix all = FastMatrix.make(length, n);
        DataBlockIterator xcols = all.columnsIterator();
        if (constant) {
            xcols.next().set(1);
        }
        if (trend) {
            xcols.next().set(i -> i);
        }
        return all;
    }

    private void buildXc() {
        if (X.isEmpty()) {
            Xc = FastMatrix.EMPTY;
            return;
        }
        Xc = X.deepClone();
        Cumulator cumul = new Cumulator(ratio);
        DataBlockIterator cXc = Xc.columnsIterator();
        while (cXc.hasNext()) {
            cumul.transform(cXc.next().drop(startOffset, 0));
        }
    }

    private void scale(DataNormalizer normalizer) {
        if (normalizer != null) {
            ho = DoubleSeq.of(hy.toArray());
            yfactor = normalizer.normalize(hy);
        } else {
            ho = hy;
            yfactor = 1;
        }
        if (Xc.isEmpty()) {
            return;
        }

        int nx = Xc.getColumnsCount();
        xfactors = new double[nx];

        if (normalizer != null) {
            DataBlockIterator cols = X.columnsIterator();
            DataBlockIterator ccols = Xc.columnsIterator();
            int i = 0;
            while (cols.hasNext()) {
                double z = normalizer.normalize(cols.next());
                ccols.next().mul(z);
                xfactors[i++] = z;
            }
        } else {
            for (int i = 0; i < xfactors.length; ++i) {
                xfactors[i] = 1;
            }
        }
    }

}
