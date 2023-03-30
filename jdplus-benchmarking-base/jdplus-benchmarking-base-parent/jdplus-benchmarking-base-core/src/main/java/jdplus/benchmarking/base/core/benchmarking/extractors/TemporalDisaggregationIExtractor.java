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
import jdplus.toolkit.base.core.stats.likelihood.LikelihoodStatistics;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.service.ServiceProvider;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationIResults;

/**
 *
 * @author palatej
 */
@ServiceProvider(InformationExtractor.class)
public class TemporalDisaggregationIExtractor extends InformationMapping<TemporalDisaggregationIResults> {

    public final String LIKELIHOOD = "likelihood", DISAGG = "disagg", A = "a", B="b",
            RES = "residuals", ML = "ml", PARAMETER = "parameter", EPARAMETER = "eparameter";

    public TemporalDisaggregationIExtractor() {
        set(DISAGG, TsData.class, source -> source.getDisaggregatedSeries());
        set(A, Double.class, source -> source.getA());
        set(B, Double.class, source -> source.getB());
        set(PARAMETER, Double.class, source -> {
            if (source.getMaximum() == null) {
                return Double.NaN;
            }
            double[] p = source.getMaximum().getParameters();
            return p.length == 0 ? Double.NaN : p[0];
        });
        delegate(LIKELIHOOD, LikelihoodStatistics.class, source -> source.getLikelihood());
    }

    @Override
    public Class getSourceClass() {
        return TemporalDisaggregationIResults.class;
    }

}
