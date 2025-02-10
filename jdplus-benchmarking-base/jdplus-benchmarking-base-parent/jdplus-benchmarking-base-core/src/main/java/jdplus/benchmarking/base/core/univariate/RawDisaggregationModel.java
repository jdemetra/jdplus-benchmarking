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

import jdplus.benchmarking.base.api.univariate.IndexRange;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
class RawDisaggregationModel {

    public RawDisaggregationModel(RawDisaggregationModelBuilder builder) {
        this.y = builder.getY();
        this.ho = builder.getHo();
        this.hy = builder.getHy();
        this.ratio = builder.getRatio();
        this.yposition = builder.getYposition();
        this.X = builder.getX();
        this.Xc = builder.getXc();
        this.start = builder.getStart();
        this.end = builder.getEnd();
        this.estimationStart = builder.getEstimationStart();
        this.estimationEnd = builder.getEstimationEnd();
        this.yfactor = builder.getYfactor();
        this.xfactors = builder.getXfactors();

    }

    int size() {
        return hy.length();
    }

    int definedSize() {
        return end - start;
    }

    int estimationSize() {
        return estimationEnd - estimationStart;
    }

    IndexRange definitionRange() {
        return IndexRange.of(start, end);
    }

    IndexRange estimationRange() {
        return IndexRange.of(estimationStart, estimationEnd);
    }

    int nx() {
        return X.isEmpty() ? 0 : X.getColumnsCount();
    }

    DoubleSeq y;
    // original series transformed to the highest-frequency 
    DoubleSeq ho;

    // (rescaled) series transformed to the highest-frequency  (same as ho without rescaling)
    DoubleSeq hy;
    // original regressors, rescaled and/or cumulated regressors 
    // hy, X and Xc must start at the beginning of an aggregation period (low-frequency)
    FastMatrix X, Xc;

    // range (hifreq) where the regression variables are defined (otherwise, full range)
    // that range is used to estimate the disaggregated/interpolated series
    int start, end;

    // range used to estimate the regression.
    // periods containing missing values at the beginning/end of the regression variables and of the endogenous variable are excluded
    int estimationStart, estimationEnd;

    int yposition;
    // length of hy should be a multiple of ratio 
    int ratio;
    /**
     * Scaling factor for y
     */
    double yfactor;
    /**
     * Scaling factors for X
     */
    double[] xfactors;

    FastMatrix definedXc() {
        if (Xc.isEmpty()) {
            return Xc;
        }
        return Xc.extract(start, end - start, 0, Xc.getColumnsCount());
    }

    FastMatrix definedX() {
        if (X.isEmpty()) {
            return X;
        }
        return X.extract(start, end - start, 0, X.getColumnsCount());
    }

    FastMatrix estimationXc() {
        if (Xc.isEmpty()) {
            return Xc;
        }
        return Xc.extract(estimationStart, estimationEnd - estimationStart, 0, Xc.getColumnsCount());
    }

    FastMatrix estimationX() {
        if (X.isEmpty()) {
            return X;
        }
        return X.extract(estimationStart, estimationEnd - estimationStart, 0, X.getColumnsCount());
    }

    DoubleSeq definedY() {
        return hy.range(start, end);
    }

    DoubleSeq estimationY() {
        return hy.range(estimationStart, estimationEnd);
    }

    DoubleSeq definedYo() {
        return hy.range(start, end);
    }

    DoubleSeq estimationYo() {
        return hy.range(estimationStart, estimationEnd);
    }

}
