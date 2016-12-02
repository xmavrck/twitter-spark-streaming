# Introduction:
This demo is exaplaining that how Apache Nifi is streaming live data (tweets) from twitter and sending those tweets to Apache Kafka which provides unified,high throughput and low latency platform for handling real time data feeds. So Apache Nifi is basically automating the data transfer between disparate data sources and also making data ingestion fast,easy and secure.
After this,Apache Spark Streaming comes into play which is real-time micro batching tool that we are using to ingest data from Kafka and visualizing live streaming to user on console.

# Enviroment & Technologies:
- Ubuntu 14.04
- JDK 1.8
- Apache Nifi 1.1.0
- Apache Zookeeper(bundled with kafka package) 
- Apache Kafka 2.11-0.8.2.1
- Apache Spark 1.6.0
- Apache Maven 3.3.9

# Steps for setting up enviroment: 

###### Setup your machine or VM on cloud
We are having our own private cloud,so we just create a new instance(or launch vm) on cloud and then setup the enviroment.We also have DevOps guys which can just create a script to setup the enviroment.But if we want to learn this demo completely.Then I have described the complete procedure below that how and from where  you need to download those packages.
If any user has its aws server or cloud provider,then he can just create their new instance on cloud and just follow the steps given below to setup the enviroment.

###### Setup Java 1.8
a) Add the WebUpd8 Team Personal Package Archive (PPA) by using this command  
```sudo apt-add-repository ppa:webupd8team/java```  
b) The Installation  
we should update our packages first by using this command  
```sudo apt-get update```  
Then let’s install Oracle Java 8 with the PPA installer by using this command  
```sudo apt-get install oracle-java8-installer```  
c) Verify Installation  
```java -version```  
The output will be like this:  
```  
java version "1.8.0_45"
Java(TM) SE Runtime Environment (build 1.8.0_45-b14)
Java HotSpot(TM) 64-Bit Server VM (build 25.45-b02, mixed mode)
```  
After you have installed java,you also need to set JAVA_HOME enviroment variable in your bash file which should point to home directory of java installation.
So firstly open your environment file using this command  
```sudo nano /etc/environment```  
and then add this line to the file  
```
JAVA_HOME=your-java-installation-home-directory
```  
In my vm,the path was /usr/lib/jvm/java-8-oracle.
And then you need to source to load variables by running this command  
```source /etc/environment```  
And then you can verify it by using this command  
```echo $JAVA_HOME```  
It will show the path you have set up in your environment  

##### Create/Register your own Twitter App and Get Oauth Credentials.

