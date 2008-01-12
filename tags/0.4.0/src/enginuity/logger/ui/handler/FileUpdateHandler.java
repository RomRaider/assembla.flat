package enginuity.logger.ui.handler;

import enginuity.Settings;
import enginuity.logger.definition.ConvertorUpdateListener;
import enginuity.logger.definition.EcuData;
import enginuity.logger.definition.EcuDataConvertor;
import enginuity.logger.definition.EcuSwitch;
import enginuity.logger.io.file.FileLogger;
import enginuity.logger.io.file.FileLoggerImpl;
import static enginuity.util.ParamChecker.checkNotNull;

import static java.util.Collections.synchronizedMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class FileUpdateHandler implements DataUpdateHandler, ConvertorUpdateListener {
    private final FileLogger fileLogger;
    private final Map<EcuData, Integer> ecuDatas = synchronizedMap(new LinkedHashMap<EcuData, Integer>());
    private Line currentLine = new Line(ecuDatas.keySet());

    public FileUpdateHandler(Settings settings) {
        checkNotNull(settings);
        fileLogger = new FileLoggerImpl(settings);
    }

    public synchronized void registerData(EcuData ecuData) {
        if (ecuDatas.keySet().contains(ecuData)) {
            ecuDatas.put(ecuData, ecuDatas.get(ecuData) + 1);
        } else {
            ecuDatas.put(ecuData, 1);
            resetLine();
            writeHeaders();
        }
    }

    public synchronized void handleDataUpdate(EcuData ecuData, byte[] bytes, long timestamp) {
        EcuDataConvertor convertor = ecuData.getSelectedConvertor();
        double value = convertor.convert(bytes);
        checkStartStopFileLogging(ecuData, (int) value);
        if (fileLogger.isStarted()) {
            currentLine.updateParamValue(ecuData, convertor.format(value), timestamp);
            if (currentLine.isFull()) {
                fileLogger.writeLine(currentLine.values());
                resetLine();
            }
        }
    }

    public synchronized void deregisterData(EcuData ecuData) {
        if (ecuDatas.keySet().contains(ecuData) && ecuDatas.get(ecuData) > 1) {
            ecuDatas.put(ecuData, ecuDatas.get(ecuData) - 1);
        } else {
            ecuDatas.remove(ecuData);
            resetLine();
            writeHeaders();
        }
    }

    public void cleanUp() {
        fileLogger.stop();
    }

    public void notifyConvertorUpdate(EcuData updatedEcuData) {
        resetLine();
        writeHeaders();
    }

    private void resetLine() {
        currentLine = new Line(ecuDatas.keySet());
    }

    private void checkStartStopFileLogging(EcuData ecuData, int value) {
        if (ecuData instanceof EcuSwitch) {
            EcuSwitch ecuSwitch = (EcuSwitch) ecuData;
            if (ecuSwitch.isFileLogController()) {
                if (value == 1 && !fileLogger.isStarted()) {
                    fileLogger.start();
                    writeHeaders();
                } else if (value == 0 && fileLogger.isStarted()) {
                    fileLogger.stop();
                }
            }
        }
    }

    private void writeHeaders() {
        if (fileLogger.isStarted()) {
            fileLogger.writeLine(currentLine.headers());
        }
    }

    private static final class Line {
        private static final char DELIMITER = ',';
        private final Map<EcuData, String> ecuDataValues;
        private long lastTimestamp;

        public Line(Set<EcuData> ecuParams) {
            this.ecuDataValues = new LinkedHashMap<EcuData, String>();
            for (EcuData ecuParam : ecuParams) {
                ecuDataValues.put(ecuParam, null);
            }
        }

        public void updateParamValue(EcuData ecuData, String value, long timestamp) {
            if (ecuDataValues.containsKey(ecuData)) {
                ecuDataValues.put(ecuData, value);
                lastTimestamp = timestamp;
            }
        }

        public boolean isFull() {
            for (EcuData ecuData : ecuDataValues.keySet()) {
                if (ecuDataValues.get(ecuData) == null) {
                    return false;
                }
            }
            return true;
        }

        public String values() {
            StringBuilder buffer = new StringBuilder();
            buffer.append(lastTimestamp);
            for (EcuData ecuData : ecuDataValues.keySet()) {
                String value = ecuDataValues.get(ecuData);
                buffer.append(DELIMITER).append(value);
            }
            return buffer.toString();
        }

        public String headers() {
            StringBuilder buffer = new StringBuilder();
            buffer.append("Time");
            for (EcuData ecuData : ecuDataValues.keySet()) {
                buffer.append(DELIMITER).append(ecuData.getName()).append(" (").append(ecuData.getSelectedConvertor().getUnits()).append(')');
            }
            return buffer.toString();
        }
    }

}
