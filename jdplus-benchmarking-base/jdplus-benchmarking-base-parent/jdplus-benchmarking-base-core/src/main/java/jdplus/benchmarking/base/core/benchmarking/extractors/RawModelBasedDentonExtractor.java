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

import jdplus.toolkit.base.api.information.InformationExtractor;
import jdplus.toolkit.base.api.information.InformationMapping;
import jdplus.benchmarking.base.api.univariate.TemporalDisaggregationDictionaries;
import jdplus.benchmarking.base.core.univariate.ResidualsDiagnostics;
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.benchmarking.base.core.univariate.ModelBasedDentonResults;
import jdplus.benchmarking.base.core.univariate.RawModelBasedDentonResults;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class RawModelBasedDentonExtractor extends InformationMapping<RawModelBasedDentonResults> {

    public RawModelBasedDentonExtractor() {
        set(TemporalDisaggregationDictionaries.TARGET, double[].class, source -> source.getTarget().toArray());
        set(TemporalDisaggregationDictionaries.INDICATOR, double[].class, source -> source.getIndicator().toArray());
        set(TemporalDisaggregationDictionaries.DISAGG, double[].class, source -> source.getDisaggregatedSeries().toArray());
        set(TemporalDisaggregationDictionaries.LDISAGG, double[].class, 
               source -> source.getDisaggregatedSeries()
                        .fn(source.getStdevDisaggregatedSeries(), (a, b) -> a - 2 * b).toArray());
        set(TemporalDisaggregationDictionaries.UDISAGG, double[].class, 
               source -> source.getDisaggregatedSeries()
                        .fn(source.getStdevDisaggregatedSeries(), (a, b) -> a + 2 * b).toArray());
        set(TemporalDisaggregationDictionaries.EDISAGG, double[].class, 
                source -> source.getStdevDisaggregatedSeries().toArray());
        set(TemporalDisaggregationDictionaries.LFBIRATIO, double[].class, 
                source -> source.getAggregatedBiRatios().toArray());
        set(TemporalDisaggregationDictionaries.BIRATIO, double[].class, 
                source -> source.getBiRatios().toArray());
        set(TemporalDisaggregationDictionaries.EBIRATIO, double[].class, 
                source -> source.getStdevBiRatios().toArray());
        set(TemporalDisaggregationDictionaries.LBIRATIO, double[].class, 
               source -> source.getBiRatios()
                        .fn(source.getStdevBiRatios(), (a, b) -> a - 2 * b).toArray());
        set(TemporalDisaggregationDictionaries.UBIRATIO, double[].class, 
               source -> source.getBiRatios()
                        .fn(source.getStdevBiRatios(), (a, b) -> a + 2 * b).toArray());
        set(TemporalDisaggregationDictionaries.RES, double[].class, 
                source -> source.getResiduals().toArray());
        delegate(TemporalDisaggregationDictionaries.LIKELIHOOD, LikelihoodStatistics.class, 
                source -> source.getLikelihood());
        delegate(TemporalDisaggregationDictionaries.RES, ResidualsDiagnostics.class, 
                source -> source.getResidualsDiagnostics());
    }

    @Override
    public Class getSourceClass() {
        return RawModelBasedDentonResults.class;
    }

}
