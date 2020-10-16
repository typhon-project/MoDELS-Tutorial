# MoDELS 2020 Tutorials - Analytics Component

This guide provides step-by-step instructions on the demo presented for the TYPHON analytics component as part of the MoDELS 2020 Tutorials [T7].

## Setup
You need to have Eclipse for Java developers downloaded (*NB: do not use an Eclipse newer than Eclipse 2020-6*). You can download Eclipse 2020-06 from [here](https://www.eclipse.org/downloads/packages/release/2020-06/r/eclipse-ide-java-developers).

After you have Eclipse installed, create a workspace in your filesystem and import (File --> Import... --> Existing Projects into the Workspace) the two Eclipse projects that you can download/pull from [this](https://github.com/typhon-project/MoDELS-Tutorial/tree/master/Analytics) repository, namely `ac.york.typhon.analytics.models2020.generator` and `ac.york.typhon.anlytics.models2020.scenario`.

This first is a tool that generates random TyphonQL select queries that will be consumed by the analytics scenario. The second includes the logic for the analytics scenario presented in the tutorial.

## Scenario
In this tutorial we demonstrate how analytics developers can exploit the TYPHON analytics features to calculate the most visited products of our e-commerce website in a specific period by consuming the appropriate queries. When users navigate through the catalogue of the envisioned e-commerce website an appropriate select TyphonQL query is executed to retrieve the details of this product. The query follows this format `from Product p select p.name where p.@id == # XXX` where `XXX` is the UUID of the product. 

Thus, in this scenario we would like to count the number of the times  query like this is executed against the TYPHON polystore for each of the products (identified by their UUID) in a specific time window.

## Implementation
### Query Generator
For this demo, as the envisioned e-commerce website is not live and has no real users interacting with it, we need to create a tool which will simulate the real users' behaviour, that is to pretend that users are visiting product webpages and generate the appropriate `select` queries like the one shown above.

The `ac.york.typhon.analytics.models2020.generator` projects includes such a generator. The main class of the generator (i.e., `GeneratorRunner` class) creates a list of UUID that represent the number of products available in the e-commerce database. Then, in a multi-threaded way, it generates the `select` queries by picking each time randomly one of the products from the list. The number of products in the list and the number of threads can be set by updating the `NUM_OF_PRODUCTS` and `NUM_OF_THREADS` final variables respectively. The latter indicates how many select queries will be generated.

You can run this generator by running the main method as a Java Application from your Eclipse. When running the generator, the queries will be executed against the TYPHON Polystore which will produce and publish the equivalent PostEvents in the POST Kafka log. The POST log will include messages like the following (we only show the field necessary for this demo)

```
{"startTime":1602774251438,
"preEvent":{"query":"{\"query\":\"from Product p select p.name 
where p.@id == #5e6caf60-d554-4428-b8c5-310a390ebbc4\"}"}
```
### Analytics Scenario
In order to implement our analytics scenario, we need to create two Java classes that use the TYPHON analytics architecture. These two classes are included in the `ac.york.typhon.anlytics.models2020.scenario` project you previously imported and are name `AnalyticsRunner` and `TestScenario`.

The first includes the main method that instructs which class contains the actual scenario
while the second includes the Apache Flink logic for implementing the scenario. In order to run the scenario and start producing the analytics of interest by consuming the events stored in the Kafka POST log, you need to run as a Java Application the main method included in the `AnalyticsRunner` class.

We now go step-by-step in explaining the logic of the analytics scenario as this is implemented by using Apache Flink operators. (Please note that the scope of this tutorial is not to demonstrate and explain in detail how Flink works or how Flink operators can be combined to produce relevant results. Interested attendees can visit [this](https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/datastream_api.html) webpage and find plenty of resources and documentation).

1) As explained in the slides, TYPHON analytics developers need to implement the `IAnalyzer` interface provided by the analytics architecture and implement its `analyze(...)` method to start consuming the PostEvents from the POST log.

```
public void analyze(DataStream<Event> eventsStream) throws Exception {
...
}
```

This method provides as a parameter the Flink DataStream that contains all the PostEvent objects. 

2) We then initially implement a Flink map transformation that casts the Event objects to DeserialisedPostEvent objects. This is an optional task, as we are not going to use any of the features the DeserialisedPostEvent object offers, but is shown here for consistency, as this might be necessary in other scenarios.

```
.map(new MapFunction<Event, DeserializedPostEvent>() {
	@Override
	public DeserializedPostEvent map(Event arg0) throws Exception {
		return (DeserializedPostEvent) arg0;
	}
})
```

