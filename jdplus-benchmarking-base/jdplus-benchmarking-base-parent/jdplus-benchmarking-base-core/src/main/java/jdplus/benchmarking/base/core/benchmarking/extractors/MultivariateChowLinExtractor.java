package jdplus.benchmarking.base.core.benchmarking.extractors;

import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinResults;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationDictionaries;
import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.service.ServiceProvider;
import java.util.Map;

@ServiceProvider(InformationExtractor.class)
public class MultivariateChowLinExtractor extends InformationMapping<MultivariateChowLinResults> {

    public MultivariateChowLinExtractor() {
        set(TemporalDisaggregationDictionaries.DISAGG, Map.class, source -> source.getDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.EDISAGG, Map.class, source -> source.getStdevDisaggregatedSeries());
        set(TemporalDisaggregationDictionaries.REGEFFECT, Map.class, source -> source.getRegressionEffects());
        set(TemporalDisaggregationDictionaries.REG, Map.class, source -> source.getRegressors());
        set(TemporalDisaggregationDictionaries.COEFF, Map.class, source -> source.getCoefficients());
        set(TemporalDisaggregationDictionaries.COVAR, Map.class, source -> source.getCoefficientsVariance());
    }

    @Override
    public Class getSourceClass() {
        return MultivariateChowLinResults.class;
    }
}
