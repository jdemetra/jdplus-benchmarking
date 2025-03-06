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

import java.util.ArrayList;
import jdplus.benchmarking.base.api.univariate.RawTemporalDisaggregationSpec;
import jdplus.toolkit.base.api.data.AggregationType;
import static jdplus.toolkit.base.api.data.AggregationType.Average;
import static jdplus.toolkit.base.api.data.AggregationType.First;
import static jdplus.toolkit.base.api.data.AggregationType.Last;
import static jdplus.toolkit.base.api.data.AggregationType.Sum;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoubleSeqCursor;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.data.normalizer.AbsMeanNormalizer;
import jdplus.toolkit.base.core.data.normalizer.DataNormalizer;
import jdplus.toolkit.base.core.data.transformation.Cumulator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import lombok.NonNull;

/**
 *
 * @author Jean Palate
 */
@lombok.Getter
public class RawDisaggregationModelBuilder {

    private final DoubleSeq y;
    // original series transformed to the highest-frequency 
    private DoubleSeq ho;
    // (rescaled) series transformed to the highest-frequency  (same as ho without rescaling)
    private DataBlock hy;

    // (rescaled) regressors and cumulated regressors 
    private final FastMatrix X;
    private FastMatrix Xc;
    // X, Xc and y (+ ho, hy) start at the same low-frequency period (aggregation
    // period).
    // They should have been completed with missing values to achieve that goal
    // The spans of y and of X (if any) could differ. 
    private int start, end;

    // range used to estimate the regression.
    // In the case of disaggregation, only complete periods are considered,
    // which is not the case in interpolation (we use all information).
    // Periods containing missing values (low or high-frequency) at the beginning or at the are excluded.
    // Missing values inside the series themselves are not allowed. That restriction will be removed in the fututre (TODO)
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
    private final AggregationType aType;
    private final int yposition; // only used in custom interpolation
    
    // variables name
    private ArrayList<String> varNames = new ArrayList<>();

    public RawDisaggregationModelBuilder(@NonNull DoubleSeq y, @NonNull FastMatrix regressors, @NonNull RawTemporalDisaggregationSpec spec) {
        this.y = y;
        ratio = spec.getDisaggregationRatio();
        if (ratio == 0) {
            throw new IllegalArgumentException("Disaggregation ratio should be specified");
        }
        int pos;
        aType = spec.getAggregationType();
        switch (aType) {
            case Sum, Average, Last ->
                pos = ratio - 1;
            case First ->
                pos = 0;
            default ->
                pos = spec.getObservationPosition();
        }
        yposition = pos;
        buildHY();
        X = buildX(regressors, hy.length(), spec.isConstant(), spec.isTrend());
        buildXc();
        start = findStart();
        end = findEnd();
        estimationStart = findEstimationStart();
        estimationEnd = findEstimationEnd();

        scale(spec.isRescale() ? new AbsMeanNormalizer() : null);
               
        if (spec.isConstant()) {
            varNames.add("C");
        }
        if (spec.isTrend()) {
            varNames.add("Trend");
        }
        for (int i = 0; i < regressors.getColumnsCount(); ++i) {
            varNames.add("var" + (i + 1));
        }
    }
    
    public RawDisaggregationModelBuilder(@NonNull DoubleSeq y, @NonNull RawTemporalDisaggregationSpec spec) {
        this.y = y;
        ratio = spec.getDisaggregationRatio();
        if (ratio == 0) {
            throw new IllegalArgumentException("Disaggregation ratio should be specified");
        }
        int pos;
        aType = spec.getAggregationType();
        switch (aType) {
            case Sum, Average, Last ->
                pos = ratio - 1;
            case First ->
                pos = 0;
            default ->
                pos = spec.getObservationPosition();
        }
        yposition = pos;
        buildHY();
        X = buildX(FastMatrix.EMPTY, hy.length(), spec.isConstant(), spec.isTrend());
        buildXc();
        start = findStart();
        end = findEnd();
        estimationStart = findEstimationStart();
        estimationEnd = findEstimationEnd();

        scale(spec.isRescale() ? new AbsMeanNormalizer() : null);
        
        if (spec.isConstant()) {
            varNames.add("C");
        }
        if (spec.isTrend()) {
            varNames.add("Trend");
        }
    }

