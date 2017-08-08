import com.google.common.io.Resources;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import com.jaguar.jdbc.JaguarDataSource;


/**
 * This consumer receives sql messages and run to
 */
public class SQLConsumer {

    public static void main(String[] args) throws IOException {

        // and the consumer
        KafkaConsumer<String, String> consumer;
        try (InputStream props = Resources.getResource("consumer.props").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            if (properties.getProperty("group.id") == null) {
                properties.setProperty("group.id", "group-" + new Random().nextInt(100000));
            }
            consumer = new KafkaConsumer<>(properties);
        }

        consumer.subscribe(Arrays.asList(SQLRunner.TOPIC));
	System.out.println(SQLRunner.TOPIC);
        int timeouts = 0;
	int loopid = 0;
        while (true) {
	    loopid++;
	    System.out.println(loopid);
            ConsumerRecords<String, String> records = consumer.poll(200000);

            if (records.count() == 0) {
                timeouts++;
		System.out.println(timeouts);
            } else {
                System.out.printf("Got %d records after %d timeouts\n", records.count(), timeouts);
                timeouts = 0;
            }

            for (ConsumerRecord<String, String> record : records) {
		System.out.println(record.topic());
                if( SQLRunner.TOPIC.equals(record.topic()) ) {
                    String sqlString = record.value();
                    System.out.println("Running SQL: " + sqlString);
                    try {
                        getSQLStatement().executeUpdate(sqlString);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    throw new IllegalStateException("unsupported topic:" + record.topic());
                }
            }
        }
    }

    private static Statement singltonStatement = null;
    public static Statement getSQLStatement() throws SQLException {

        if( singltonStatement == null ) {
            DataSource ds = new JaguarDataSource("192.168.7.120", 5555, "test");
            Connection connection = ds.getConnection("admin", "jaguar");  // username and password
            singltonStatement = connection.createStatement();
        }
        return singltonStatement;
    }

}
