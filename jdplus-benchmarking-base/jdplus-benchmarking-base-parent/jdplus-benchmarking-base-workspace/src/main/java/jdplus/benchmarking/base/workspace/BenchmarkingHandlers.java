/*
 * Copyright 2021 National Bank of Belgium
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
package jdplus.benchmarking.base.workspace;

import jdplus.toolkit.base.api.DemetraVersion;
import jdplus.benchmarking.base.information.CholetteSpecMapping;
import jdplus.benchmarking.base.information.DentonSpecMapping;
import jdplus.benchmarking.base.information.TemporalDisaggregationSpecMapping;
import jdplus.toolkit.base.api.information.InformationSet;
import jdplus.toolkit.base.api.information.InformationSetSerializer;
import jdplus.toolkit.base.information.MultiTsDocumentMapping;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.toolkit.base.workspace.WorkspaceFamily;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.informationSet;
import static jdplus.toolkit.base.workspace.WorkspaceFamily.parse;
import jdplus.toolkit.base.workspace.file.spi.FamilyHandler;
import jdplus.benchmarking.base.core.benchmarking.univariate.CholetteDocument;
import jdplus.benchmarking.base.core.benchmarking.univariate.DentonDocument;
import jdplus.benchmarking.base.core.univariate.TemporalDisaggregationDocument;
import nbbrd.service.ServiceProvider;

/**
 *
 * @author palatej
 */
@lombok.experimental.UtilityClass
public class BenchmarkingHandlers {

    public static final LinearId TEMPDISAGG_FAMILY = new LinearId("temporal disaggregation", "regression model");
    public static final String TEMPDISAGG_REPOSITORY = "TsDisaggregationDoc";

    public final WorkspaceFamily MOD_DOC_TEMPDISAGG = parse("temporal disaggregation@documents@regression model");

    @ServiceProvider(FamilyHandler.class)
    public static final class DocTemporalDisaggregation implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_TEMPDISAGG,
                new InformationSetSerializer<TemporalDisaggregationDocument>() {
            @Override
            public InformationSet write(TemporalDisaggregationDocument object, boolean verbose) {
                return MultiTsDocumentMapping.write(object, TemporalDisaggregationSpecMapping.SERIALIZER, verbose);
            }

            @Override
            public TemporalDisaggregationDocument read(InformationSet info) {

                TemporalDisaggregationDocument doc = new TemporalDisaggregationDocument();
                MultiTsDocumentMapping.read(info, TemporalDisaggregationSpecMapping.SERIALIZER, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, TEMPDISAGG_REPOSITORY);

    }

    public static final LinearId CHOLETTE_FAMILY = new LinearId("benchmarking", "cholette");
    public static final String CHOLETTE_REPOSITORY = "CholetteDoc";

    public final WorkspaceFamily MOD_DOC_CHOLETTE = parse("benchmarking@documents@cholette");

    @ServiceProvider(FamilyHandler.class)
    public static final class DocCholette implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_CHOLETTE,
                new InformationSetSerializer<CholetteDocument>() {
            @Override
            public InformationSet write(CholetteDocument object, boolean verbose) {
                return MultiTsDocumentMapping.write(object, CholetteSpecMapping.SERIALIZER, verbose);
            }

            @Override
            public CholetteDocument read(InformationSet info) {

                CholetteDocument doc = new CholetteDocument();
                MultiTsDocumentMapping.read(info, CholetteSpecMapping.SERIALIZER, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, CHOLETTE_REPOSITORY);

    }

    public static final LinearId DENTON_FAMILY = new LinearId("benchmarking", "denton");
    public static final String DENTON_REPOSITORY = "DentonDoc";

    public final WorkspaceFamily MOD_DOC_DENTON = parse("benchmarking@documents@denton");

    @ServiceProvider(FamilyHandler.class)
    public static final class DocDenton implements FamilyHandler {

        @lombok.experimental.Delegate
        private final FamilyHandler delegate = informationSet(MOD_DOC_CHOLETTE,
                new InformationSetSerializer<DentonDocument>() {
            @Override
            public InformationSet write(DentonDocument object, boolean verbose) {
                return MultiTsDocumentMapping.write(object, DentonSpecMapping.SERIALIZER, verbose);
            }

            @Override
            public DentonDocument read(InformationSet info) {

                DentonDocument doc = new DentonDocument();
                MultiTsDocumentMapping.read(info, DentonSpecMapping.SERIALIZER, doc);
                return doc;
            }

            @Override
            public boolean match(DemetraVersion version) {
                return version == DemetraVersion.JD3;
            }

        }, DENTON_REPOSITORY);

    }

}
