package com.shiftmanagerserver.entities;

public class Constraint {
    private Shift shift;
    private ConstraintType constraintType;
    private String konanId;

    public Constraint() {
    }

    public Constraint(String konanId, Shift shift) {
        this.konanId = konanId;
        this.shift = shift;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public ConstraintType getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(ConstraintType constraintType) {
        this.constraintType = constraintType;
    }

    public String konanId() {
        return konanId;
    }

    public void setKonanId(String konanId) {
        this.konanId = konanId;
    }
}