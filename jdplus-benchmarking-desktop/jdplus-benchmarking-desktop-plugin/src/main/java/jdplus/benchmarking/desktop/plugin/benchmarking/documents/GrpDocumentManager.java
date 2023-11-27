/*
 * Copyright 2013 National Bank of Belgium
 *
 * Licensed under the EUPL, Version 1.1 or – as soon they will be approved 
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and 
 * limitations under the Licence.
 */
package jdplus.benchmarking.desktop.plugin.benchmarking.documents;

import jdplus.toolkit.desktop.plugin.workspace.AbstractWorkspaceItemManager;
import jdplus.toolkit.desktop.plugin.workspace.WorkspaceItemManager;
import jdplus.toolkit.base.api.util.Id;
import jdplus.toolkit.base.api.util.LinearId;
import jdplus.benchmarking.base.core.benchmarking.univariate.GRPDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(
        service = WorkspaceItemManager.class, position = 1500)
public class GrpDocumentManager extends AbstractWorkspaceItemManager<GRPDocument> {

    public static final LinearId ID = new LinearId("Benchmarking", "Univariate", "GRP");
    public static final String PATH = "grp";
    public static final String ITEMPATH = "grp.item";
    public static final String CONTEXTPATH = "grp.context";

    @Override
    protected String getItemPrefix() {
        return "GRP";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public GRPDocument createNewObject() {
        return new GRPDocument();
    }

    @Override
    public ItemType getItemType() {
        return ItemType.Doc;
    }

    @Override
    public String getActionsPath() {
        return PATH;
    }

    @Override
    public Status getStatus() {
        return Status.Certified;
    }

    @Override
    public Class getItemClass() {
        return GRPDocument.class;
    }

}
