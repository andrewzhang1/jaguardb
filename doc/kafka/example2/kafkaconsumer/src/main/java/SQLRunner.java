import java.io.IOException;

/**
 * Pick whether we want to run as producer or consumer.
 * This lets us have a single executable as a build target.
 */
public class SQLRunner {

    public final static String TOPIC = "jaguar-kafka-Example1";

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            throw new IllegalArgumentException("Must have either 'producer' or 'consumer' as argument");
        }
        switch (args[0]) {
            case "consumer":
                SQLConsumer.main(args);
                break;
            default:
                throw new IllegalArgumentException("Don't know how to do " + args[0]);
        }
    }

}
