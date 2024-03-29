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
package jdplus.benchmarking.base.api.benchmarking.univariate;

import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.data.AggregationType;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder=true, buildMethodName="buildWithoutValidation")
public class CubicSplineSpec implements ProcSpecification, Validatable<CubicSplineSpec> {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("benchmarking", "cubicspline", null);

    @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;

    public static Builder builder() {
        return new Builder()
                .aggregationType(AggregationType.Last);
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    @Override
    public CubicSplineSpec validate() throws IllegalArgumentException {
        if (aggregationType == AggregationType.None || aggregationType == AggregationType.Max
                || aggregationType == AggregationType.Min) {
            throw new IllegalArgumentException();
        }
        return this;
    }

    public static class Builder implements Validatable.Builder<CubicSplineSpec>{
        
    }
    public static final CubicSplineSpec DEFAULT = builder().build();

}
