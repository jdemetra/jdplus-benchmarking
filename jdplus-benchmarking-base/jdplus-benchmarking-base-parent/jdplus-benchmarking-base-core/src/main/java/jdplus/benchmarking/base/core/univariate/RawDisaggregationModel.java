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

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;

/**
 *
 * @author Jean Palate
 */
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class RawDisaggregationModel {

    public RawDisaggregationModel(RawDisaggregationModelBuilder builder) {
        this.hy = builder.getHy();
        this.ho = builder.getHo();
        this.ratio = builder.getRatio();
        this.Xo = builder.getXo();
        this.X = builder.getX();
        this.Xc = builder.getXc();
        this.start = builder.getStart();
        this.end = builder.getEnd();
        this.estimationStart = builder.getEstimationStart();
        this.estimationEnd = builder.getEstimationEnd();
        this.yfactor = builder.getYfactor();
        this.xfactors = builder.getXfactors();

    }

    public int size() {
        return hy.length();
    }

    public int definedSize() {
        return end - start;
    }

    public int estimationSize() {
        return estimationEnd - estimationStart;
    }

    public int nx() {
        return X.isEmpty() ? 0 : X.getColumnsCount();
    }

    // series transformed to the highest-frequency
    DoubleSeq hy, ho;
    // regressors and cumulated regressors
    FastMatrix Xo, X, Xc;
    // range used to estimate the regression.
    // periods containing missing values (low or high-frequency) are excluded
    // hy, X and Xc start at the beginning of an aggregation period (low-frequency)
    // and finish at the end of such a period. The length of hy and the 
    // number of rows= of X (if any) are identical. They have been completed with
    // missing values to achieve that goal      
    int start, end;

    int estimationStart, estimationEnd;

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

    public FastMatrix definedXc() {
        if (Xc.isEmpty()) {
            return Xc;
        }
        return Xc.extract(start, end - start, 0, Xc.getColumnsCount());
    }

    public FastMatrix definedX() {
        if (X.isEmpty()) {
            return X;
        }
        return X.extract(start, end - start, 0, X.getColumnsCount());
    }

    public FastMatrix estimationXc() {
        if (Xc.isEmpty()) {
            return Xc;
        }
        return Xc.extract(estimationStart, estimationEnd - estimationStart, 0, Xc.getColumnsCount());
    }

    public FastMatrix estimationX() {
        if (X.isEmpty()) {
            return X;
        }
        return X.extract(estimationStart, estimationEnd - estimationStart, 0, X.getColumnsCount());
    }

    public DoubleSeq definedY() {
        return hy.range(start, end);
    }

    public DoubleSeq estimationY() {
        return hy.range(estimationStart, estimationEnd);
    }

    public DoubleSeq estimationYo() {
        return ho.range(estimationStart, estimationEnd);
    }

    public DoubleSeq definedYo() {
        return ho.range(start, end);
    }
}
