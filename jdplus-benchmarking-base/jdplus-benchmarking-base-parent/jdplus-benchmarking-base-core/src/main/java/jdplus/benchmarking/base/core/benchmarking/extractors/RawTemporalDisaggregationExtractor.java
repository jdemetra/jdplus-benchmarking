/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jdplus.benchmarking.base.core.benchmarking.extractors;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.benchmarking.base.core.univariate.RawDisaggregationResults;
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
public class RawTemporalDisaggregationExtractor extends InformationMapping<RawDisaggregationResults>{
    
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
//        set(TemporalDisaggregationDictionaries.REGNAMES, String[].class, source -> {
//            FastMatrix vars = source.getRegressors();
//            int n = vars == null ? 0 : vars.getColumnsCount();
//            if (n == 0) {
//                return null;
//            }
//            String[] names = new String[n];
//            for (int i = 0; i < names.length; ++i) {
//                names[i] = vars[i].getName();
//            }
//            return names;
//        });
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
        return RawDisaggregationResults.class;
    }
}
