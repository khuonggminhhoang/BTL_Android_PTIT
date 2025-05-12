package com.example.foodorderapp.network;

import com.example.foodorderapp.core.model.Experience; // Thêm import này
import com.example.foodorderapp.core.model.Skill;
import com.example.foodorderapp.network.request.CreateExperienceRequest;
import com.example.foodorderapp.network.request.CreateSkillRequest;
import com.example.foodorderapp.network.request.UpdateExperienceRequest;
import com.example.foodorderapp.network.request.UpdateSkillRequest;
import com.example.foodorderapp.network.response.ExperienceDetailApiResponse;
import com.example.foodorderapp.network.response.ExperiencesApiResponse; // Thêm import này
import com.example.foodorderapp.network.response.ProfileApiResponse;
import com.example.foodorderapp.network.response.SkillDetailApiResponse;
import com.example.foodorderapp.network.response.SkillsApiResponse;
import java.util.List;
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

public interface ApiService {

    @GET("profile/me")
    Call<ProfileApiResponse> getMyProfile(@Header("Authorization") String authToken);

    @Multipart
    @POST("profile/me")
    Call<ProfileApiResponse> updateMyProfile(
            @Header("Authorization") String authToken,
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part avatarFile
    );

    @GET("skills")
    Call<SkillsApiResponse> getCurrentUserSkills(@Header("Authorization") String authToken);

    // PHƯƠNG THỨC MỚI ĐỂ LẤY EXPERIENCES
    @GET("experiences") // Đường dẫn endpoint là /api/v1/experiences
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

    // PHƯƠNG THỨC MỚI ĐỂ XÓA SKILL
    @DELETE("skills/{id}")
    Call<Void> deleteSkill( // Hoặc Call<BaseApiResponse> nếu API trả về JSON
                            @Header("Authorization") String authToken,
                            @Path("id") int skillId
    );

    // PHƯƠNG THỨC MỚI ĐỂ XÓA EXPERIENCE
    @DELETE("experiences/{id}")
    Call<Void> deleteExperience( // Hoặc Call<BaseApiResponse>
                                 @Header("Authorization") String authToken,
                                 @Path("id") int experienceId
    );

    @PATCH("skills/{id}")
    Call<SkillDetailApiResponse> updateSkill( // API trả về đối tượng skill đã cập nhật
                                              @Header("Authorization") String authToken,
                                              @Path("id") int skillId,
                                              @Body UpdateSkillRequest updateSkillRequest // Đối tượng chứa dữ liệu cần cập nhật
    );

    // PHƯƠNG THỨC MỚI ĐỂ CẬP NHẬT EXPERIENCE
    @PATCH("experiences/{id}")
    Call<ExperienceDetailApiResponse> updateExperience( // API trả về đối tượng experience đã cập nhật
                                                        @Header("Authorization") String authToken,
                                                        @Path("id") int experienceId,
                                                        @Body UpdateExperienceRequest updateExperienceRequest // Đối tượng chứa dữ liệu cần cập nhật
    );

    @POST("skills")
    Call<SkillDetailApiResponse> createSkill( // API trả về đối tượng skill đã tạo
                                              @Header("Authorization") String authToken,
                                              @Body CreateSkillRequest createSkillRequest
    );

    // PHƯƠNG THỨC MỚI ĐỂ TẠO EXPERIENCE
    @POST("experiences")
    Call<ExperienceDetailApiResponse> createExperience( // API trả về đối tượng experience đã tạo
                                                        @Header("Authorization") String authToken,
                                                        @Body CreateExperienceRequest createExperienceRequest
    );
}