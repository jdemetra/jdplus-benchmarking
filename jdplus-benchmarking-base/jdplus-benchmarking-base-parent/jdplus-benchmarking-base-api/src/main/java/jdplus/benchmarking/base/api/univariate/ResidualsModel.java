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

/**
 *
 * @author Jean Palate
 */
public enum ResidualsModel {
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
