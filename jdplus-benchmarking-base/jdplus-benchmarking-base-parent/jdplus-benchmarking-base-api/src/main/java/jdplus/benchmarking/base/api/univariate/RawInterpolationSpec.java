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
public final class RawInterpolationSpec implements ProcSpecification, Validatable<RawInterpolationSpec> {

    public static final String VERSION = "3.0.0";

    public static final String FAMILY = "temporalinterpolation";
    public static final String METHOD = "generic";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    private int firstObservationPosition;
    private int frequencyRatio;

    @lombok.NonNull
    ModelSpec modelSpec;

    @lombok.NonNull
    EstimationSpec estimationSpec;

    @lombok.NonNull
    AlgorithmSpec algorithmSpec;

    public static class Builder implements Validatable.Builder<RawInterpolationSpec> {
    }

    public static Builder builder(int frequencyRatio) {
        return new Builder()
                .frequencyRatio(frequencyRatio)
                .firstObservationPosition(0)
                .estimationSpec(EstimationSpec.DEFAULT)
                .algorithmSpec(AlgorithmSpec.DEFAULT)
                .modelSpec(ModelSpec.DEFAULT);
    }

    @Override
    public RawInterpolationSpec validate() throws IllegalArgumentException {
        switch (modelSpec.getResidualsModel()) {
            case Rw, RwAr1 -> {
                if (modelSpec.isConstant() && !modelSpec.isZeroInitialization()) {
                    throw new IllegalArgumentException("constant not allowed");
                }
            }
            case I2, I3 -> {
                if (modelSpec.isConstant() && !modelSpec.isZeroInitialization()) {
                    throw new IllegalArgumentException("constant not allowed");
                }
                if (modelSpec.isTrend() && !modelSpec.isZeroInitialization()) {
                    throw new IllegalArgumentException("trend not allowed");
                }
            }
        }
        return this;
    }

    @Override
    public String display() {
        StringBuilder builder = new StringBuilder();
        builder.append("Interpolation [")
                .append(modelSpec.getResidualsModel().name())
                .append(']');
        return builder.toString();
    }

}
