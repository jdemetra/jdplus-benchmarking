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
import jdplus.benchmarking.base.core.univariate.ADLDefinition;
import jdplus.toolkit.base.core.data.DataBlock;
import jdplus.toolkit.base.core.data.DataBlockIterator;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.ssf.ISsfLoading;
import jdplus.toolkit.base.core.ssf.StateComponent;
import jdplus.toolkit.base.core.ssf.benchmarking.SsfCumulator;
import jdplus.toolkit.base.core.ssf.univariate.Ssf;
import jdplus.toolkit.base.core.ssf.arima.Rw;
import jdplus.toolkit.base.core.ssf.arima.AR1;
import jdplus.toolkit.base.core.ssf.basic.RegSsf;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class SsfADL {

    public Ssf ssfRepresentation(ADLDefinition definition, FastMatrix X, int ratio, int startPosition) {

        StateComponent ncmp;
        StateComponent rcmp;
        ISsfLoading rloading;
        FastMatrix W = regressionMatrix(definition, X);
        double phi = definition.getPhi();
        if (phi == 1) {
            ncmp = Rw.DEFAULT;
            rloading = RegSsf.defaultLoading(1, Rw.defaultLoading(), W);
            rcmp = RegSsf.of(ncmp, W);
        } else {
            ncmp = AR1.of(phi);
            rloading = RegSsf.defaultLoading(1, AR1.defaultLoading(), W);
            rcmp = RegSsf.of(ncmp, W);
        }

        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, ratio, startPosition),
                SsfCumulator.defaultLoading(rloading, ratio, startPosition));
        return ssf;
    }

    public Ssf ssfRepresentation(FastMatrix W, double phi, int ratio, int startPosition) {

        StateComponent ncmp;
        StateComponent rcmp;
        ISsfLoading rloading;
        if (phi == 1) {
            ncmp = Rw.DEFAULT;
            rloading = RegSsf.defaultLoading(1, Rw.defaultLoading(), W);
            rcmp = RegSsf.of(ncmp, W);
        } else {
            ncmp = AR1.of(phi);
            rloading = RegSsf.defaultLoading(1, AR1.defaultLoading(), W);
            rcmp = RegSsf.of(ncmp, W);
        }

        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, ratio, startPosition),
                SsfCumulator.defaultLoading(rloading, ratio, startPosition));
        return ssf;
    }

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
        double phi = definition.getPhi();

        FastMatrix W = FastMatrix.make(n, nx);
        int c = 0;
        if (definition.isMean()) {
            DataBlock col = W.column(c++);
            col.set(1);
            if (phi != 1) {
                col.set(0, 1 / (1 - phi));
            }
            cumul(col, phi);
        }
        if (definition.isTrend()) {
           DataBlock col = W.column(c++);
            col.set(i -> i);
            if (phi != 1) {
                col.set(0, - phi / ((1 - phi) * (1 - phi)));
            }
            cumul(col, phi);
        }
        switch (definition.getXar()) {
            case NONE -> {
                DataBlockIterator cols = X.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock col = W.column(c++);
                    col.copy(cols.next());
                    cumul(col, phi);
                }
            }
            case FREE -> {
                DataBlockIterator cols = X.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock cur = cols.next();
                    DataBlock col0 = W.column(c++);
                    col0.drop(1, 0).copy(cur.drop(0, 1));
                    col0.set(0, cur.get(0));
                    DataBlock col1 = W.column(c);
                    col1.copy(cur);
                    if (phi != 1) {
                        col0.mul(0, 1 / (1 - phi));
                        col1.mul(0, 1 / (1 - phi));
                    }
                    cumul(col0, phi);
                    cumul(col1, phi);
                }
            }
            case SAME -> {
                DataBlockIterator cols = X.columnsIterator();
                while (cols.hasNext()) {
                    DataBlock col = W.column(c++);
                    col.copy(cols.next());
                }
            }
        }
        return W;
    }

    private void cumul(DataBlock C, double phi) {
        if (phi == 1) {
            C.cumul();
        } else {
            C.applyRecursively(1, (a, b) -> phi * a + b);
        }

    }
}
