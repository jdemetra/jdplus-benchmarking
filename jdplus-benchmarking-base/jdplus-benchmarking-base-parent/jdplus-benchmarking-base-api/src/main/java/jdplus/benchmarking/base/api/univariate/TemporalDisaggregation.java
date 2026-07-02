/*
 * Copyright 2019 National Bank of Belgium.
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *      https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jdplus.benchmarking.base.api.univariate;

import jdplus.toolkit.base.api.design.Algorithm;
import jdplus.toolkit.base.api.processing.ProcResults;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.design.Development;
import nbbrd.service.Quantifier;
import nbbrd.service.ServiceDefinition;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.experimental.UtilityClass
public class TemporalDisaggregation {

    private final AtomicReference<TemporalDisaggregation.Processor> PROCESSOR = new AtomicReference<>(TemporalDisaggregationLoader.Processor.load());

    public void setProcessor(Processor algorithm) {
        PROCESSOR.set(algorithm);
    }

    public Processor getProcessor() {
        return PROCESSOR.get();
    }

    public ProcResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec, List<String> items) {
        return PROCESSOR.get().process(aggregatedSeries, indicators, spec, items);
    }

    public ProcResults process(TsData aggregatedSeries, int nBackcasts, int nForecasts, TemporalDisaggregationSpec spec, List<String> items) {
        return PROCESSOR.get().process(aggregatedSeries, nBackcasts, nForecasts, spec, items);
    }

    @Algorithm
    @SuppressWarnings("SingleFallbackNotExpected")
    @ServiceDefinition(quantifier = Quantifier.SINGLE)
    public interface Processor {

        ProcResults process(TsData aggregatedSeries, TsData[] indicators, TemporalDisaggregationSpec spec, List<String> items);

        ProcResults process(TsData aggregatedSeries, int nBackcasts, int nForecasts, TemporalDisaggregationSpec spec, List<String> items);
    }
}
