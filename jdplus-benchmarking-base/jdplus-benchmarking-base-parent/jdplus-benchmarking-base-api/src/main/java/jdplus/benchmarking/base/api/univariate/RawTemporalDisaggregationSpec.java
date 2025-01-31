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
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Getter
@lombok.Builder(toBuilder = true, buildMethodName = "buildWithoutValidation")
public final class RawTemporalDisaggregationSpec implements ProcSpecification, Validatable<RawTemporalDisaggregationSpec> {

    public static final String VERSION = "3.0.0";

    public static final String FAMILY = "temporaldisaggregation";
    public static final String METHOD = "generic";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    public static final SsfInitialization DEF_ALGORITHM = SsfInitialization.SqrtDiffuse;
    public static final boolean DEF_FAST = true, DEF_RESCALE = true, DEF_LOG = false, DEF_DIFFUSE = false;

    public static final double DEF_EPS = 1e-5;

    public static final AggregationType DEF_AGGREGATION = AggregationType.Sum;

    public static final RawTemporalDisaggregationSpec CHOWLIN = builder()
            .aggregationType(AggregationType.Sum)
            .residualsModel(RawTemporalDisaggregationSpec.Model.Ar1)
            .constant(true)
            .truncatedParameter(0.0)
            .fast(DEF_FAST)
            .estimationPrecision(DEF_EPS)
            .rescale(DEF_RESCALE)
            .algorithm(DEF_ALGORITHM)
            .disaggregationRatio(0)
            .build();

    public static final RawTemporalDisaggregationSpec FERNANDEZ = builder()
            .aggregationType(AggregationType.Sum)
            .residualsModel(RawTemporalDisaggregationSpec.Model.Rw)
            .constant(false)
            .fast(DEF_FAST)
            .estimationPrecision(DEF_EPS)
            .rescale(DEF_RESCALE)
            .algorithm(DEF_ALGORITHM)
            .disaggregationRatio(0)
            .build();

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    public static enum Model {
        Wn,
        Ar1,
        Rw,
        RwAr1,
        I2, I3;

        public boolean hasParameter() {
            return this == Ar1 || this == RwAr1;
        }

        public boolean isStationary() {
            return this == Ar1 || this == Wn;
        }

        public int getParametersCount() {
            return (this == Ar1 || this == RwAr1) ? 1 : 0;
        }

        public int getDifferencingOrder() {
            return switch (this) {
                case Rw, RwAr1 ->
                    1;
                case I2 ->
                    2;
                case I3 ->
                    3;
                default ->
                    0;
            };
        }
    }

    @lombok.NonNull
    private AggregationType aggregationType;
    private int observationPosition;
    private int disaggregationRatio;

    @lombok.NonNull
    private Model residualsModel;
    private boolean constant, trend;
    private Parameter parameter;
//    @lombok.NonNull
//    private RangeSelector estimationSpan; // TODO
    private boolean log, diffuseRegressors;
    private Double truncatedParameter;
    private boolean zeroInitialization, fast;

    private double estimationPrecision;
    private SsfInitialization algorithm;
    private boolean rescale;

    public boolean isParameterEstimation() {
        return (residualsModel == Model.Ar1 || residualsModel == Model.RwAr1)
                && parameter.getType() != ParameterType.Fixed;
    }

    public static class Builder implements Validatable.Builder<RawTemporalDisaggregationSpec> {
    }

    public static Builder builder() {
        return new Builder()
                .aggregationType(DEF_AGGREGATION)
                .residualsModel(Model.Ar1)
                .constant(true)
                .fast(DEF_FAST)
                .algorithm(DEF_ALGORITHM)
                .rescale(DEF_RESCALE)
                .parameter(Parameter.undefined())
                .estimationPrecision(DEF_EPS)
                .disaggregationRatio(0);
    }

    @Override
    public RawTemporalDisaggregationSpec validate() throws IllegalArgumentException {
        switch (residualsModel) {
            case Rw, RwAr1 -> {
                if (constant && !zeroInitialization) {
                    throw new IllegalArgumentException("constant not allowed");
                }
            }
            case I2, I3 -> {
                if (constant && !zeroInitialization) {
                    throw new IllegalArgumentException("constant not allowed");
                }
                if (trend && !zeroInitialization) {
                    throw new IllegalArgumentException("trend not allowed");
                }
            }
        }
        return this;
    }

    @Override
    public String display() {
        if (aggregationType == AggregationType.Average || aggregationType == AggregationType.Sum) {
            return switch (residualsModel) {
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
        } else {
            StringBuilder builder = new StringBuilder();
            builder.append("Interpolation [")
                    .append(residualsModel.name())
                    .append(']');
            return builder.toString();
        }

    }

}
