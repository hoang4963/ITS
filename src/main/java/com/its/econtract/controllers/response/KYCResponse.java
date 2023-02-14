package com.its.econtract.controllers.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author quangdt
 */
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class KYCResponse {

    @JsonProperty(value = "verify_result")
    private Integer verifyResult;

    @JsonProperty(value = "verify_result_text")
    private String verify_result_text;

    @JsonProperty(value = "sim")
    private Double sim;

    @JsonProperty(value = "verification_time")
    private Double verificationTime;

    @JsonProperty("face_loc_cmt")
    private int[] faceLocCmt;

    @JsonProperty("face1_angle")
    private int face1Angle;

    @JsonProperty(value = "face_loc_live")
    private int[] faceLocLive;

    @JsonProperty(value = "face2_angle")
    private int face2Angle = -16;

    @JsonProperty("live_image_status")
    private String liveImageStatus;

    @JsonProperty("face_anti_spoof_status")
    private FaceAntiSpoofStatus faceAntiSpoofStatus;

    @JsonProperty("message")
    private Message message;

    @JsonProperty("face_card_angle")
    private Double faceCardAngle;

    @JsonProperty("face_live_angle")
    private Double faceLiveAngle;

    @JsonProperty("face_loc_card")
    private int[] faceLocCard;

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("result_code")
    private Integer resultCode;

    @JsonProperty("server_name")
    private String serverName;

    @JsonProperty("version")
    private String version;

    @JsonProperty("wearing_mask")
    private String wearingMask;

    @JsonProperty("wearing_mask_score")
    private Float wearingMaskScore;

    @Getter
    @Setter
    @ToString
    public static class FaceAntiSpoofStatus {
        @JsonProperty("fake_code")
        private String fakeCode;

        @JsonProperty("fake_score")
        private Double fakeScore;

        @JsonProperty("fake_type")
        private String fakeType;

        @JsonProperty("status")
        private String status;
    }

    @Getter
    @Setter
    @ToString
    public static class Message {
        @JsonProperty("api_version")
        private String apiVersion;

        @JsonProperty("error_code")
        private String errorCode;

        @JsonProperty("error_message")
        private String errorMessage;
    }

}


