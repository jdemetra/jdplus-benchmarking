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
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.IndexRange;
import jdplus.toolkit.base.api.data.DoubleSeq;
import jdplus.toolkit.base.api.information.GenericExplorable;
import nbbrd.design.Development;
import jdplus.toolkit.base.api.math.functions.ObjectiveFunctionPoint;
import jdplus.toolkit.base.api.math.matrices.Matrix;
import jdplus.toolkit.base.core.math.matrices.FastMatrix;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseConcentratedLikelihood;
import jdplus.toolkit.base.core.stats.likelihood.DiffuseLikelihoodStatistics;

/**
 *
 * @author Jean Palate <jean.palate@nbb.be>
 */
@lombok.Value
@lombok.Builder
@Development(status = Development.Status.Alpha)
public class RawTemporalDisaggregationResults implements GenericExplorable{

    @lombok.NonNull
    DoubleSeq series;
    
    FastMatrix regressors;
    
    int disaggregationRatio;
    
    IndexRange estimationRange;
    
    int hyperParametersCount;
    
    /**
     * Regression estimation. The order correspond to the columns of regressors
     * 
     */
    DiffuseConcentratedLikelihood likelihood;
    DiffuseLikelihoodStatistics stats;
    
    public DoubleSeq getCoefficients(){
        return likelihood.coefficients();
    }
    
    public Matrix getCoefficientsCovariance(){
        return likelihood.covariance(hyperParametersCount, true);
    }
    
    ObjectiveFunctionPoint maximum;
    
    RawResidualsDiagnostics residualsDiagnostics;

    @lombok.NonNull
    DoubleSeq disaggregatedSeries;
    
    @lombok.NonNull
    DoubleSeq stdevDisaggregatedSeries;
    
    DoubleSeq regressionEffects;
    
}
