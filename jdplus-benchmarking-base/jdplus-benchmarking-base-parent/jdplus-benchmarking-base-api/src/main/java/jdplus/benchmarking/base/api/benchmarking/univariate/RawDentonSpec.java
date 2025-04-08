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
package jdplus.benchmarking.base.api.benchmarking.univariate;

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.util.Validatable;
import nbbrd.design.Development;

/**
 *
 * @author LEMASSO
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder=true, buildMethodName="buildWithoutValidation")
public class RawDentonSpec implements ProcSpecification, Validatable<RawDentonSpec> {

    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor("benchmarking", "denton", null);

    private boolean multiplicative, modified;
    private int differencing;
    @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;
    
    private int frequencyRatio;
            
    public static Builder builder() {
        return new Builder()
                .multiplicative(true)
                .modified(true)
                .differencing(1)
                .aggregationType(AggregationType.Sum)
                .observationPosition(0)
                .frequencyRatio(0);
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public RawDentonSpec validate() throws IllegalArgumentException {
        if (aggregationType == AggregationType.None || aggregationType == AggregationType.Max
                || aggregationType == AggregationType.Min) {
            throw new IllegalArgumentException();
        }
        if (aggregationType == AggregationType.UserDefined && observationPosition<0)
            throw new IllegalArgumentException();
        return this;
    }

    public static class Builder implements Validatable.Builder<RawDentonSpec>{
        
    }
    public static final RawDentonSpec DEFAULT = builder().build();
}