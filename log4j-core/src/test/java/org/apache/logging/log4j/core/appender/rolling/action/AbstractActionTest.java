package org.apache.logging.log4j.core.appender.rolling.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.junit.StatusLoggerRule;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusLogger;
import org.junit.Rule;
import org.junit.Test;

public class AbstractActionTest {

	public static AbstractAction mockAbstractAction1() throws IOException {
		AbstractAction mockInstance = spy(AbstractAction.class);
		doThrow(new IOException("failed")).when(mockInstance).execute();
		return mockInstance;
	}

	@Rule
	public StatusLoggerRule statusLogger = new StatusLoggerRule(Level.WARN);

	// Test for LOG4J2-2658
	@Test
	public void testExceptionsAreLoggedToStatusLogger() throws IOException {
		StatusLogger statusLogger = StatusLogger.getLogger();
		statusLogger.clear();
		AbstractActionTest.mockAbstractAction1().run();
		List<StatusData> statusDataList = statusLogger.getStatusData();
		assertEquals(1, statusDataList.size());
		StatusData statusData = statusDataList.get(0);
		assertEquals(Level.WARN, statusData.getLevel());
		String formattedMessage = statusData.getFormattedStatus();
		assertTrue(formattedMessage, formattedMessage.contains("Exception reported by action 'class org.apache."
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
		assertEquals(1, statusDataList.size());
		StatusData statusData = statusDataList.get(0);
		assertEquals(Level.WARN, statusData.getLevel());
		String formattedMessage = statusData.getFormattedStatus();
		assertTrue(formattedMessage.contains("Exception reported by action"));
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
		assertEquals(1, statusDataList.size());
		StatusData statusData = statusDataList.get(0);
		assertEquals(Level.WARN, statusData.getLevel());
		String formattedMessage = statusData.getFormattedStatus();
		assertTrue(formattedMessage.contains("Exception reported by action"));
	}
}
