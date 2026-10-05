package com.xq.web.image;

import com.alibaba.fastjson.JSONObject;
import com.xq.config.MinioUtils;
import com.xq.utils.ResultUtils;
import com.xq.utils.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {
    @Autowired
    private MinioUtils minioUtils;

    @RequestMapping("/uploadImage")
    public ResultVo uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("开始文件上传");
        JSONObject images = null;
        try{
            images= minioUtils.uploadFile(file,"gymnasium");
        }catch (Exception e){
            e.printStackTrace();
        }
        return ResultUtils.success("文件上传成功",images);
    }
}
