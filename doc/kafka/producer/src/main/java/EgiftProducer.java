import com.google.common.io.Resources;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * This producer will send jaguar sql statement to consumer which is supposed to run jaguar sql through jaguar JDBC
 */
public class EgiftProducer {
	public static void main(String[] args) throws IOException {
		// set up the producer
		KafkaProducer<String, String> producer;
		try (InputStream props = Resources.getResource("producer.props").openStream()) {
			Properties properties = new Properties();
			properties.load(props);
			producer = new KafkaProducer<>(properties);
		}
		
		try {
			String [] pt = new String[]{"50元星礼卡","80元星礼卡","100元星礼卡","200元星礼卡","300元星礼卡","500元星礼卡","美式咖啡","拿铁","抹茶拿铁","焦糖玛奇朵","当季特饮","当季特饮-2"};		
			String [] pt2 = new String[]{"svc","svc","svc","svc","svc","svc","coupon","coupon","coupon","coupon","coupon","coupon"};
			char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
			Random random = new Random();
			
			long start = System.currentTimeMillis();
			long cnt = Integer.parseInt(args[1]);
			System.out.println( "Data Count="+ cnt );
			for (int i = 0; i < cnt; i++) {
				// generate random keys
				StringBuilder sb = new StringBuilder();
				StringBuilder sb2 = new StringBuilder();
				for (int j = 0; j < 64; j++) {
					char c = chars[random.nextInt(62)];
					sb.append(c);
				}
				for (int j = 0; j < 28; j++) {
					char c = chars[random.nextInt(62)];
					sb2.append(c);
				}				
				int prod_id = random.nextInt( 20 )+1;
				String code_no = sb.toString(); 
				int status = random.nextInt( 10 )+1;
				SimpleDateFormat flog_time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				String log_time = flog_time.format(new Date());
				int uuid = random.nextInt( 9999999 );
				// generate random values
				String card_id = sb2.toString();
				long prod_no = random.nextInt( 2000000000 );
				int iprod = random.nextInt( 12 );
				String prod_name = pt[ iprod ];
				String prod_type = pt2[ iprod ];
				double price = random.nextInt( 500 ) + random.nextDouble();
				long log_time_ms = flog_time.parse( log_time ).getTime();
				int log_time_y = flog_time.parse( log_time ).getYear()+1900;
				int log_time_m = flog_time.parse( log_time ).getMonth()+1;
				int log_time_d = flog_time.parse( log_time ).getDate();
				int log_time_q = 1;
				if ( log_time_m > 3 && log_time_m <= 6 ) {
					log_time_q = 2;
				} else if ( log_time_m > 6 && log_time_m <= 9 ) {
					log_time_q = 3;
				} else if ( log_time_m > 9 && log_time_m <= 12 ) {
					log_time_q = 4;
				}
				int log_time_w = flog_time.parse( log_time ).getDay();
				int log_time_h = flog_time.parse( log_time ).getHours();
				int log_time_1 = flog_time.parse( log_time ).getMinutes();
				int log_time_5 = log_time_1/5*5;
				int log_time_10 = log_time_1/10*10;
				int log_time_30 = log_time_1/30*30;
				// format random string
				String csvstr = String.format("'%d', '%s', '%d', '%s', '%d', '%s', '%d', '%s', '%s', '%f', '', '', '%d', '%d%02d%02d', '%d', '%d', '%d', '%d', '%d', '%d', '%d', '%d', '%d', '%d', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', ''", prod_id, code_no, status, log_time, uuid, card_id, prod_no, prod_name, prod_type, price, log_time_ms, log_time_y, log_time_m, log_time_d, log_time_y, log_time_q, log_time_m, log_time_d, log_time_w, log_time_h, log_time_1, log_time_5, log_time_10, log_time_30);
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
