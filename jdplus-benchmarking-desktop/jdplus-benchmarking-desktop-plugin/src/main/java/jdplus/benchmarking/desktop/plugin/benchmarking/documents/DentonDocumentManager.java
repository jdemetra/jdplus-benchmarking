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
import javax.swing.Icon;
import jdplus.benchmarking.base.core.benchmarking.univariate.DentonDocument;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(
        service = WorkspaceItemManager.class,
        position = 1000)
public class DentonDocumentManager extends AbstractWorkspaceItemManager<DentonDocument> {

    public static final LinearId ID = new LinearId("Benchmarking", "Univariate", "Denton");
    public static final String PATH = "denton";
    public static final String ITEMPATH = "denton.item";
    public static final String CONTEXTPATH = "denton.context";

    @Override
    protected String getItemPrefix() {
        return "Denton";
    }

    @Override
    public Id getId() {
        return ID;
    }

    @Override
    public DentonDocument createNewObject() {
        return new DentonDocument();
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
        return DentonDocument.class;
    }

    @Override
    public Icon getManagerIcon() {
        return ImageUtilities.loadImageIcon("jdplus/benchmarking/desktop/plugin/resource-monitor_16x16.png", false);
    }
}