    RawDisaggregationModel build() {
        return new RawDisaggregationModel(this);
    }

    private void buildHY() {
        int ny = y.length();
        double[] dy = new double[ny * ratio];
        for (int i = 0; i < dy.length; ++i) {
            dy[i] = Double.NaN;
        }

        DoubleSeqCursor reader = y.cursor();
        for (int j = yposition, i = 0; i < ny; ++i, j += ratio) {
            dy[j] = reader.getAndNext();
        }
        hy = DataBlock.of(dy);
    }

    private static FastMatrix buildX(FastMatrix regressors, int length, boolean constant, boolean trend) {
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

    private void buildXc() {        
        if (X.isEmpty()) {
            Xc = FastMatrix.EMPTY;
            return;
        }
        if (aType != AggregationType.Average && aType != AggregationType.Sum) {
            Xc = X;
        } else {
            Xc = X.deepClone();
            Cumulator cumul = new Cumulator(ratio);
            DataBlockIterator cXc = Xc.columnsIterator();
            while (cXc.hasNext()) {
                cumul.transform(cXc.next());
            }
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

    private int findStart() {
        if (Xc.isEmpty()) {
            return 0;
        }
        DataBlockIterator rows = Xc.rowsIterator();
        int pos = 0;
        while (rows.hasNext()) {
            if (rows.next().allMatch(x -> Double.isFinite(x))) {
                return pos;
            }
            ++pos;
        }
        return pos;
    }

    private int findEnd() {
        if (Xc.isEmpty()) {
            return hy.length();
        }
        DataBlockIterator rows = Xc.reverseRowsIterator();
        int pos = Xc.getRowsCount();
        while (rows.hasNext()) {
            if (rows.next().allMatch(x -> Double.isFinite(x))) {
                return pos;
            }
            --pos;
        }
        return pos;
    }

    private int findEstimationStart() {
        int ystart = estimationStart(y, ratio, yposition, aType);
        if (Xc.isEmpty()) {
            return ystart;
        }
        DataBlockIterator rows = Xc.rowsIterator();
        int xstart = 0;
        while (rows.hasNext()) {
            if (rows.next().allMatch(x -> Double.isFinite(x))) {
                break;
            }
            ++xstart;
        }
        if (aType == AggregationType.Average || aType == AggregationType.Sum) { // we must have full periods
            int tmp = xstart % ratio;
            if (tmp != 0) {
                xstart += ratio - tmp;
            }
        }
        return Math.max(ystart, xstart);
    }

    private int findEstimationEnd() {
        int yend = estimationEnd(y, ratio, yposition, aType);
        if (Xc.isEmpty()) {
            return yend;
        }
        DataBlockIterator rows = Xc.reverseRowsIterator();
        int xend = Xc.getRowsCount();
        while (rows.hasNext()) {
            if (rows.next().allMatch(x -> Double.isFinite(x))) {
                break;
            }
            --xend;
        }
        if (aType == AggregationType.Average || aType == AggregationType.Sum) { // we must have full periods
            xend -= xend % ratio;
        }
        return Math.min(xend, yend);
    }

    private static int first(DoubleSeq z) {
        DoubleSeqCursor reader = z.cursor();
        int l = z.length();
        for (int i = 0; i < l; ++i) {
            if (Double.isFinite(reader.getAndNext())) {
                return i;
            }
        }
        return l;
    }

    private static int last(DoubleSeq z) {
        DoubleSeqCursor reader = z.reverse().cursor();
        int l = z.length() - 1;
        for (int i = 0; i <= l; ++i) {
            if (Double.isFinite(reader.getAndNext())) {
                return l - i;
            }
        }
        return -1;
    }

    private static int estimationStart(DoubleSeq y, int ratio, int ypos, AggregationType type) {
        int start = first(y);
        if (type == AggregationType.Average || type == AggregationType.Sum) {
            return start * ratio;
        } else {
            return start * ratio + ypos;
        }
    }

    private static int estimationEnd(DoubleSeq y, int ratio, int ypos, AggregationType type) {
        int end = last(y);
        if (type == AggregationType.Average || type == AggregationType.Sum) {
            return (end + 1) * ratio;
        } else {
            return end * ratio + ypos + 1;
        }
    }
    
}
