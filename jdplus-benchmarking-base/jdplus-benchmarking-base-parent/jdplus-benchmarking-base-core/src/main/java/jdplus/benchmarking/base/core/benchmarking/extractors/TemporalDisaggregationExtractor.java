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
package jdplus.benchmarking.base.core.benchmarking.extractors;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.benchmarking.base.core.univariate.ResidualsDiagnostics;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationResults;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import nbbrd.service.ServiceProvider;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationDictionaries;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class TemporalDisaggregationExtractor extends InformationMapping<TemporalDisaggregationResults> {

    public TemporalDisaggregationExtractor() {
        set(TemporalDisaggregationDictionaries.DISAGG, TsData.class,
                source -> source.getDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.EDISAGG, TsData.class,
                source -> source.getStdevDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.LDISAGG, TsData.class,
                source -> source.getDisaggregatedSeries()
                        .fn(source.getStdevDisaggregatedSeries(), (a, b) -> a - 2 * b));
        set(TemporalDisaggregationDictionaries.UDISAGG, TsData.class,
                source -> source.getDisaggregatedSeries().fn(source.getStdevDisaggregatedSeries(), (a, b) -> a + 2 * b));
        set(TemporalDisaggregationDictionaries.REGEFFECT, TsData.class,
                source -> source.getRegressionEffects());
        set(TemporalDisaggregationDictionaries.SMOOTHINGEFFECT, TsData.class,
                source -> TsData.subtract(source.getDisaggregatedSeries(), source.getRegressionEffects()));
        set(TemporalDisaggregationDictionaries.COEFF, double[].class,
                source -> source.getCoefficients().toArray());
        set(TemporalDisaggregationDictionaries.COVAR, Matrix.class,
                source -> source.getCoefficientsCovariance());
        set(TemporalDisaggregationDictionaries.REGNAMES, String[].class, source -> {
            Variable[] vars = source.getIndicators();
            int n = vars == null ? 0 : vars.length;
            if (n == 0) {
                return null;
            }
            String[] names = new String[n];
            for (int i = 0; i < names.length; ++i) {
                names[i] = vars[i].getName();
            }
            return names;
        });
        set(TemporalDisaggregationDictionaries.PARAMETER, Double.class, source -> {
            if (source.getMaximum() == null) {
                return Double.NaN;
            }
            double[] p = source.getMaximum().getParameters();
            return p.length == 0 ? Double.NaN : p[0];
        });
        set(TemporalDisaggregationDictionaries.EPARAMETER, Double.class, source -> {
            if (source.getMaximum() == null) {
                return Double.NaN;
            }
            Matrix H = source.getMaximum().getHessian();
            int n = source.getLikelihood().degreesOfFreedom() - 1;
            return (H == null || H.isEmpty()) ? Double.NaN : Math.sqrt(-1 / (n * H.get(0, 0)));
        });
        set(TemporalDisaggregationDictionaries.SPART, Double.class, source -> {
            TsData re = source.getRegressionEffects();
            if (re == null) {
                return null;
            }
            DoubleSeq T = source.getDisaggregatedSeries().getValues();
            DoubleSeq R = re.getValues();
            DoubleSeq S = DoublesMath.subtract(T, R);
            double vart = T.ssq();
            double vars = S.ssq();
            return Math.sqrt(vars / vart);
        });
        delegate(TemporalDisaggregationDictionaries.LIKELIHOOD, DiffuseLikelihoodStatistics.class, source -> source.getStats());
        delegate(TemporalDisaggregationDictionaries.RES, ResidualsDiagnostics.class, source -> source.getResidualsDiagnostics());
    }

    @Override
    public Class getSourceClass() {
        return TemporalDisaggregationResults.class;
    }

}
