package org.xbase.android.log;

public enum LogLevel {

    LOG_LEVEL_OFF(0), LOG_LEVEL_REPORT(1), LOG_LEVEL_ERROR(2), LOG_LEVEL_WARN(3), LOG_LEVEL_INFO(4), LOG_LEVEL_DEBUG(5);

    private int logLevel;

    LogLevel(int value) {
        this.logLevel = value;
    }

    public int getValue() {
        return logLevel;
    }

    @Override
    public String toString() {
        String level = "";
        switch (logLevel) {
            case 0:
                level = "off";
            break;
            case 1:
                level = "Report";
            break;
            case 2:
                level = "E";
            break;
            case 3:
                level = "W";
            break;
            case 4:
                level = "I";
            break;
            case 5:
                level = "D";
            break;
            default:
            break;
        }

        return level;
    }
}
