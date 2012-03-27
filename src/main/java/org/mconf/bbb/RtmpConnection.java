package org.mconf.bbb;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.mconf.bbb.BigBlueButtonClient.OnConnectedListener;
import org.mconf.bbb.BigBlueButtonClient.OnExceptionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.flazr.rtmp.client.ClientHandler;
import com.flazr.rtmp.client.ClientOptions;

public abstract class RtmpConnection extends ClientHandler implements ChannelFutureListener {

	private static final Logger log = LoggerFactory.getLogger(RtmpConnection.class);

	final protected BigBlueButtonClient context;
	
	public RtmpConnection(ClientOptions options, BigBlueButtonClient context) {
		super(options);
		this.context = context;
	}
	
	private ClientBootstrap bootstrap;
	private ChannelFuture future;
	
	public boolean connect() {  
        bootstrap = getBootstrap(Executors.newCachedThreadPool());
        future = bootstrap.connect(new InetSocketAddress(options.getHost(), options.getPort()));
        future.addListener(this);
    	return true;
    }
	
	public void disconnect() {
		if (future.getChannel().isConnected()) {
			future.getChannel().close(); //ClosedChannelException
			future.getChannel().getCloseFuture().awaitUninterruptibly();
			bootstrap.getFactory().releaseExternalResources();
		}
	}
	
	abstract protected ClientBootstrap getBootstrap(final Executor executor);
	
	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (!future.isSuccess())
			for (OnConnectedListener listener : context.getConnectedListeners()) {
				listener.onConnectedUnsuccessfully();
		}
	}	
	
	public BigBlueButtonClient getContext() {
		return context;
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		String exceptionMessage = e.getCause().getMessage();
		if (exceptionMessage != null && exceptionMessage.contains("ArrayIndexOutOfBoundsException") && exceptionMessage.contains("bad value / byte: 101 (hex: 65)")) {
			log.debug("Ignoring malformed metadata");
			return;
		} else {
			super.exceptionCaught(ctx, e);
	
			for (OnExceptionListener listener : context.getExceptionListeners())
				listener.onException(e.getCause());
		}
	}	
}
