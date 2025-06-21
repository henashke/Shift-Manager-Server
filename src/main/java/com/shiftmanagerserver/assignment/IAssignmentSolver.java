package com.shiftmanagerserver.assignment;

import com.shiftmanagerserver.entities.Constraint;
import com.shiftmanagerserver.entities.Shift;
import com.shiftmanagerserver.entities.User;

import java.util.Map;
import java.util.Set;

public interface IAssignmentSolver {

    void findAssignment(Map<User, Set<Constraint>> constraints, Set<Shift> shifts);
}
