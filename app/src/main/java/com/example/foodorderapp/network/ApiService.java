package com.example.foodorderapp.network;

import com.example.foodorderapp.network.response.ProfileApiResponse;
import java.util.Map; // Import Map
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart; // Import Multipart
import retrofit2.http.POST;      // Import POST
import retrofit2.http.Part;      // Import Part
import retrofit2.http.PartMap;   // Import PartMap

public interface ApiService {

    @GET("profile/me")
    Call<ProfileApiResponse> getMyProfile(@Header("Authorization") String authToken);

    @Multipart
    @POST("profile/me") // Theo thông tin bạn cung cấp: POST /api/v1/profile/me
    Call<ProfileApiResponse> updateMyProfile(
            @Header("Authorization") String authToken,
            @PartMap Map<String, RequestBody> fields, // Gửi các trường text dưới dạng RequestBody
            @Part MultipartBody.Part avatarFile       // Gửi file avatar
    );

    // Nếu bạn không upload file và chỉ gửi JSON thuần túy cho việc update (ít phổ biến khi có avatar)
    // thì có thể dùng @Body với một đối tượng UserUpdateRequest:
    // @POST("profile/me")
    // Call<ProfileApiResponse> updateMyProfile(@Header("Authorization") String authToken, @Body UserUpdateRequest updateRequest);
}