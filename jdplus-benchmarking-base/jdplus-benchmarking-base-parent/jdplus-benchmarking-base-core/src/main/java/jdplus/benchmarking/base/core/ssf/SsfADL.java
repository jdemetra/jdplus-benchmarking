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

import jdplus.benchmarking.base.api.univariate.ADLSpec.XAR;
import jdplus.benchmarking.base.core.univariate.ADLDefinition;
import jdplus.benchmarking.base.core.univariate.ADLProcessor;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.core.data.DataBlock;
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
public class SsfADL {

    public Ssf ssfRepresentation(ADLDefinition definition, FastMatrix X, int ratio, int startPosition) {

        FastMatrix W = ADLProcessor.regressionMatrix(definition, X);
        StateComponent ncmp;
        ISsfLoading nloading;
        StateComponent rcmp;

        double phi = definition.getPhi();
        if (phi == 1) {
            ncmp = Rw.DEFAULT;
            nloading = Rw.defaultLoading();
            rcmp = TransitionRegSsf.of(ncmp, W, w0_fernandez(definition, X));
        } else {
            ncmp = AR1.of(phi);
            nloading = AR1.defaultLoading();
            rcmp = TransitionRegSsf.of(ncmp, W, w0_chowlin(definition, X));
        }

        ISsfLoading rloading = TransitionRegSsf.defaultLoading(ncmp.dim(), nloading);
        Ssf ssf = Ssf.of(SsfCumulator.of(rcmp, rloading, ratio, startPosition),
                SsfCumulator.defaultLoading(rloading, ratio, startPosition));
        return ssf;
    }

    // w0 expresses the contraints that we put on the diffuse coefficients for the initialization of the filter
    private DoubleSeq w0_chowlin(ADLDefinition definition, FastMatrix X) {
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
            w0[c++] = (1 - 2 * phi) * q * q;
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

    private DoubleSeq w0_fernandez(ADLDefinition definition, FastMatrix X) {
        int nx = X.getColumnsCount();
        if (definition.getXar() == XAR.FREE) {
            nx += X.getColumnsCount();
        }
        if (definition.isTrend()) {
            ++nx;
        }
        return DoubleSeq.onMapping(nx, i -> 0);
    }
}
