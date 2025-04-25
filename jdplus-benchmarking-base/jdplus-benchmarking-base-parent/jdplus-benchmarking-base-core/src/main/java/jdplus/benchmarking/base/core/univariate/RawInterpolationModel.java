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
class RawInterpolationModel {

    public RawInterpolationModel(RawInterpolationModelBuilder builder) {
        this.y = builder.getY();
        this.ho = builder.getHo();
        this.hy = builder.getHy();
        this.ratio = builder.getRatio();
        this.startOffset = builder.getStartOffset();
        this.X = builder.getX();
        this.Xo = builder.getXo();
        this.estimationStart = builder.getEstimationStart();
        this.estimationEnd = builder.getEstimationEnd();
        this.yfactor = builder.getYfactor();
        this.xfactors = builder.getXfactors();

    }

    int size() {
        return hy.length();
    }

    int estimationSize() {
        return estimationEnd - estimationStart;
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
    FastMatrix Xo, X;

    // range used to estimate the regression.
    // periods containing missing values at the beginning/end of the regression variables and of the endogenous variable are excluded
    int estimationStart, estimationEnd;

    int startOffset;
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

    FastMatrix estimationX() {
        if (X.isEmpty()) {
            return X;
        }
        return X.extract(estimationStart, estimationEnd - estimationStart, 0, X.getColumnsCount());
    }

    DoubleSeq estimationY() {
        return hy.range(estimationStart, estimationEnd);
    }

    DoubleSeq estimationYo() {
        return hy.range(estimationStart, estimationEnd);
    }

}
