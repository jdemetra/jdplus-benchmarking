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
package jdplus.benchmarking.base.core.ssf;

import jdplus.benchmarking.base.api.univariate.ADLSpec;
import jdplus.benchmarking.base.api.univariate.ADLSpec.XAR;
import jdplus.benchmarking.base.core.univariate.ADLDefinition;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.benchmarking.SsfCumulator;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.arima.Rw;
import jdplus.toolkit.base.core.ssf.arima.AR1;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SsfADL1 {

    public Ssf ssfRepresentation(ADLDefinition definition, FastMatrix X, int ratio, int startPosition) {
        FastMatrix W = regressionMatrix(definition, X);
        DoubleSeq w0 = w0(definition, X);
        return ssfRepresentation(definition, W, w0, ratio, startPosition);
    }

    public DoubleSeq w0(ADLDefinition definition, FastMatrix X) {
        return definition.getPhi() == 1 ? w0_r1(definition, X) : w0_ar(definition, X);
    }

    public Ssf ssfRepresentation(ADLDefinition definition, FastMatrix W, DoubleSeq w0, int ratio, int startPosition) {
        StateComponent ncmp;
        ISsfLoading nloading;

        double phi = definition.getPhi();
        if (phi == 1) {
            ncmp = Rw.DEFAULT;
            nloading = Rw.defaultLoading();
        } else {
            ncmp = AR1.of(phi);
            nloading = AR1.defaultLoading();
        }
        StateComponent rcmp = TransitionRegSsf.of(ncmp, W, w0);

        ISsfLoading rloading = TransitionRegSsf.defaultLoading(ncmp.dim(), nloading);
        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, ratio, startPosition),
                SsfCumulator.defaultLoading(rloading, ratio, startPosition));
        return ssf;
    }

    // w0 expresses the contraints that we put on the diffuse coefficients for the initialization of the filter
    // y0 = w0*beta
    private DoubleSeq w0_ar(ADLDefinition definition, FastMatrix X) {
        int nx = X.getColumnsCount();
        if (definition.getXar() == XAR.FREE) {
            nx += X.getColumnsCount();
        }
        if (definition.isMean()) {
            ++nx;
        }
        if (definition.isTrend()) {
            ++nx;
        }
        double[] w0 = new double[nx];
        double phi = definition.getPhi();
        double q = 1 / (1 - phi);
        int c = 0;
        if (definition.isMean()) {
            w0[c++] = q;
        }
        if (definition.isTrend()) {
            w0[c++] = -phi * q * q;
        }
        DataBlock row = X.row(0);
        switch (definition.getXar()) {
            case NONE -> {
                for (int i = 0; i < X.getColumnsCount(); ++i) {
                    w0[c++] = q * row.get(i);
                }
            }
            case FREE -> {
                for (int i = 0; i < X.getColumnsCount(); ++i) {
                    double w = q * row.get(i);
                    w0[c++] = w;
                    w0[c++] = w;
                }
            }
            case SAME -> {
                for (int i = 0; i < X.getColumnsCount(); ++i) {
                    w0[c++] = row.get(i);
                }
            }
        }
        return DoubleSeq.of(w0);
    }

    private DoubleSeq w0_r1(ADLDefinition definition, FastMatrix X) {
        int nx = X.getColumnsCount();
        if (definition.getXar() == XAR.FREE) {
            nx += X.getColumnsCount();
        }
        if (definition.isMean()) {
            ++nx;
        }
        if (definition.isTrend()) {
            ++nx;
        }
        return DoubleSeq.onMapping(nx, i -> 0);
    }

    /**
     * Row t correspond to the matrix going from t to t+1 (contains x(t+1)-rho
     * x(t) in the case chowlin, t+1 for the trend)
     *
     * @param definition
     * @param X
     * @return
     */
    public FastMatrix regressionMatrix(ADLDefinition definition, FastMatrix X) {
        int nx = X.getColumnsCount();
        if (definition.getXar() == ADLSpec.XAR.FREE) {
            nx += X.getColumnsCount();
        }
        if (definition.isMean()) {
            ++nx;
        }
        if (definition.isTrend()) {
            ++nx;
        }
        int n = X.getRowsCount();
//        if (definition.isXunitRoot()) {
//            z = X.deepClone();
//            for (int i = n - 1; i > 0; --i) {
//                z.row(i).sub(z.row(i - 1));
//            }
//            z.row(0).set(0);
//        // z contains now either the original x or dx
//        }
        double phi = definition.getPhi();
        FastMatrix W = FastMatrix.make(n, nx);
        int c = 0;
        if (definition.isMean()) {
            W.column(c++).set(1);
        }
        if (definition.isTrend()) {
            W.column(c++).set(i -> i + 1);
        }
        switch (definition.getXar()) {
            case NONE -> {
                DataBlockIterator cols = X.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock col = W.column(c++);
                    col.drop(0, 1).copy(cols.next().drop(1, 0));
                    col.set(n - 1, col.get(n - 2));
                }
            }
            case FREE -> {
                DataBlockIterator cols = X.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock cur = cols.next();
                    DataBlock col = W.column(c++);
                    col.drop(0, 1).copy(cur.drop(1, 0)); // x(t+1)
                    col.set(n - 1, cur.get(n - 1)); // x(t)
                    W.column(c).copy(cur);
                }
            }
            case SAME -> {
                DataBlockIterator cols = X.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock cur = cols.next();
                    DataBlock col = W.column(c++);
                    col.drop(0, 1).copy(cur.drop(1, 0)); // x(t+1)
                    col.set(n - 1, cur.get(n - 1)); // x(t)
                    col.addAY(-phi, cur);
                }
            }
        }
        return W;
    }

}
