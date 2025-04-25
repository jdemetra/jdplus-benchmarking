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

import nbbrd.design.Development;

/**
 *
 * @author Jean Palate
 */
@Development(status = Development.Status.Release)
@lombok.Value
@lombok.AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class IndexRange {

    public static final IndexRange EMPTY = new IndexRange(0, 0);

    int start, end;

    public boolean contains(int element) {
        return start <= element && element < end;
    }

    public boolean isEmpty() {
        return this == EMPTY;
    }

    public int size() {
        return end - start;
    }

    public static IndexRange of(int start, int end) {
        if (start >= end) {
            return EMPTY;
        } else {
            return new IndexRange(start, end);
        }
    }
}
