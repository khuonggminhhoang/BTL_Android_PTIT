package com.example.foodorderapp.network;

import com.example.foodorderapp.core.model.Experience;
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.network.request.CreateExperienceRequest;
import com.example.foodorderapp.network.request.CreateSkillRequest;
import com.example.foodorderapp.network.request.UpdateExperienceRequest;
import com.example.foodorderapp.network.request.UpdateSkillRequest;
import com.example.foodorderapp.network.response.ExperienceDetailApiResponse;
import com.example.foodorderapp.network.response.ExperiencesApiResponse;
import com.example.foodorderapp.network.response.PaginatedJobResponse; // Import mới
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.example.foodorderapp.network.response.SkillDetailApiResponse;
import com.example.foodorderapp.network.response.SkillsApiResponse;
// import com.example.foodorderapp.network.response.LogoutResponse; // <<< KHÔNG CẦN NỮA

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
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query; // Import Query

public interface ApiService {

    // Các phương thức hiện có...

    @GET("profile/me")
    Call<ProfileApiResponse> getMyProfile(@Header("Authorization") String authToken);

    @Multipart
    @POST("profile/me") // Sử dụng POST hoặc PATCH tùy theo thiết kế API của bạn cho update
    Call<ProfileApiResponse> updateMyProfile(
            @Header("Authorization") String authToken,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part avatarFile
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

    // --- THÊM PHƯƠNG THỨC MỚI CHO PHÂN TRANG CÔNG VIỆC ---
    /**
     * Lấy danh sách công việc có phân trang.
     * Backend của bạn (NestJS) dường như sử dụng các query param là 'pageNumber' và 'pageSize'.
     * Endpoint là /api/v1/jobs (dựa trên phân tích file Node.js)
     */
    @GET("jobs") // Đường dẫn API cho danh sách công việc
    Call<PaginatedJobResponse> getJobsPaginated(
            // Không cần @Header("Authorization") String authToken nếu API này public
            // Nếu API yêu cầu token, hãy thêm vào. Dựa trên JobController.ts, nó là public (@SkipAuth).
            @Query("pageNumber") int pageNumber,
            @Query("pageSize") int pageSize,
            @Query("sort") String sort // Ví dụ: "createdAt,DESC" hoặc "-createdAt" tùy theo backend
            // Dựa trên FilterJobDto, backend có thể nhận 'id,-createdAt'
    );
}
