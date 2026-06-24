package com.mycompany.backend.demo.client;

import com.mycompany.backend.demo.model.ProductUserProfile;
import com.mycompany.grpc.user.GetUserProfileRequest;
import com.mycompany.grpc.user.UserProfileResponse;
import com.mycompany.grpc.user.UserServiceGrpc;
import org.springframework.stereotype.Component;

@Component
public class UserServiceGrpcClient {
    private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    public UserServiceGrpcClient(UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub) {
        this.userServiceBlockingStub = userServiceBlockingStub;
    }

    public ProductUserProfile getUserProfile(int userId) {
        GetUserProfileRequest request = GetUserProfileRequest.newBuilder()
                .setUserId(userId)
                .build();

        UserProfileResponse response = userServiceBlockingStub.getUserProfile(request);

        return new ProductUserProfile(
                response.getId(),
                response.getName(),
                response.getMembershipLevel(),
                response.getRegion()
        );
    }
}
