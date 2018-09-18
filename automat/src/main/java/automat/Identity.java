package automat;

import java.util.Optional;

public interface Identity {

    String password();

    String username();
}

class IdentityBean {

    private String password;
    private String username;

    public static IdentityBean of(Optional<Identity> identity) {
        return identity.map(i -> of(i)).orElseThrow(()->new IllegalStateException("No identity found"));
    }

    public static IdentityBean of(Identity identity) {
        return new IdentityBean(identity);
    }

    public IdentityBean(Identity identity) {
        this(identity.username(), identity.password());
    }

    public IdentityBean(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
