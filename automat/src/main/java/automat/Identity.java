package automat;

public enum Identity {
    WATCHERBGYPSY("watcherbgypsy","password");

    private final String username;
    private final String password;

    Identity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
