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
package jdplus.benchmarking.base.api.multivariate;

import java.util.List;
import jdplus.benchmarking.base.api.benchmarking.multivariate.ContemporaneousConstraint;
import jdplus.benchmarking.base.api.benchmarking.univariate.CholetteSpec;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.api.processing.AlgorithmDescriptor;
import jdplus.toolkit.base.api.processing.ProcSpecification;
import jdplus.toolkit.base.api.ssf.SsfInitialization;
import jdplus.toolkit.base.api.util.Validatable;
import nbbrd.design.Development;

/**
 *
 * @author LEMASSO
 */
@Development(status = Development.Status.Beta)
@lombok.Value
@lombok.Builder(toBuilder=true, buildMethodName="buildWithoutValidation")
public class MultivariateChowLinSpec implements ProcSpecification, Validatable<MultivariateChowLinSpec> {

    public static final AlgorithmDescriptor ALGORITHM = new AlgorithmDescriptor("temporaldisaggregation", "multivariatechowlin", null);

    public static enum errorsVarianceMethod {
        fromUnivariate, allEquals, userDefined
    };

    public static MultivariateChowLinSpec.errorsVarianceMethod DEF_VAR_METHOD = MultivariateChowLinSpec.errorsVarianceMethod.fromUnivariate;
    public static final boolean DEF_AVERAGE = false, DEF_FIXEDRHOS = true, DEF_DIFFUSE = false, DEF_ZERO = false;
    public static final int DEF_PERIOD = 4, DEF_TRUNCATEDRHOS = -1;
    public static final SsfInitialization DEF_ALGORITHM = SsfInitialization.SqrtDiffuse;
    
    private int defaultPeriod;
    private boolean average;
    private boolean[] constant, trend;
    private double[] rhos;
    private errorsVarianceMethod varMethod;
    private Matrix var;
    private boolean fixedRhos;
    private double truncatedRhos;
    private SsfInitialization algorithm;
    private boolean diffuseRegressors;
    private boolean zeroInitialization;
    
    @lombok.NonNull
    @lombok.Singular
    private List<ContemporaneousConstraint> contemporaneousConstraints;

    @Override
    public AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM;
    }

    @Override
    public MultivariateChowLinSpec validate() throws IllegalArgumentException {
        for (int i = 0; i < rhos.length; ++i){
            if (rhos[i] <= -1 || rhos[i] > 1){
                throw new IllegalArgumentException("All rho's should be in ]-1,1]");
            }      
        }
        return this;   
    }
    
    public static class Builder implements Validatable.Builder<MultivariateChowLinSpec>{
    }
    
    public static Builder builder(){
        
        return new Builder()
                .defaultPeriod(DEF_PERIOD)
                .average(DEF_AVERAGE)
                .rhos(null)
                .constant(null)
                .trend(null)
                .varMethod(DEF_VAR_METHOD)
                .var(null)
                .fixedRhos(DEF_FIXEDRHOS)
                .truncatedRhos(DEF_TRUNCATEDRHOS)
                .zeroInitialization(DEF_ZERO)
                .algorithm(DEF_ALGORITHM)
                .diffuseRegressors(DEF_DIFFUSE);            
    }    
}
