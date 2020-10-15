package ac.york.typhon.analytics.models2020.scenario;

import java.util.Date;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.time.Time;

import ac.york.typhon.analytics.analyzer.IAnalyzer;
import ac.york.typhon.analytics.commons.datatypes.commands.SelectCommand;
import ac.york.typhon.analytics.commons.datatypes.events.DeserializedPostEvent;
import ac.york.typhon.analytics.commons.datatypes.events.Event;
import ac.york.typhon.analytics.commons.datatypes.events.PostEvent;

public class TestScenario implements IAnalyzer {

	@Override
	public void analyze(DataStream<Event> eventsStream) throws Exception {
		eventsStream
		.map(new MapFunction<Event, DeserializedPostEvent>() {

			@Override
			public DeserializedPostEvent map(Event arg0) throws Exception {
				return (DeserializedPostEvent) arg0;
			}
		})
		.filter(new FilterFunction<DeserializedPostEvent>(
				) {
			
			@Override
			public boolean filter(DeserializedPostEvent pe) throws Exception {
				return pe.getPreEvent().getQuery().contains("from Product p select p");
			}
		})
		.assignTimestampsAndWatermarks(new BoundedOutOfOrdernessGenerator())
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
		.keyBy(0)
		.timeWindow(Time.seconds(15))
		.sum(1)
		.map(new MapFunction<Tuple3<String,Integer, String>, Tuple3<String,Integer, String>>() {

			@Override
			public Tuple3<String, Integer, String> map(Tuple3<String, Integer, String> value) throws Exception {
				System.out.println("Product with id: " + value.f0 + " has been visited " + value.f1 + " times.");
				return value;
			}
		});
	}

}
