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
package jdplus.benchmarking.base.core.benchmarking.extractors;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.benchmarking.base.core.univariate.RawTemporalDisaggregationResults;
import nbbrd.service.ServiceProvider;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationDictionaries;
import jdplus.benchmarking.base.core.univariate.RawResidualsDiagnostics;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;

/**
 *
 * @author LEMASSO
 */
@ServiceProvider(InformationExtractor.class)
public class RawTemporalDisaggregationExtractor extends InformationMapping<RawTemporalDisaggregationResults> {

    public RawTemporalDisaggregationExtractor() {

        set(TemporalDisaggregationDictionaries.DISAGG, double[].class,
                source -> source.getDisaggregatedSeries().toArray());
        set(TemporalDisaggregationDictionaries.EDISAGG, double[].class,
                source -> source.getStdevDisaggregatedSeries().toArray());
        set(TemporalDisaggregationDictionaries.LDISAGG, double[].class,
                source -> source.getDisaggregatedSeries()
                        .fn(source.getStdevDisaggregatedSeries(), (a, b) -> a - 2 * b).toArray());
        set(TemporalDisaggregationDictionaries.UDISAGG, double[].class,
                source -> source.getDisaggregatedSeries().fn(source.getStdevDisaggregatedSeries(), (a, b) -> a + 2 * b).toArray());
        set(TemporalDisaggregationDictionaries.REGEFFECT, double[].class,
                source -> source.getRegressionEffects().toArray());
        set(TemporalDisaggregationDictionaries.SMOOTHINGEFFECT, double[].class, source -> {
            return DoublesMath.subtract(source.getDisaggregatedSeries(), source.getRegressionEffects()).toArray();
        }
        );
        set(TemporalDisaggregationDictionaries.COEFF, double[].class,
                source -> source.getCoefficients().toArray());
        set(TemporalDisaggregationDictionaries.COVAR, Matrix.class,
                source -> source.getCoefficientsCovariance());
        set(TemporalDisaggregationDictionaries.REGNAMES, String[].class, 
                source -> source.getRegressorsName());
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
            DoubleSeq R = source.getRegressionEffects();
            if (R == null) {
                return null;
            }
            DoubleSeq T = source.getDisaggregatedSeries();
            DoubleSeq S = DoublesMath.subtract(T, R);
            double vart = T.ssq();
            double vars = S.ssq();
            return Math.sqrt(vars / vart);
        });
        delegate(TemporalDisaggregationDictionaries.LIKELIHOOD, DiffuseLikelihoodStatistics.class, source -> source.getStats());
        delegate(TemporalDisaggregationDictionaries.RES, RawResidualsDiagnostics.class, source -> source.getResidualsDiagnostics());
    }

    @Override
    public Class getSourceClass() {
        return RawTemporalDisaggregationResults.class;
    }
}
