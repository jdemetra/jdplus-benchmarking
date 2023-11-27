/*
 * Copyright 2022 National Bank of Belgium
 * 
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * https://joinup.ec.europa.eu/software/page/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.base.core.univariate;

import jdplus.benchmarking.base.api.univariate.ADLSpec.XAR;

/**
 * @author palatej
 */
@lombok.Value
@lombok.Builder(builderClassName = "Builder", toBuilder = true)
public class ADLDefinition {
    
    public static ADLDefinition DEFAULT=adl_11(.9);
    

    @lombok.With
    double phi;
    boolean mean;
    boolean trend;
    XAR xar;

    public static ADLDefinition adl_11(double phi) {
        return builder()
                .phi(phi)
                .mean(true)
                .trend(false)
                .xar(XAR.FREE)
                .build();
    }

    public static ADLDefinition adl_10(double phi){
        return builder()
                .phi(phi)
                .mean(true)
                .trend(false)
                .xar(XAR.NONE)
                .build();
    }

    public static ADLDefinition chowLin(double phi) {
        return builder()
                .phi(phi)
                .mean(true)
                .trend(false)
                .xar(XAR.SAME)
                .build();
    }

    public static ADLDefinition fernandez() {
        return builder()
                .phi(1)
                .mean(false)
                .trend(false)
                .xar(XAR.SAME)
                .build();
    }
}
