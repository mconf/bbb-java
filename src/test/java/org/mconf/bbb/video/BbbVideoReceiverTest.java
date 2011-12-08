package org.mconf.bbb.video;

import static org.junit.Assert.*;

import org.junit.Test;

public class BbbVideoReceiverTest {
	@Test
	public void getAspectRatioTest() {
		assertEquals(BbbVideoReceiver.getAspectRatio(99, "320x24099"), 320/(float)240, 0.0001);
		assertEquals(BbbVideoReceiver.getAspectRatio(24, "320x24024"), 320/(float)240, 0.0001);
		assertEquals(BbbVideoReceiver.getAspectRatio(99, "320x24024"), -1, 0.0001);
	}
}
