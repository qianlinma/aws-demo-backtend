package com.mycompany.backend.demo.config;

import com.mycompany.grpc.user.UserServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class UserGrpcClientConfig {
    @Bean(destroyMethod = "shutdown")
    public ManagedChannel userGrpcChannel(
            @Value("${user.service.grpc-target:user.demo.internal:9090}") String userServiceGrpcTarget
    ) {
        return ManagedChannelBuilder.forTarget(userServiceGrpcTarget)
                .usePlaintext()
                .build();
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(ManagedChannel userGrpcChannel) {
        return UserServiceGrpc.newBlockingStub(userGrpcChannel)
                .withDeadlineAfter(2, TimeUnit.SECONDS);
    }
}
