package ac.york.typhon.analytics.models2020.scenario;

import java.util.UUID;

import ac.york.typhon.analytics.builder.AnalyticsJobBuilder;
import ac.york.typhon.analytics.commons.enums.AnalyticTopicType;

public class AnalyticsRunner {

	public static void main(String[] args) throws Exception {
		AnalyticsJobBuilder.build(new TestScenario(), AnalyticTopicType.POST, UUID.randomUUID().toString());
	}

}
