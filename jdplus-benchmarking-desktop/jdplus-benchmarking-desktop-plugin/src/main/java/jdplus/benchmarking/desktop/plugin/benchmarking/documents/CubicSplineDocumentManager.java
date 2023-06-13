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
import jdplus.benchmarking.base.core.benchmarking.univariate.CubicSplineDocument;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jean Palate
 */
@ServiceProvider(
        service = WorkspaceItemManager.class, position = 1500)
public class CubicSplineDocumentManager extends AbstractWorkspaceItemManager<CubicSplineDocument> {

    public static final LinearId ID = new LinearId("Benchmarking", "Univariate", "CubicSpline");
    public static final String PATH = "cholette";
    public static final String ITEMPATH = "cholette.item";
    public static final String CONTEXTPATH = "cholette.context";

    @Override
    protected String getItemPrefix() {
        return "CubicSpline";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public CubicSplineDocument createNewObject() {
        return new CubicSplineDocument();
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
        return CubicSplineDocument.class;
    }

}
