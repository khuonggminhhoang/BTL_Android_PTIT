package com.example.foodorderapp.network;

import com.example.foodorderapp.core.model.Experience;
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.network.request.CreateExperienceRequest;
import com.example.foodorderapp.network.request.CreateSkillRequest;
import com.example.foodorderapp.network.request.UpdateExperienceRequest;
import com.example.foodorderapp.network.request.UpdateSkillRequest;
import com.example.foodorderapp.network.response.ExperienceDetailApiResponse;
import com.example.foodorderapp.network.response.ExperiencesApiResponse;
import com.example.foodorderapp.network.response.JobCategoryResponse;
import com.example.foodorderapp.network.response.JobDetailResponse; // Import mới
import com.example.foodorderapp.network.response.PaginatedJobResponse;
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.example.foodorderapp.network.response.SkillDetailApiResponse;
import com.example.foodorderapp.network.response.SkillsApiResponse;

import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
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
import retrofit2.http.Query;

public interface ApiService {

    // Lấy thông tin hồ sơ cá nhân của người dùng hiện tại
    @GET("profile/me")
    Call<ProfileApiResponse> getMyProfile(@Header("Authorization") String authToken);

    // Cập nhật thông tin hồ sơ cá nhân của người dùng hiện tại
    // Bao gồm khả năng tải lên ảnh đại diện
    @Multipart
    @POST("profile/me") // Sử dụng POST hoặc PATCH tùy theo thiết kế API của bạn cho việc cập nhật
    Call<ProfileApiResponse> updateMyProfile(
            @Header("Authorization") String authToken, // Token xác thực
            @PartMap Map<String, RequestBody> fields, // Các trường thông tin cập nhật khác (ví dụ: tên, headline)
            @Part MultipartBody.Part avatarFile // Tệp ảnh đại diện
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

    // Đăng xuất người dùng
    @POST("auth/logout")
    Call<Void> logout(@Header("Authorization") String authToken); // Token xác thực

    // Lấy danh sách công việc có phân trang
    @GET("jobs")
    Call<PaginatedJobResponse> getJobsPaginated(
            @Query("pageNumber") int pageNumber,
            @Query("pageSize") int pageSize,
            @Query("sort") String sort
    );

    // Lấy danh sách tất cả các danh mục công việc
    @GET("job-categories")
    Call<JobCategoryResponse> getJobCategories();

    // PHƯƠNG THỨC MỚI: Lấy chi tiết một công việc
    @GET("jobs/{id}")
    Call<JobDetailResponse> getJobDetail(@Path("id") int jobId); // Không cần token nếu API public
}
