package com.project.scenepickbe.chat;

import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import com.project.scenepickbe.common.apiPayload.code.exception.GeneralException;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final ChatStompInterceptor interceptor;

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws/chat").setAllowedOrigins("http://localhost:5173", "https://scenepick.co.kr");
		registry.setPreserveReceiveOrder(true);
		registry.setErrorHandler(new StompSubProtocolErrorHandler() {
			@Override
			public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
				String code = "CHAT400";
				for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
					if (cause instanceof GeneralException general) {
						code = general.getErrorReasonHttpStatus().getCode();
						break;
					}
				}
				var headers = StompHeaderAccessor.create(StompCommand.ERROR);
				headers.setMessage(code);
				return MessageBuilder.createMessage(code.getBytes(StandardCharsets.UTF_8), headers.getMessageHeaders());
			}
		});
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		registry.setApplicationDestinationPrefixes("/app");
		registry.enableSimpleBroker("/topic", "/queue").setTaskScheduler(chatHeartbeatScheduler())
			.setHeartbeatValue(new long[] {10000, 10000});
		registry.setPreservePublishOrder(true);
	}

	@Bean
	public ThreadPoolTaskScheduler chatHeartbeatScheduler() {
		var scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("chat-heartbeat-");
		return scheduler;
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(interceptor);
		registration.taskExecutor().corePoolSize(2).maxPoolSize(4).queueCapacity(256);
	}

	@Override
	public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
		registration.setMessageSizeLimit(16384).setSendTimeLimit(10000).setSendBufferSizeLimit(65536);
	}
}
