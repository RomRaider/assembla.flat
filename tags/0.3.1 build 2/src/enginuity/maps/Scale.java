//This object defines the scaling factor and offset for calculating real values

package enginuity.maps;

import java.io.Serializable;

public class Scale implements Serializable {
    
    public static final int LINEAR  = 1;
    public static final int INVERSE = 2;
    
    private String unit            = "0x";
    private String expression      = "x";
    private String byteExpression  = "x";
    private String format          = "#";
    private double coarseIncrement = 2;
    private double fineIncrement   = 1;
    
    public Scale() {
    }
    
    public String toString() {
       return "\n      ---- Scale ----" +
              "\n      Expression: " + getExpression() +
              "\n      Unit: " + getUnit() +
              "\n      ---- End Scale ----";
    
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public double getCoarseIncrement() {
        return coarseIncrement;
    }

    public void setCoarseIncrement(double increment) {
        this.coarseIncrement = increment;
    }
    
    public boolean isReady() {
        if (unit == null) return false;
        else if (expression == null) return false;
        else if (format == null) return false;
        else if (coarseIncrement < 1) return false;
        
        return true;
    }

    public String getByteExpression() {
        return byteExpression;
    }

    public void setByteExpression(String byteExpression) {
        this.byteExpression = byteExpression;
    }

    public double getFineIncrement() {
        return fineIncrement;
    }

    public void setFineIncrement(double fineIncrement) {
        this.fineIncrement = fineIncrement;
    }
}