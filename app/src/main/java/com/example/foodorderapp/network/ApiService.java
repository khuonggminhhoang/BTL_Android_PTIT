package com.example.foodorderapp.network;

import com.example.foodorderapp.core.model.Experience;
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.network.request.CreateExperienceRequest;
import com.example.foodorderapp.network.request.CreateSkillRequest;
import com.example.foodorderapp.network.request.UpdateExperienceRequest;
import com.example.foodorderapp.network.request.UpdateSkillRequest;
import com.example.foodorderapp.network.response.ExperienceDetailApiResponse;
import com.example.foodorderapp.network.response.ExperiencesApiResponse;
import com.example.foodorderapp.network.response.NotificationDetailApiResponse;
import com.example.foodorderapp.network.response.NotificationsApiResponse;
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.example.foodorderapp.network.response.SkillDetailApiResponse;
import com.example.foodorderapp.network.response.SkillsApiResponse;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.example.foodorderapp.config.Config;
import com.example.foodorderapp.features.auth.ui.activity.LoginActivity;


import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import androidx.annotation.Nullable;

public interface ApiService {

    @GET("profile/me")
    @Headers("Cache-Control: no-cache")
    Call<ProfileApiResponse> getMyProfile(@Header("Authorization") String authToken);

    @Multipart
    @POST("profile/me")
    Call<ProfileApiResponse> updateMyProfile(
            @Header("Authorization") String authToken,
            @PartMap Map<String, RequestBody> fields,
            @Part @Nullable MultipartBody.Part avatarFile
    );

    @GET("skills")
    Call<SkillsApiResponse> getCurrentUserSkills(@Header("Authorization") String authToken);

    @GET("experiences")
    Call<ExperiencesApiResponse> getCurrentUserExperiences(@Header("Authorization") String authToken);

    @GET("skills/{id}")
    Call<SkillDetailApiResponse> getSkillDetail(
            @Header("Authorization") String authToken,
            @Path("id") int skillId
    );

    @GET("experiences/{id}")
    Call<ExperienceDetailApiResponse> getExperienceDetail(
            @Header("Authorization") String authToken,
            @Path("id") int experienceId
    );

    @DELETE("skills/{id}")
    Call<Void> deleteSkill(
            @Header("Authorization") String authToken,
            @Path("id") int skillId
    );

    @DELETE("experiences/{id}")
    Call<Void> deleteExperience(
            @Header("Authorization") String authToken,
            @Path("id") int experienceId
    );

    @PATCH("skills/{id}")
    Call<SkillDetailApiResponse> updateSkill(
            @Header("Authorization") String authToken,
            @Path("id") int skillId,
            @Body UpdateSkillRequest updateSkillRequest
    );

    @PATCH("experiences/{id}")
    Call<ExperienceDetailApiResponse> updateExperience(
            @Header("Authorization") String authToken,
            @Path("id") int experienceId,
            @Body UpdateExperienceRequest updateExperienceRequest
    );

    @POST("skills")
    Call<SkillDetailApiResponse> createSkill(
            @Header("Authorization") String authToken,
            @Body CreateSkillRequest createSkillRequest
    );

    @POST("experiences")
    Call<ExperienceDetailApiResponse> createExperience(
            @Header("Authorization") String authToken,
            @Body CreateExperienceRequest createExperienceRequest
    );

    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String authToken);

    @Multipart
    @POST("profile/me/portfolio")
    Call<ProfileApiResponse> uploadPortfolio(
                                              @Header("Authorization") String authToken,
                                              @Part MultipartBody.Part file
    );

    @DELETE("profile/me/portfolio")
    Call<Void> deletePortfolio(@Header("Authorization") String authToken);

    @GET("notifications")
    Call<NotificationsApiResponse> getNotifications(@Header("Authorization") String authToken);

    @GET("notifications/{id}")
    Call<NotificationDetailApiResponse> getNotificationDetail(@Header("Authorization") String authToken, @Path("id") int notificationId);

    @PATCH("notifications/{id}/read")
    Call<NotificationDetailApiResponse> markNotificationAsRead(@Header("Authorization") String authToken, @Path("id") int notificationId);
}