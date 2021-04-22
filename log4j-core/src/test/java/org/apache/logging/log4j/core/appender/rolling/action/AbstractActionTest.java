package org.apache.logging.log4j.core.appender.rolling.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.junit.StatusLoggerLevel;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusLogger;
import org.junit.jupiter.api.Test;

@StatusLoggerLevel("WARN")
public class AbstractActionTest {

	public static AbstractAction mockAbstractAction1() throws IOException {
		AbstractAction mockInstance = spy(AbstractAction.class);
		doThrow(new IOException("failed")).when(mockInstance).execute();
		return mockInstance;
	}

	// Test for LOG4J2-2658
	@Test
	public void testExceptionsAreLoggedToStatusLogger() throws IOException {
		StatusLogger statusLogger = StatusLogger.getLogger();
		statusLogger.clear();
		AbstractActionTest.mockAbstractAction1().run();
		List<StatusData> statusDataList = statusLogger.getStatusData();
		assertThat(statusDataList, hasSize(1));
		StatusData statusData = statusDataList.get(0);
		assertEquals(Level.WARN, statusData.getLevel());
		String formattedMessage = statusData.getFormattedStatus();
		assertThat(formattedMessage, containsString("Exception reported by action 'class org.apache."
				+ "logging.log4j.core.appender.rolling.action.AbstractActionTest$TestAction' java.io.IOException: "
				+ "failed" + System.lineSeparator()
				+ "\tat org.apache.logging.log4j.core.appender.rolling.action.AbstractActionTest"
				+ "$TestAction.execute(AbstractActionTest.java:"));
	}

	@Test
	public void testRuntimeExceptionsAreLoggedToStatusLogger() {
		StatusLogger statusLogger = StatusLogger.getLogger();
		statusLogger.clear();
		new AbstractAction() {
			@Override
			public boolean execute() {
				throw new IllegalStateException();
			}
		}.run();
		List<StatusData> statusDataList = statusLogger.getStatusData();
		assertThat(statusDataList, hasSize(1));
		StatusData statusData = statusDataList.get(0);
		assertEquals(Level.WARN, statusData.getLevel());
		String formattedMessage = statusData.getFormattedStatus();
		assertThat(formattedMessage, containsString("Exception reported by action"));
	}

	@Test
	public void testErrorsAreLoggedToStatusLogger() {
		StatusLogger statusLogger = StatusLogger.getLogger();
		statusLogger.clear();
		new AbstractAction() {
			@Override
			public boolean execute() {
				throw new AssertionError();
			}
		}.run();
		List<StatusData> statusDataList = statusLogger.getStatusData();
		assertThat(statusDataList, hasSize(1));
		StatusData statusData = statusDataList.get(0);
		assertEquals(Level.WARN, statusData.getLevel());
		String formattedMessage = statusData.getFormattedStatus();
		assertThat(formattedMessage, containsString("Exception reported by action"));
	}
}
