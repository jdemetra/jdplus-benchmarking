package jdplus.benchmarking.base.core.benchmarking.extractors;

import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinResults;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationDictionaries;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.data.DoublesMath;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import nbbrd.service.ServiceProvider;

import java.util.*;

@ServiceProvider(InformationExtractor.class)
public class MultivariateChowLinExtractor extends InformationMapping<MultivariateChowLinResults> {

    public MultivariateChowLinExtractor() {
        set(TemporalDisaggregationDictionaries.DISAGG, Map.class, source -> source.getDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.EDISAGG, Map.class, source -> source.getStdevDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.REGEFFECT, Map.class, source -> source.getRegressionEffects());
        set(TemporalDisaggregationDictionaries.REG, Map.class, source -> source.getRegressors());
        set(TemporalDisaggregationDictionaries.COEFF, Map.class, source -> source.getCoefficients());
        set(TemporalDisaggregationDictionaries.COVAR, Map.class, source -> source.getCoefficientsVariance());
        set(TemporalDisaggregationDictionaries.REGNAMES, Map.class, source -> source.getRegressorsNames());
        set(TemporalDisaggregationDictionaries.SPART, double[].class, source -> {
            Map<String, TsData> regeffect = source.getRegressionEffects();
            double[] sp = new double[regeffect.size()];
            int index = 0;
            for (String sName : regeffect.keySet()) {
                TsData re = regeffect.get(sName);
                if (re == null || re.isEmpty()) {
                    sp[index] = Double.NaN;
                } else{
                    DoubleSeq T = source.getDisaggregatedSeries().get(sName).getValues();
                    DoubleSeq R = re.getValues();
                    DoubleSeq S = DoublesMath.subtract(T, R);
                    double vart = T.ssq();
                    double vars = S.ssq();
                    sp[index] = Math.sqrt(vars / vart);
                }
                ++index;
            }
            return(sp);
        });
    }

    @Override
    public Class getSourceClass() {
        return MultivariateChowLinResults.class;
    }
}
