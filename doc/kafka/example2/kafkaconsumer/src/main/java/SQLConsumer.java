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


public class SQLConsumer {

    public static void main(String[] args) throws IOException 
	{

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
        int timeouts = 0;
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(200000);

            if (records.count() == 0) {
                timeouts++;
            } else {
                System.out.printf("Got %d messages after %d timeouts\n", records.count(), timeouts);
                System.out.printf("Begin inserting %d messages to Jaguar database:", records.count());
                timeouts = 0;
            }

            for (ConsumerRecord<String, String> record : records) {
                if( SQLRunner.TOPIC.equals(record.topic()) ) {
		            String[] parts = (record.value()).split(",");
		            String sqlString = "INSERT INTO pricetable (item, price) VALUES ('" + parts[0] + "' , '" + parts[1] + "');";
                    System.out.println("Insert (" + parts[0] + ", " + parts[1] + "):");
	                System.out.println(sqlString);
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
    public static Statement getSQLStatement() throws SQLException 
	{

        if( singltonStatement == null ) {
            DataSource ds = new JaguarDataSource("192.168.7.120", 5555, "Example1");
            Connection connection = ds.getConnection("admin", "jaguar");  // username and password
            singltonStatement = connection.createStatement();
        }
        return singltonStatement;
    }
}
