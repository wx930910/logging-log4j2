/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.test.appender;

import java.io.Serializable;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.mockito.Mockito;

/**
 * This appender is primarily used for testing. Use in a real environment is
 * discouraged as the List could eventually grow to cause an OutOfMemoryError.
 *
 * This appender will use {@link Layout#encode(Object, ByteBufferDestination)}
 * (and not {@link Layout#toByteArray(LogEvent)}).
 */
public class EncodingListAppender extends ListAppender {

	public static ByteBufferDestination mockByteBufferDestination1() {
		ByteBuffer mockFieldVariableByteBuffer = ByteBuffer.wrap(new byte[8192]);
		ByteBufferDestination mockInstance = Mockito.mock(ByteBufferDestination.class);
		Mockito.doAnswer((stubInvo) -> {
			ByteBuffer data = stubInvo.getArgument(0);
			mockFieldVariableByteBuffer.put(data);
			return null;
		}).when(mockInstance).writeBytes(Mockito.any(ByteBuffer.class));
		Mockito.doAnswer((stubInvo) -> {
			byte[] data = stubInvo.getArgument(0);
			int offset = stubInvo.getArgument(1);
			int length = stubInvo.getArgument(2);
			mockFieldVariableByteBuffer.put(data, offset, length);
			return null;
		}).when(mockInstance).writeBytes(Mockito.any(byte[].class), Mockito.anyInt(), Mockito.anyInt());
		Mockito.when(mockInstance.getByteBuffer()).thenAnswer((stubInvo) -> {
			return mockFieldVariableByteBuffer;
		});
		Mockito.when(mockInstance.drain(Mockito.any()))
				.thenThrow(new IllegalStateException("Unexpected message larger than 4096 bytes"));
		return mockInstance;
	}

	public EncodingListAppender(final String name) {
		super(name);
	}

	public EncodingListAppender(final String name, final Filter filter, final Layout<? extends Serializable> layout,
			final boolean newline, final boolean raw) {
		super(name, filter, layout, newline, raw);
	}

	@Override
	public synchronized void append(final LogEvent event) {
		final Layout<? extends Serializable> layout = getLayout();
		if (layout == null) {
			events.add(event);
		} else {
			final ByteBufferDestination content = EncodingListAppender.mockByteBufferDestination1();
			layout.encode(event, content);
			content.getByteBuffer().flip();
			final byte[] record = new byte[content.getByteBuffer().remaining()];
			content.getByteBuffer().get(record);
			write(record);
		}
	}

}
