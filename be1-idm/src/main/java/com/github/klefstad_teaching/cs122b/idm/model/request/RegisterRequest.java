package com.github.klefstad_teaching.cs122b.idm.model.request;

import com.github.klefstad_teaching.cs122b.core.error.ResultError;
import com.github.klefstad_teaching.cs122b.core.result.IDMResults;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterRequest {
    String email;
    char[] password;

    public String getEmail() {
        return this.email;
    }

    public RegisterRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public char[] getPassword() {
        return this.password;
    }

    public RegisterRequest setPassword(char[] password) {
        this.password = password;
        return this;
    }

    public void validate() {
        if (this.email.length() > 32 || this.email.length() < 6) {
            throw new ResultError(IDMResults.EMAIL_ADDRESS_HAS_INVALID_LENGTH);
        }

        String email = "^[A-Za-z0-9]*@[A-Za-z0-9]*(\\.[A-Za-z]{2,})$";
        Pattern email_pattern = Pattern.compile(email);
        Matcher email_matcher = email_pattern.matcher(this.email);

        if (!email_matcher.matches()) {
            throw new ResultError((IDMResults.EMAIL_ADDRESS_HAS_INVALID_FORMAT));
        }

        if (this.password.length < 10 || this.password.length > 20) {
            throw new ResultError(IDMResults.PASSWORD_DOES_NOT_MEET_LENGTH_REQUIREMENTS);
        }

        String password = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{0,}$";
        Pattern password_pattern = Pattern.compile(password);
        CharSequence pwd = new String(this.password);
        Matcher password_matcher = password_pattern.matcher(pwd);

        if (!password_matcher.matches()) {
            throw new ResultError(IDMResults.PASSWORD_DOES_NOT_MEET_CHARACTER_REQUIREMENT);
        }

        for (char c : this.password) {
            if (!Character.isLetterOrDigit(c)) {
                throw new ResultError(IDMResults.PASSWORD_DOES_NOT_MEET_CHARACTER_REQUIREMENT);
            }
        }
    }
}
