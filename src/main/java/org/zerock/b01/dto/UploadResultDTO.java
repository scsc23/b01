package org.zerock.b01.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UploadResultDTO {

    private String uuid;

    private String fileName;

    private boolean img;

    public String getLink() {

        if (img) {
            return  "s_" + uuid + "_" + fileName;   // 이미지인 경우 s_를 붙여서 썸네일 구분
        } else {
            return uuid + "_" + fileName;
        }
    }
}
