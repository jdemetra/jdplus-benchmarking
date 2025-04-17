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
public final class RawInterpolationSpec implements ProcSpecification, Validatable<RawInterpolationSpec> {

    public static final String VERSION = "3.0.0";

    public static final String FAMILY = "temporalinterpolation";
    public static final String METHOD = "generic";
    public static final AlgorithmDescriptor DESCRIPTOR = new AlgorithmDescriptor(FAMILY, METHOD, VERSION);

    public static final SsfInitialization DEF_ALGORITHM = SsfInitialization.SqrtDiffuse;
    public static final boolean DEF_FAST = true, DEF_RESCALE = true, DEF_LOG = false, DEF_DIFFUSE = false;

    public static final double DEF_EPS = 1e-5;

    public static final AggregationType DEF_AGGREGATION = AggregationType.Last;

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return DESCRIPTOR;
    }

    @lombok.NonNull
    private AggregationType interpolationType;
    private int observationPosition;
    private int frequencyRatio;

    @lombok.NonNull
    private ResidualsModel residualsModel;
    private boolean constant, trend;
    private Parameter parameter;
    @lombok.NonNull
    private IndexRange estimationRange;
    private boolean log, diffuseRegressors;
    private Double truncatedParameter;
    private boolean zeroInitialization, fast;

    private double estimationPrecision;
    private SsfInitialization algorithm;
    private boolean rescale;

    public boolean isParameterEstimation() {
        return (residualsModel == ResidualsModel.Ar1 || residualsModel == ResidualsModel.RwAr1)
                && parameter.getType() != ParameterType.Fixed;
    }

    public static class Builder implements Validatable.Builder<RawInterpolationSpec> {
    }

    public static Builder builder() {
        return new Builder()
                .interpolationType(DEF_AGGREGATION)
                .residualsModel(ResidualsModel.Ar1)
                .constant(true)
                .estimationRange(IndexRange.EMPTY)
                .fast(DEF_FAST)
                .algorithm(DEF_ALGORITHM)
                .rescale(DEF_RESCALE)
                .parameter(Parameter.undefined())
                .estimationPrecision(DEF_EPS)
                .frequencyRatio(0);
    }

    @Override
    public RawInterpolationSpec validate() throws IllegalArgumentException {
        if (interpolationType == AggregationType.Sum || interpolationType == AggregationType.Average) {
            throw new IllegalArgumentException(interpolationType.name() + " not allowed in interpolation (consider disaggregation)");
        }
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
        StringBuilder builder = new StringBuilder();
        builder.append("Interpolation [")
                .append(residualsModel.name())
                .append(']');
        return builder.toString();
    }

}
