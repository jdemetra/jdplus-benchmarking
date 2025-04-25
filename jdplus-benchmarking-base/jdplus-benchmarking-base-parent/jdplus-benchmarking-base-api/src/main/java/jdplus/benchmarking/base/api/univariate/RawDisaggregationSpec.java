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

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Getter
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class RawDisaggregationSpec implements ProcSpecification, Validatable<RawDisaggregationSpec> {

    public static final String VERSION = "3.0.0";

    public static final String FAMILY = "temporaldisaggregation";
    public static final String METHOD = "generic";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    public static final boolean DEF_AVERAGE = false;

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    private boolean average;
    private int frequencyRatio;

    @lombok.NonNull
    ModelSpec modelSpec;

    @lombok.NonNull
    EstimationSpec estimationSpec;

    @lombok.NonNull
    AlgorithmSpec algorithmSpec;

    public static class Builder implements Validatable.Builder<RawDisaggregationSpec> {
    }

    public static RawDisaggregationSpec chowLin(int frequencyRatio) {
        return new Builder()
                .average(DEF_AVERAGE)
                .frequencyRatio(frequencyRatio)
                .modelSpec(ModelSpec.CHOWLIN)
                .estimationSpec(EstimationSpec.DEFAULT)
                .algorithmSpec(AlgorithmSpec.DEFAULT)
                .build();
    }

    public static RawDisaggregationSpec fernandez(int frequencyRatio) {
        return new Builder()
                .average(DEF_AVERAGE)
                .frequencyRatio(frequencyRatio)
                .modelSpec(ModelSpec.FERNANDEZ)
                .estimationSpec(EstimationSpec.DEFAULT)
                .algorithmSpec(AlgorithmSpec.DEFAULT)
                .build();
    }

    public static Builder builder(int frequencyRatio) {
        return new Builder()
                .average(DEF_AVERAGE)
                .frequencyRatio(frequencyRatio)
                .estimationSpec(EstimationSpec.DEFAULT)
                .algorithmSpec(AlgorithmSpec.DEFAULT)
                .modelSpec(ModelSpec.DEFAULT);
    }

    @Override
    public RawDisaggregationSpec validate() throws IllegalArgumentException {
        modelSpec.check();
        return this;
    }

    @Override
    public String display() {
        return switch (modelSpec.getResidualsModel()) {
            case Ar1 ->
                "Chow-Lin";
            case Rw ->
                "Fernandez";
            case RwAr1 ->
                "Litterman";
            case Wn ->
                "Ols";
            default ->
                "regression";
        };
    }

}
