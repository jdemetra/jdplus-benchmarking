/*
 * Copyright 2019 National Bank of Belgium.
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
package jdplus.benchmarking.base.core.univariate;

import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.GenericExplorable;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.functions.ObjectiveFunctionPoint;
import jdplus.toolkit.base.api.timeseries.TsData;
import jdplus.toolkit.base.api.timeseries.TsDomain;
import jdplus.toolkit.base.api.timeseries.regression.Variable;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Alpha)
public class TemporalDisaggregationResults implements GenericExplorable{

    @lombok.NonNull
    TsData originalSeries;
    
    @lombok.NonNull
    TsDomain disaggregationDomain;
    
    /**
     * Regression variables
     */
    private Variable[] indicators;
    
    
    /**
     * Regression estimation. The order correspond to the order of the variables
     * 
     */
    int hyperParametersCount;
    
    DiffuseConcentratedLikelihood likelihood;
    DiffuseLikelihoodStatistics stats;
    
    public DoubleSeq getCoefficients(){
        return likelihood.coefficients();
    }
    
    public Matrix getCoefficientsCovariance(){
        return likelihood.covariance(hyperParametersCount, true);
    }
    
    ObjectiveFunctionPoint maximum;
    
    ResidualsDiagnostics residualsDiagnostics;

    @lombok.NonNull
    TsData disaggregatedSeries;
    
    @lombok.NonNull
    TsData stdevDisaggregatedSeries;
    
    TsData regressionEffects;
    
}