Firstly we need to create an app on Twitter (if we don't have account on twitter ,then we need to signup).  
We are doing this to get Oauth Crendentials so that our Apache Nifi can stream tweets from Twitter using these Oauth Crendentials.This means we don't need specify our twitter credentials in Apache Nifi while fetching tweets.  
Url for creating your own twitter apps is 
```
https://apps.twitter.com/
```  
After you signin,you need to create your app and after you able to create your app successfully,then you will see the Oauth crendentials for the app which we will use in next steps to stream tweets from nifi.  
The Oauth credentials we require are:  
- consumer key
- consumer secret
- access token
- access token secret

##### Setup Apache Kafka

Firstly we need to download Apache kafka from the link  given below  
```
https://kafka.apache.org/downloads.html
```  
In this demo, We have used kafka_2.11-0.8.2.1.So if you want to download this version,you need to follow this link:  
```
https://www.apache.org/dyn/closer.cgi?path=/kafka/0.8.2.1/kafka_2.11-0.8.2.1.tgz
```  
After you have download the tar file of kafka,you need to extract it.  
The default port of kafka is 9092. So if it is conflicting with any port in your machine,then you can change this value and also other required properties in 
```
KAFKA_HOME/config/server.properties
```  
Now when we need to launch kafka,then we also need to install apache zookeeper which is used to coordinating tasks, state management, configuration, etc across a distributed system. So Apache Zookeeper is a general purpose distributed process coordination system. Ex. Our kafka is launched in multiple nodes. So Zookeeper actually keeps track of Kafka Nodes,kafka topics in the cluster.  
You don't need to install apache zookeeper separately as it is already bundled with kafka installation package.And the default port used by zookeeper is 2181. So if you need to change the port ,you can change it in 
```
KAFKA_HOME/conf/zookeeper.properties
```  
Ex:  
```
clientPort=2181
```  

##### Setup Apache Spark

Firstly we need to download apache spark from this link:  
```
http://spark.apache.org/downloads.html
```  
The version we used in this demo is 1.6.0.  
After you have downloaded the tar file,you need to extract it.  
To verify the installation,you can use this command:  
```
cd SPARK_HOME/bin
spark-shell
```  
This will launch the spark shell.  

##### Setup Apache Nifi
Firstly we need to download apache nifi from this link:  
```
https://nifi.apache.org/download.html
```  
Filename: nifi-1.1.0-bin.tar.gz  
Default port for nifi is ```8080```.If this port is clashing with some other port,then you can change your port no and other required settings in 
```
NIFI_HOME/conf/nifi.properties
```  
Example:  
in nifi.properties,this properties can be useful to look upon.  
Site to Site properties  
```
nifi.remote.input.secure=false // so that we can access our nifi ui remotely
```  
web properties   
```
nifi.web.http.port=8091 // this port was initially 8080,but I modified it as it was clashing with my app ports.
```  


##### Setup Apache Maven  

Maven is a project building tool in which define all our dependencies in a pom(Project Object Folder) file and then maven automatically builds the project and add dependencies to the jar either from local or remote repository.  
So we can install it using command:  
```sudo apt-get install maven```


#  SetUp Nifi Flow  

Before we start with creating actual nifi workflow, we need to launch some other process first.

##### Launch Apache Zookeeper

We can launch the apache zookeeper process by this command.  
```KAFKA_HOME/bin/zookeeper-server-start.sh config/zookeeper.properties```

#####  Launch Apache Kafka

We can launch the apache kafka by this command.  
```KAFKA_HOME/bin/kafka-server-start.sh config/server.properties```


#####  Create Kafka Topic

Kafka Topic is like grouping messages of related data as we have tables in database.  
So topic is like a container with which messages are associate.  
We can create a topic by using command:  
```
KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic tweets-live-streaming
```
There are some arguements which we are passing while creating the topic:  
```
a) zookeeper url = localhost:2181
b) replication-factor =1 (because we have launched only one node of kafka)
c) partitions = 1 (we can increase partitions if we want to read data concurrently from kafka node,this worked same like we have partitions in our harddisk.)
d) topic = tweets-live-streaming (this is the topic name in which we are storing tweets)
```  

#####  Create nifi flow

We can launch nifi by using these commands:  
Firstly go to your nifi home directory first  
```
cd NIFI_HOME
```  
then launch nifi with this command  	
```
./bin/nifi.sh start
```
After sometime,you can launch your nifi in your browser with this link  
```
http://hostname:8080/nifi/
```  

##### Create GetTwitter Processor
a) Now in menu bar in top left side,there is one option processor,you need to drag that onto the workflow sheet.  
b) When you drag that processor,a pop "Add Processor" will be shown.In the Filter field,you will start typing GetTwitter and in the results,you will see the GetTwitter  
Processor and then just add it.  
c) Now you will see the GetTwitter Processor on your workflow sheet and then you need to select that processor and just right on it.Then you need to select Configure option.  
d) After you select this,you will see a pop up "Configure Processor" .In this popup,you have four tabs and you need to change these attributes in these:  
###### Settings :
``` 
Name: GetTwitter_Processor (optional)
We have checkbox for auto terminate the relationships .We don't need to check the success checkbox right now.This I have explained in furthur steps(while creating Publish Kafka Processor).
```  
###### Scheduling :
There is a progress bar in which you can manage latency and throughput.So if you want realtime tweet streaming,you can set this progress bar to 0ms.This means lower latency and if we want higher throughput means efficient resource management then we can increase the value of progress bar accordingly.  
###### Properties:
The properties that are in bold are manadatory for this processor.  
```
Twitter Endpoint : Sample Endpoint is default value.This enpoint provides us the public data or tweets.Other two options are Firehose EndPoint(provides access to all tweets) and Filter Endpoint(we can filter the data on the basis of terms and our followers userid's ).
```
Then we will add our Oauth Credentials that we get from our twitter app:  
```
- Consumer Key
- Consumer Secret
- Access Token
- Access Token Secret
```	
and that's it.Our Twitter processor is ready.  
We can learn more about this GetTwitter Processor at  
```
https://nifi.apache.org/docs/nifi-docs/components/org.apache.nifi.processors.twitter.GetTwitter/
```

