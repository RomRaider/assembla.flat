/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2010 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.ecu.ui.handler.maf;

import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;
import com.romraider.logger.ecu.ui.tab.maf.MafTab;
import static java.lang.Math.abs;
import static java.lang.System.currentTimeMillis;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import javax.swing.SwingUtilities;
import java.util.Set;

public final class OLMafUpdateHandler implements DataUpdateHandler {
    private static final Logger LOGGER = getLogger(OLMafUpdateHandler.class);
    private static final String MAFV = "P18";
    private static final String OL_FUELING_BASE_16 = "E124";
    private static final String OL_FUELING_BASE_32 = "E123";
    private MafTab mafTab;
    private double lastMafv;
    private long lastUpdate;

    public synchronized void registerData(LoggerData loggerData) {
    }

    public synchronized void handleDataUpdate(Response response) {
        if (mafTab.isRecordData() && ( containsData(response, MAFV, OL_FUELING_BASE_16) || containsData(response, MAFV, OL_FUELING_BASE_32) )) {
            boolean valid = true;

            // cl/ol check
            if ((containsData(response, "E3") || containsData(response, "E33"))) {
                double clOl = -1;
                if (containsData(response, "E3")) {
                    clOl = (int) findValue(response, "E3");
                    LOGGER.trace("MAF:[CL/OL:E3]:  " + clOl);
                }
                if (containsData(response, "E33")) {
                    clOl = (int) findValue(response, "E33");
                    LOGGER.trace("MAF:[CL/OL:E33]: " + clOl);
                }
                if (!mafTab.methodIsCl()) {
                	valid = mafTab.isValidClOl(clOl);
                } else {
                	valid = false;
                }
                LOGGER.trace("MAF:[CL/OL]:     " + valid);
            }

            // afr check
            String afrId = mafTab.getSelectedAfrSource().getId();
            if (valid && containsData(response, afrId)) {
                double afr = findValue(response, afrId);
                LOGGER.trace("MAF:[AFR:" + afrId + "]: " + afr);
                valid = mafTab.isValidAfr(afr);
                LOGGER.trace("MAF:[AFR]:     " + valid);
            }

            // rpm check
            if (valid && containsData(response, "P8")) {
                double rpm = findValue(response, "P8");
                LOGGER.trace("MAF:[RPM:P8]: " + rpm);
                valid = mafTab.isValidRpm(rpm);
                LOGGER.trace("MAF:[RPM]:    " + valid);
            }

            // maf check
            if (valid && containsData(response, "P12")) {
                double maf = findValue(response, "P12");
                LOGGER.trace("MAF:[MAF:P12]: " + maf);
                valid = mafTab.isValidMaf(maf);
                LOGGER.trace("MAF:[MAF]:     " + valid);
            }

            // intake air temp check
            if (valid && containsData(response, "P11")) {
                double temp = findValue(response, "P11");
                LOGGER.trace("MAF:[IAT:P11]: " + temp);
                valid = mafTab.isValidIntakeAirTemp(temp);
                LOGGER.trace("MAF:[IAT]:     " + valid);
            }

            // coolant temp check
            if (valid && containsData(response, "P2")) {
                double temp = findValue(response, "P2");
                LOGGER.trace("MAF:[CT:P2]: " + temp);
                valid = mafTab.isValidCoolantTemp(temp);
                LOGGER.trace("MAF:[CT]:    " + valid);
            }

            // dMAFv/dt check
            if (valid && containsData(response, "P18")) {
                double mafv = findValue(response, "P18");
                long now = currentTimeMillis();
                double mafvChange = abs((mafv - lastMafv) / (now - lastUpdate) * 1000);
                LOGGER.trace("MAF:[dMAFv/dt]: " + mafvChange);
                valid = mafTab.isValidMafvChange(mafvChange);
                LOGGER.trace("MAF:[dMAFv/dt]: " + valid);
                lastMafv = mafv;
                lastUpdate = now;
            }

            // tip-in throttle check
            if (valid && (containsData(response, "E23") || containsData(response, "E54"))) {
                double tipIn = -1;
                if (containsData(response, "E23")) {
                    tipIn = findValue(response, "E23");
                    LOGGER.trace("MAF:[TIP:E23]: " + tipIn);
                }
                if (containsData(response, "E54")) {
                    tipIn = findValue(response, "E54");
                    LOGGER.trace("MAF:[TIP:E54]: " + tipIn);
                }
                valid = mafTab.isValidTipInThrottle(tipIn);
                LOGGER.trace("MAF:[TIP]:     " + valid);
            }

            if (valid) {
                final double mafv = findValue(response, MAFV);                
                final double afrActual = findValue(response, mafTab.getSelectedAfrSource().getId());
                double afrTarget = 0.0;
                if (containsData(response, OL_FUELING_BASE_16)) {
                	afrTarget = findValue(response, OL_FUELING_BASE_16);
                } else if (containsData(response, OL_FUELING_BASE_32)) {
                	afrTarget = findValue(response, OL_FUELING_BASE_32);
                }                
                final double afrError = ( afrTarget - afrActual) / afrTarget;
                
                LOGGER.trace("MAF Data: " + mafv + "v, " + afrError + "%");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mafTab.addData(mafv, afrError);
                    }
                });
                
            }
        }
    }

    private boolean containsData(Response response, String... ids) {
        Set<LoggerData> datas = response.getData();
        for (String id : ids) {
            boolean found = false;
            for (LoggerData data : datas) {
                if (data.getId().equals(id)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    private double findValue(Response response, String id) {
        for (LoggerData loggerData : response.getData()) {
            if (id.equals(loggerData.getId())) {
                return response.getDataValue(loggerData);
            }
        }
        throw new IllegalStateException("Expected data item " + id + " not in response.");
    }

    public synchronized void deregisterData(LoggerData loggerData) {
    }

    public synchronized void cleanUp() {
    }

    public synchronized void reset() {
    }

    public void setMafTab(MafTab mafTab) {
        this.mafTab = mafTab;
    }
}
