/*
 * Copyright 2025 National Bank of Belgium.
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
package jdplus.benchmarking.base.core.multivariate;

import java.util.LinkedHashMap;
import java.util.Map;

import jdplus.benchmarking.base.api.multivariate.ModelData;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLin;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinResults;
import jdplus.benchmarking.base.api.multivariate.MultivariateChowLinSpec;
import jdplus.toolkit.base.api.timeseries.TsData;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author LEMASSO
 */
@ServiceProvider(MultivariateChowLin.Processor.class)
public class MultivariateChowLinProcessor implements MultivariateChowLin.Processor {
    @Override
    public MultivariateChowLinResults process(LinkedHashMap<String, ModelData> mData, Map<String, TsData> constraints, MultivariateChowLinSpec spec) {
        MultivariateChowLinEngine engine=new MultivariateChowLinEngine();
        return engine.process(mData, constraints, spec);
    }
}
