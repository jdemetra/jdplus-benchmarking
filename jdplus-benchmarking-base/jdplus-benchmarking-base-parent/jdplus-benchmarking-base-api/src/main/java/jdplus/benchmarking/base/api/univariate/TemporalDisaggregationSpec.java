/*
 * Copyright 2022 National Bank of Belgium.
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
public final class TemporalDisaggregationSpec implements ProcSpecification, Validatable<TemporalDisaggregationSpec> {

    public static final String VERSION = "3.0.0";

    public static final String FAMILY = "temporaldisaggregation";
    public static final String METHOD = "generic";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    public static final boolean DEF_AVERAGE = false;
    public static final int DEF_PERIOD = 4;

    public static final TemporalDisaggregationSpec CHOWLIN = new Builder()
            .average(false)
            .defaultPeriod(DEF_PERIOD)
            .modelSpec(ModelSpec.CHOWLIN)
            .algorithmSpec(AlgorithmSpec.DEFAULT)
            .estimationSpec(TsEstimationSpec.DEFAULT)
            .build();

    public static final TemporalDisaggregationSpec FERNANDEZ = new Builder()
            .average(false)
            .defaultPeriod(DEF_PERIOD)
            .modelSpec(ModelSpec.FERNANDEZ)
            .algorithmSpec(AlgorithmSpec.DEFAULT)
            .estimationSpec(TsEstimationSpec.DEFAULT)
            .build();

    public static final TemporalDisaggregationSpec LITTERMAN = new Builder()
            .average(false)
            .defaultPeriod(DEF_PERIOD)
            .modelSpec(ModelSpec.LITTERMAN)
            .algorithmSpec(AlgorithmSpec.DEFAULT)
            .estimationSpec(TsEstimationSpec.DEFAULT)
            .build();

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    private boolean average;
    private int defaultPeriod;

    @lombok.NonNull
    ModelSpec modelSpec;

    @lombok.NonNull
    TsEstimationSpec estimationSpec;

    @lombok.NonNull
    AlgorithmSpec algorithmSpec;

    public static class Builder implements Validatable.Builder<TemporalDisaggregationSpec> {
    }

    public static Builder builder() {
        return new Builder()
                .defaultPeriod(DEF_PERIOD)
                .average(DEF_AVERAGE)
                .estimationSpec(TsEstimationSpec.DEFAULT)
                .algorithmSpec(AlgorithmSpec.DEFAULT)
                .modelSpec(ModelSpec.DEFAULT);
    }

    @Override
    public TemporalDisaggregationSpec validate() throws IllegalArgumentException {
//        if (aggregationType != AggregationType.Sum && aggregationType != AggregationType.Average) {
//            throw new IllegalArgumentException(aggregationType.name() + " not allowed in disaggregation (consider interpolation)");
//        }
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
