package com.romraider.logger.utec.plugin;

import com.romraider.logger.ecu.external.ExternalDataItem;
import com.romraider.logger.utec.gui.mapTabs.UtecDataManager;

public class LoadExternalDataItem implements ExternalDataItem {

    public String getName() {
        return "TXS LOAD";
    }

    public String getDescription() {
        return "TXS Utec Load";
    }

    public String getUnits() {
        return "n/a";
    }

    public double getData() {
        return UtecDataManager.getLoadData();
    }

}