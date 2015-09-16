package org.eclipse.californium.actinium;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class AppNameTest extends BaseServerTest {

	private final String appName;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"hello-1"}, {"Hello-1"}, {"a=a"}, {"a?"}, {"zürich"}, {"sd-sd"}, {"-a"}, {"4number"}, {"test/test"},
				{"test~test"}, {"test?test"}, {"test!test"}, {"test@test"}, {"test&test"}, {"test'test"},
				{"test/test"}, {"test+test"}, {"test,test"}, {"test*test"}, {"test;test"},
				{"test)test"}, {"test(test"}, {""},
		});
	}


	public AppNameTest(String appName) {
		this.appName = appName;
	}

	@Test
	public void testIsAppRunningIfCreateIsSuccessful() throws Exception {
		String instanceName = "test";
		boolean successful = testInstallHelloWorld(appName, false);
		if (successful) {
			createInstance(appName, instanceName);
		}
	}

}