##### Create PublishKafka Processor
Now we will create PublishKafka Processor in which tweets will be publish on our ```tweets-live-streaming``` kafka topic from GetTwitter Processor.  
a) Drag a processor on workflow sheet and then you will see the "Add Processor" popup,then you need to type "PublishKafka" in filter box,and then add the PublishKafka processor.After you add that processor,selct it and right click on it and click configure.  
b) Now again you will see "Configure Processor" popup,then you need to change these attributes of following tabs:  
###### Settings:  
```
Name: PublishKafka_Processor (optional)
Tick the failure and success checkbox.Because we don't have furthur processsor in the worksheet where we can send data from kafka.So that's why we want to auto terminate relationships here in both cases i.e. success or failure.
```
###### Properties:
```
Kafka Brokers: kafka-node-hostname:kafka-port (ex: your-vm-ip:9092)
Topic Name: tweets-live-streaming
```  
If you want to learn more about PublishKafka Processor,follow this link  
```
https://nifi.apache.org/docs/nifi-docs/components/org.apache.nifi.processors.kafka.pubsub.PublishKafka/
```

##### Create Connection between GetTwitter and PublishKafka Processor

As we know our both processors are set now,now we need to create connection between them. So when we mouse over on GetTwitter Processor,we will an arrow and so we need to drag that arrow to PublishKafka processor and connects that arrow with it.Then you will see a "create connection" pop up and then just check on "success" checkbox means if GetTwitter Processor successfully started then it will routed(means data will be routed) to PublishKafka Processor.  

##### Start the Nifi Workflow
Now you need to select the GetTwitter Processor,then right click on it and click start.
In the same way, select the PublishKafka Processor,then right click on it and click start.
And in short time,if all configurations are done properly according to the doc then you start seeing the live flow of data on UI from twitter to kafka.  
As twitter is writting data to Kafka,So you will see the write bytes updating after some interval in GetTwitter Processor and in PublishKafka Processor,you will see read bytes updating as Kafka is reading from Twitter.  

##### Launching Spark Streaming:
a) First you need to clone the code from git repo.  
b)  After you have cloned the code and now go to the home directory of your Spark Streaming Project which is containing src folder and other necessary files using this command  
```
cd SPARK-STREAMING-PROJECT_FOLDER
```  
c) Then you need to build your project using this command  
```
mvn clean package
```  
d) Then you need to start your spark streaming job. So for this,you need to go to your SPARK_HOME(spark installation directory).  
```
cd SPARK_HOME
```
e) Then you can submit your spark application like this  
```
./bin/spark-submit --class main-class ../monitoring-your-jar-file-path
```  
Example:  
```
./bin/spark-submit 
--class com.xenonstack.spark_streaming_tweets_kafka.ReadTweetsFromKafka /home/ubuntu/monitoring-spark-streaming-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```  
This will launch your spark streaming job and you will start seeing live streaming of tweets on your console.
