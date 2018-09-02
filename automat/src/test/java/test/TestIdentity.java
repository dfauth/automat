package test;

import automat.Identity;

public enum TestIdentity implements Identity {
    WATCHERBGYPSY("watcherbgypsy","password");

    private final String username;
    private final String password;

    TestIdentity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String password() {
        return password;
    }

    public String username() {
        return username;
    }
}
