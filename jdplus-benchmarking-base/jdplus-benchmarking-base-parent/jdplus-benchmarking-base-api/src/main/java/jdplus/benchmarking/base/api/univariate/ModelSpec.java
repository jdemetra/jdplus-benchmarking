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

import jdplus.toolkit.base.api.data.AggregationType;
import jdplus.toolkit.base.api.data.Parameter;
import jdplus.toolkit.base.api.data.ParameterType;
import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Beta)
@lombok.Getter
@lombok.Builder(toBuilder = true, builderClassName = "Builder", buildMethodName = "build")
public final class ModelSpec {

    public static final boolean DEF_LOG = false, DEF_DIFFUSE = false, DEF_ZERO = false;

    public static final AggregationType DEF_AGGREGATION = AggregationType.Sum;

    public static final ModelSpec CHOWLIN = new Builder()
            .residualsModel(ResidualsModel.Ar1)
            .constant(true)
            .trend(false)
            //            .log(DEF_LOG)
            .parameter(Parameter.undefined())
            .zeroInitialization(DEF_ZERO)
            .diffuseRegressors(DEF_DIFFUSE)
            .build();

    public static final ModelSpec FERNANDEZ = new Builder()
            .residualsModel(ResidualsModel.Rw)
            .constant(false)
            .trend(false)
            //            .log(DEF_LOG)
            .parameter(Parameter.undefined())
            .zeroInitialization(DEF_ZERO)
            .diffuseRegressors(DEF_DIFFUSE)
            .build();

    public static final ModelSpec LITTERMAN = new Builder()
            .residualsModel(ResidualsModel.RwAr1)
            .constant(false)
            .trend(false)
            //            .log(DEF_LOG)
            .parameter(Parameter.undefined())
            .zeroInitialization(DEF_ZERO)
            .diffuseRegressors(DEF_DIFFUSE)
            .build();

    @lombok.NonNull
    private ResidualsModel residualsModel;
    private boolean constant, trend;
    private Parameter parameter;
//    private boolean log;
    private boolean diffuseRegressors;
    private boolean zeroInitialization;

    public boolean isParameterEstimation() {
        return (residualsModel == ResidualsModel.Ar1 || residualsModel == ResidualsModel.RwAr1)
                && parameter.getType() != ParameterType.Fixed;
    }

    public static final ModelSpec DEFAULT = builder().build();

    public static Builder builder() {
        return new Builder()
                .residualsModel(ResidualsModel.Ar1)
                .constant(true)
                .trend(false)
                .parameter(Parameter.undefined())
                .zeroInitialization(DEF_ZERO)
                .diffuseRegressors(DEF_DIFFUSE);
    }

    public void check() {
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
    }
}
