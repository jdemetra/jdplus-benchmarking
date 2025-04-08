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
package jdplus.benchmarking.base.api.univariate;

import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.data.AggregationType;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.util.Validatable;
import java.time.LocalDate;
import java.util.Map;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder=true, buildMethodName="buildWithoutValidation")
public class RawModelBasedDentonSpec implements ProcSpecification, Validatable<RawModelBasedDentonSpec> {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("temporaldisaggregation", "rawmdenton", null);

    @lombok.Singular
    private Map<Integer, Double> shockVariances; 
    @lombok.Singular
    private Map<Integer, Double> fixedBiRatios; 
    @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;

    private int frequencyRatio;

    public static Builder builder() {
        return new Builder()
                .aggregationType(AggregationType.Sum)
                .observationPosition(0);
    }

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    @Override
    public RawModelBasedDentonSpec validate() throws IllegalArgumentException {
        if (aggregationType == AggregationType.None || aggregationType == AggregationType.Max
                || aggregationType == AggregationType.Min) {
            throw new IllegalArgumentException();
        }
        if (aggregationType == AggregationType.UserDefined && observationPosition<0)
            throw new IllegalArgumentException();
        return this;
    }

    public static class Builder implements Validatable.Builder<RawModelBasedDentonSpec>{
        
    }
    public static final RawModelBasedDentonSpec DEFAULT = builder().build();
    
    @Override
    public String display(){
        return "Raw Model-based Denton";
    }

}
