import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * This producer will send jaguar sql statement to consumer which is supposed to run jaguar sql through jaguar JDBC
 */
public class SQLProducer {

    public static void main(String[] args) throws IOException {

        // set up the producer
        KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("producer.props").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            producer = new KafkaProducer<>(properties);
        }

    	try {
            long start = System.currentTimeMillis();
	    long cnt = Integer.parseInt(args[1]);
            System.out.println( "Data Count="+ cnt );
            for (int i = 0; i < cnt; i++) {
                String csvstr = String.format("%d,%d", i, System.currentTimeMillis());
                producer.send(new ProducerRecord<String, String>(SQLRunner.TOPIC, csvstr));
            }
            long middle = System.currentTimeMillis();
            producer.flush();
            long end = System.currentTimeMillis();
            System.out.println( "flushtime: " +  (end - middle) + ", sendtime: " + (middle-start) );

        } catch (Throwable throwable) {
            System.out.printf("%s", throwable.getStackTrace());
        } finally {
            producer.close();
        }

    }
}
