package org.eclipse.californium.actinium;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class InstanceNameTest extends BaseServerTest {

	private final String instanceName;

	@Parameterized.Parameters(name = "{0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{"hello-1"}, {"Hello-1"}, {"a=a"}, {"a?"}, {"zürich"}, {"sd-sd"}, {"-a"}, {"4number"}, {"test/test"},
				{"test~test"}, {"test?test"}, {"test#test"}, {"test!test"}, {"test@test"}, {"test&test"}, {"test'test"},
				{"test/test"}, {"test test"}, {"test+test"}, {"test,test"}, {"test*test"}, {"test;test"},
				{"test)test"}, {"test(test"}, {"test[test"},
		});
	}


	public InstanceNameTest(String instanceName) {
		this.instanceName = instanceName;
	}

	@Test
	public void testIsAppRunningIfCreateIsSuccessful() throws Exception {
		String scriptName = "helloWorld";
		testInstallHelloWorld(scriptName);
		boolean successful = createInstance(scriptName, instanceName, false);
		if (successful) {
			testCheckIfInstanceExists(instanceName);
			testCheckInstance(scriptName, instanceName);
			Thread.sleep(2000);
			testCheckIfInstanceIsRunning(instanceName);
			testGET("apps/running/"+instanceName, "Hello World");
		}
	}

}
