package com.shiftmanagerserver.converters;

import com.shiftmanagerserver.dto.Preference;
import com.shiftmanagerserver.dto.ShiftDTO;
import com.shiftmanagerserver.dto.ShiftPreferenceDTO;
import com.shiftmanagerserver.dto.UserDTO;
import com.shiftmanagerserver.entities.Shift;
import com.shiftmanagerserver.entities.ShiftPreference;
import com.shiftmanagerserver.model.User;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConverterUtils {

    private ConverterUtils() {
    }

    public static ShiftPreferenceDTO convert(ShiftPreference shift) {
        return new ShiftPreferenceDTO(
                shift.uuid(),
                convert(shift.preference()),
                getShiftStartTime(shift.shift()),
                getShiftStartTime(shift.shift()));
    }

    public static UserDTO convert(User user) {
        Set<ShiftPreferenceDTO> preferences = user.preferences().stream()
                .map(ConverterUtils::convert)
                .collect(Collectors.toSet());
        return new UserDTO(UUID.fromString(user.id()),
                preferences);
    }

    public static ShiftDTO convert(Shift shift) {
        return new ShiftDTO(shift.uuid(),
                getShiftStartTime(shift),
                getShiftEndTime(shift)
        );
    }

    public static Preference convert(String preference) {
        return switch (preference) {
            case "CANNOT" -> Preference.CANNOT;
            case "PREFER_NOT" -> Preference.PREFER_NOT;
            case "PREFER" -> Preference.PREFER;
            case "MUST" -> Preference.MUST;
            default -> Preference.NO_PREFERENCE;
        };
    }

    private static OffsetDateTime getShiftStartTime(Shift shift) {
        return OffsetDateTime.of(
                shift.date(),
                LocalTime.of(shift.isDay() ? 8 : 20, shift.isDay() ? 45 : 0),
                ZoneOffset.ofHours(3));
    }

    private static OffsetDateTime getShiftEndTime(Shift shift) {
        return OffsetDateTime.of(
                shift.date(),
                LocalTime.of(shift.isDay() ? 20 : 8, shift.isDay() ? 0 : 45),
                ZoneOffset.ofHours(3));
    }
}
