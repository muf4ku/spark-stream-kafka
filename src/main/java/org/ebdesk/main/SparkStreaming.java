package org.ebdesk.main;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.TaskContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.HasOffsetRanges;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import org.apache.spark.streaming.kafka010.OffsetRange;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SparkStreaming {
	public static void main(String[] args) throws InterruptedException {
		Map<String, Object> kafkaParams = new HashMap<>();
		kafkaParams.put("bootstrap.servers", "kafka01.production.bt:6667");
		kafkaParams.put("key.deserializer", StringDeserializer.class);
		kafkaParams.put("value.deserializer", StringDeserializer.class);
		kafkaParams.put("group.id", "use_a_separate_group_id_for_each_stream");
		kafkaParams.put("auto.offset.reset", "latest");
		kafkaParams.put("enable.auto.commit", false);

		Collection<String> topics = Arrays.asList("ann-twitter-post");

		SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("StreamKafka");
		JavaStreamingContext jssc = new JavaStreamingContext(conf, Durations.seconds(1));

//		JavaStreamingContext streamingContext;
		JavaInputDStream<ConsumerRecord<String, String>> stream = KafkaUtils.createDirectStream(jssc,
				LocationStrategies.PreferConsistent(),
				ConsumerStrategies.<String, String>Subscribe(topics, kafkaParams));

		stream.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<String, String>>>() {

			@Override
			public void call(JavaRDD<ConsumerRecord<String, String>> t) throws Exception {
				t.foreach(new VoidFunction<ConsumerRecord<String, String>>() {

					@Override
					public void call(ConsumerRecord<String, String> t) throws Exception {
						System.out.println(t.value());
					}
				});
			}
		});

//		stream.print();
		jssc.start();
		jssc.awaitTermination();

	}

}
