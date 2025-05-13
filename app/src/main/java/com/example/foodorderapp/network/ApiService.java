package com.example.foodorderapp.network;

import com.example.foodorderapp.core.model.Experience;
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.network.request.CreateExperienceRequest;
import com.example.foodorderapp.network.request.CreateSkillRequest;
import com.example.foodorderapp.network.request.UpdateExperienceRequest;
import com.example.foodorderapp.network.request.UpdateSkillRequest;
import com.example.foodorderapp.network.response.CompaniesApiResponse;
import com.example.foodorderapp.network.response.ExperienceDetailApiResponse;
import com.example.foodorderapp.network.response.ExperiencesApiResponse;
import com.example.foodorderapp.network.response.JobCategoryResponse;
import com.example.foodorderapp.network.response.JobDetailResponse;
import com.example.foodorderapp.network.response.PaginatedJobResponse;
import com.example.foodorderapp.network.response.NotificationDetailApiResponse;
import com.example.foodorderapp.network.response.NotificationsApiResponse;
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.example.foodorderapp.network.response.SkillDetailApiResponse;
import com.example.foodorderapp.network.response.SkillsApiResponse;
// import com.example.foodorderapp.network.response.LogoutResponse; // <<< KHÔNG CẦN NỮA

import java.util.List; // Thêm import này
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
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
import retrofit2.http.Query;
import androidx.annotation.Nullable;

public interface ApiService {

    // Lấy thông tin hồ sơ cá nhân của người dùng hiện tại
    @GET("profile/me")
    @Headers("Cache-Control: no-cache")
    Call<ProfileApiResponse> getMyProfile(@Header("Authorization") String authToken);

    @Multipart
    @POST("profile/me") // Sử dụng POST hoặc PATCH tùy theo thiết kế API của bạn cho việc cập nhật
    Call<ProfileApiResponse> updateMyProfile(
            @Header("Authorization") String authToken,
            @PartMap Map<String, RequestBody> fields,
            @Part @Nullable MultipartBody.Part avatarFile
    );

    // Lấy danh sách kỹ năng của người dùng hiện tại
    @GET("skills")
    Call<SkillsApiResponse> getCurrentUserSkills(@Header("Authorization") String authToken);

    // Lấy danh sách kinh nghiệm làm việc của người dùng hiện tại
    @GET("experiences")
    Call<ExperiencesApiResponse> getCurrentUserExperiences(@Header("Authorization") String authToken);

    // Lấy chi tiết một kỹ năng cụ thể
    @GET("skills/{id}")
    Call<SkillDetailApiResponse> getSkillDetail(
            @Header("Authorization") String authToken, // Token xác thực
            @Path("id") int skillId // ID của kỹ năng
    );

    // Lấy chi tiết một kinh nghiệm làm việc cụ thể
    @GET("experiences/{id}")
    Call<ExperienceDetailApiResponse> getExperienceDetail(
            @Header("Authorization") String authToken, // Token xác thực
            @Path("id") int experienceId // ID của kinh nghiệm
    );

    // Xóa một kỹ năng
    @DELETE("skills/{id}")
    Call<Void> deleteSkill(
            @Header("Authorization") String authToken, // Token xác thực
            @Path("id") int skillId // ID của kỹ năng cần xóa
    );

    // Xóa một kinh nghiệm làm việc
    @DELETE("experiences/{id}")
    Call<Void> deleteExperience(
            @Header("Authorization") String authToken, // Token xác thực
            @Path("id") int experienceId // ID của kinh nghiệm cần xóa
    );

    // Cập nhật một kỹ năng hiện có
    @PATCH("skills/{id}")
    Call<SkillDetailApiResponse> updateSkill(
            @Header("Authorization") String authToken, // Token xác thực
            @Path("id") int skillId, // ID của kỹ năng cần cập nhật
            @Body UpdateSkillRequest updateSkillRequest // Dữ liệu cập nhật
    );

    // Cập nhật một kinh nghiệm làm việc hiện có
    @PATCH("experiences/{id}")
    Call<ExperienceDetailApiResponse> updateExperience(
            @Header("Authorization") String authToken, // Token xác thực
            @Path("id") int experienceId, // ID của kinh nghiệm cần cập nhật
            @Body UpdateExperienceRequest updateExperienceRequest // Dữ liệu cập nhật
    );

    // Tạo một kỹ năng mới
    @POST("skills")
    Call<SkillDetailApiResponse> createSkill(
            @Header("Authorization") String authToken, // Token xác thực
            @Body CreateSkillRequest createSkillRequest // Dữ liệu để tạo kỹ năng mới
    );

    // Tạo một kinh nghiệm làm việc mới
    @POST("experiences")
    Call<ExperienceDetailApiResponse> createExperience(
            @Header("Authorization") String authToken, // Token xác thực
            @Body CreateExperienceRequest createExperienceRequest // Dữ liệu để tạo kinh nghiệm mới
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

    // Lấy danh sách công việc có phân trang và lọc
    @GET("jobs")
    Call<PaginatedJobResponse> getJobsFiltered(
            @Query("pageNumber") Integer pageNumber,
            @Query("pageSize") Integer pageSize,
            @Query("sort") String sort,
            @Query("jobCategoryId") Integer jobCategoryId,
            @Query("location") String location,
            @Query("search") String search,
            @Query("salaryGte") Integer salaryGte,
            @Query("salaryLte") Integer salaryLte,
            @Query("isTopJob") Boolean isTopJob,
            @Query("searchFields") List<String> searchFields // <<< THÊM THAM SỐ NÀY
    );


    // Lấy danh sách tất cả các danh mục công việc
    @GET("job-categories")
    Call<JobCategoryResponse> getJobCategories();

    // PHƯƠNG THỨC MỚI: Lấy chi tiết một công việc
    @GET("jobs/{id}")
    Call<JobDetailResponse> getJobDetail(@Path("id") int jobId);

    // Lấy danh sách top các công ty
    @GET("companies/top")
    Call<CompaniesApiResponse> getTopCompanies();
}