3) The next step includes a filtering operator. As this scenario is only applicable to the aforementioned `select` queries we need to only accept and pass to the rest of the operators in the stream chain, those Events that were generated from such queries. In the code, we do this filtering by accepting only those Events for which the field `query` includes the substring `from Product p select p`.
```
.filter(new FilterFunction<DeserializedPostEvent>() {
	@Override
	public boolean filter(DeserializedPostEvent pe) throws Exception {
		return pe.getPreEvent().getQuery().contains("from Product p select p");
	}
})
```

After this filtering our steam includes only relevant to this scenario events. Of course, as our generator only produces such events, this filtering is unnecessary, but again in real scenario where many types of queries are executed by the system (e.g., `insert Order, insert User, update Order, etc.`) we need to apply this filtering. 

4) As the scenario requires the calculation of the times that each product is visited *within a specific time window* (e.g., the top visited products for each day) we need to instruct Flink on which field of our object includes the time that the event happened. This is done by implementing the `AssignerWithPeriodicWatermarks` interface of Flink in the class `BoundedOutOfOrdernessGenerator` which is included in the project and is instantiated in the `assignTimestampsAndWatermarks` Flink operator

```
.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessGenerator())
```

More about Flink's timing and watermarks functionality can be found [here](https://ci.apache.org/projects/flink/flink-docs-release-1.11/dev/event_time.html).

5) As the Event object includes more information that is needed for this scenario, it is good practice to transform it to another object that holds only the necessary information. In practice this is only the UUID of the product included in the query. We also choose to keep the timestamp that this query was executed (more on this later on). Flink offers a `Tuple` data structure that it is helpful in such cases. We pick to use a `Tuple3` consisting of the `UUID` of the product queried, an Integer storing the number of the times this product is visited *by this specific query (thus this is always 1 - more on this later on) and the datetime that the query was executed.

```
.map(new MapFunction<DeserializedPostEvent, Tuple3<String, Integer, String>>() {
	@Override
	public Tuple3<String, Integer, String> map(DeserializedPostEvent value) throws Exception {
		String idFirstPart = value.getPreEvent().getQuery().split("p.@id == #")[1];
		String id = idFirstPart.substring(0, idFirstPart.length() - 2);
		String eventTime = value.getStartTime().toString();
		Tuple3<String, Integer, String> tuple = new Tuple3<String, Integer, String>(id, 1, eventTime);
		return tuple;
	}
})
```

6) We now describe the next three operators in the scenario which are shown below.

```
.keyBy(0)
.timeWindow(Time.seconds(15))
.sum(1)
```
The first operator (`keyBy(0)`) instruct Flink to separate the incoming Tuples based on their first (0) field (i.e., the UUID of the selected product). This way, we can sum the total number of Tuples for each product. 

As we are interested in calculating, the totals for a specific time window (e.g., for each day) we need to ask Flink to split them into time windows. This is done by using the `timeWindow` operator passing as a parameter the size of the window. For the needs of the demo, our data generator is running for a few minutes simulating products' browsing (it would be unrealistic to run this for days in order to be able to demonstrate this scenario) we pick a small time window (i.e., of 15 seconds). In a real environment where users are visiting our e-commerce website, we can change this to any other (e.g., `Time.days(1)`).

We then use the pre-built Flink's `sum` operator to calculate the times each product is visited in the specified time window. This is done by instructing Flink to sum the values of the second (1) field on the incoming Tuple. This is the reason why we included the value '1' in the second field of the Tuple when creating it. In practice, Flink through the `sum` operator adds all these '1's calculating the total number a product UUID is queried. There are many other ways to implement the totals (e.g., through implementing a method that can be use with the `aggregate` or the `process` operator) but this is the one requiring the less code and has no performance loss.

7) Each time a time windows closes the result of the `sum` operator for *each* UUID is passed to the next operator in the chain which is actually a map operator in our case exploited to print the results in the console. 

```
.map(new MapFunction<Tuple3<String,Integer, String>, Tuple3<String,Integer, String>>() {
	@Override
	public Tuple3<String, Integer, String> map(Tuple3<String, Integer, String> value) throws Exception {
		System.out.println("Product with id: " + value.f0 + " has been visited " + value.f1 + " times.");
		return value;
	}
})
```

This prints a descriptive message (e.g., "Product with id: 7fa14c72-8b3c-4c2c-8aa3-0078c25abaad has been visited 4 times.") Of course, we can implement a custom sink to store the results in a file or in a database and then load them in a visualisation tool (like Grafana) to produce the relevant bar charts or diagrams.

## Known Issues
* As this project is still under development, there might be the case that some TyphonQL queries cannot be parsed, throwing an exception. As you POST log might include such queries from the previous slots we advise you to clear the POST queue before trying the analytics demo by restarting the polystore (`-f docker-compose-local-deployment.yaml down` and `-f docker-compose-local-deployment.yaml up` afterwards). 

