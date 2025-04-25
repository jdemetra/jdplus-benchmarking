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
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.util.Validatable;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Getter
@lombok.Builder(toBuilder = true, builderClassName = "Builder")
public final class AlgorithmSpec {

    public static final SsfInitialization DEF_ALGORITHM = SsfInitialization.SqrtDiffuse;
    public static final boolean DEF_FAST = true, DEF_RESCALE = true;

    public static final AlgorithmSpec DEFAULT = builder().build();

    private SsfInitialization algorithm;
    private boolean fast;
    private boolean rescale;

    public static Builder builder() {
        return new Builder()
                .algorithm(DEF_ALGORITHM)
                .fast(DEF_FAST)
                .rescale(DEF_RESCALE);
    }

}
