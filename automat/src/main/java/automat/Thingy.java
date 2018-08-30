package automat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Thingy {

    private static final Logger logger = LogManager.getLogger(Thingy.class);

    public static Thingy given() {
        return new Thingy();
    }

}
