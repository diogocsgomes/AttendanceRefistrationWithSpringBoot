package pt.isec.pd.tp_pd.utils;

import pt.isec.pd.tp_pd.data.Event;

public class InputValidator {
	public boolean isInputEmpty(String... inputs) {
		for (String input : inputs) {
			if (input.isBlank()) return true;
		}
		return false;
	}

	public boolean isEmailValid(String email) {
		return email.matches("^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");
	}

	public boolean isPasswordValid(String password, String passwordConfirmation) {
		return password.equals(passwordConfirmation);
	}

	public boolean isHourValid(String hour) {
		return hour.matches("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$");
	}

	public void validateEvent(Event event) throws IllegalArgumentException {
		if (event.getCode() == null) {
            throw new IllegalArgumentException(
					"Application had problem with " +
							"generating event access code.\n" +
							"Please try again.");
        }

		if (isInputEmpty(
				event.getName(), event.getPlace(),
				event.getStartHour(), event.getEndHour(),
				event.getEventDate())) {
			throw new IllegalArgumentException("All fields must contain data.");
		}

		if (event.getName().length() > 100) {
			throw new IllegalArgumentException(
					"Name of event must be 100 characters or less.");
		}

		if (event.getPlace().length() > 100) {
			throw new IllegalArgumentException(
					"Place of event must be 100 characters or less.");
		}

		if (!isHourValid(event.getStartHour())) {
			throw new IllegalArgumentException(
					"Given start hour is not in proper format.\n" +
					"Proper format is HH:MM. In example 17:00");
		}

		if (!isHourValid(event.getEndHour())) {
			throw new IllegalArgumentException(
					"Given end hour is not in proper format.\n" +
					"Proper format is HH:MM. In example 17:00");
		}

//		if (!DateTimeFormatChecker.isTimePeriodValid(
//				event.getStartHour(),
//				event.getExpirationCodeDate(),
//				event.getEndHour())) {
//			throw new IllegalArgumentException(
//					"Given expiration code hour " +
//							"must be between start hour end end hour.");
//		}

		if (!DateTimeFormatChecker.isValidDateFormat(event.getEventDate())) {
			throw new IllegalArgumentException(
					"Given date is not in proper format.\n" +
					"Proper format is YYYY-MM-DD. In example 2023-07-28");
		}
	}
}