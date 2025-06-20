package com.shiftmanagerserver.converters;

import com.shiftmanagerserver.entities.Shift;
import com.shiftmanagerserver.entities.ShiftPreference;
import com.shiftmanagerserver.dto.ShiftDTO;
import com.shiftmanagerserver.dto.ShiftPreferenceDTO;
import com.shiftmanagerserver.dto.UserDTO;
import com.shiftmanagerserver.model.User;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConverterUtils {

    public static ShiftPreferenceDTO convert(ShiftPreference shift) {
        return new ShiftPreferenceDTO(
                shift.uuid(),
                null,
                null,
                null);
    }

    public static UserDTO convert(User user) {
        Set<ShiftPreferenceDTO> preferences = user.preferences().stream()
                .map(ConverterUtils::convert)
                .collect(Collectors.toSet());
        return new UserDTO(UUID.fromString(user.id()),
                preferences);
    }

    public static ShiftDTO convert(Shift shift) {
        return new ShiftDTO(shift.uuid(),null,null);
    }
}
