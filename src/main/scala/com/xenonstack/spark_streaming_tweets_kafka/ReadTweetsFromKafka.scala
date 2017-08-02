package com.xenonstack.spark_streaming_tweets_kafka

import kafka.serializer.StringDecoder

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{ Seconds, StreamingContext }
import org.apache.spark.streaming.kafka.KafkaUtils
/*
 * This object will read tweets from kafka
 * using spark-streaming module of Apache Spark.
 * We are setting interval of 10 seconds to read tweets from Kafka.
 * */
object ReadTweetsFromKafka {
  /*
   * This method is entry point of our application
   * */
  def main(args: Array[String]) {
    // defining kafka topic from where we will read tweets 
    val _TOPIC_NAME = "tweets-live-streaming";
    // defining streaming interval in seconds to read from kafka
    val STREAMING_INTERVAL_IN_SECONDS = 10;
    // defining our Spark Streaming Context in which we are setting our driver program node and appname and interval in seconds
    val sparkStreamingContext = new StreamingContext(new SparkConf().setMaster("local[2]").setAppName("ReadTweetsFromKafka"), Seconds(STREAMING_INTERVAL_IN_SECONDS))
    // setting KafkaDataStream using KafkaUtils 
    //in which we are specifying the sparkStreamingContext,kafka broker list(hostname and portno) and 
    //the topic name from where we need to read tweets.
    val kafkaDataStream = KafkaUtils.
    createDirectStream[String, String, StringDecoder, StringDecoder](sparkStreamingContext, Map("metadata.broker.list" -> "localhost:9092"), Set(_TOPIC_NAME));
    // we are printing the kafka data stream here to see the tweets
    kafkaDataStream.print();
    // starting our spark streaming context
    sparkStreamingContext.start()
    // we will put our app on hold till spark streaming completes the process of fetching tweets from kafka
    sparkStreamingContext.awaitTermination()
    // whenever sparkStreamingContext will stopped by user or either in case of no tweets
    // then we will stop spark streaming context
    sparkStreamingContext.stop()
  }
}
