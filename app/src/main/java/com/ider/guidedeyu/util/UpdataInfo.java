package com.ider.guidedeyu.util;

/**
 * Created by Eric on 2017/5/17.
 */

public class UpdataInfo {

    private String version_code;
    private String version_name;
    private String remark;
    private String md5;
    private String url;
    private String time;

    public String getVersioncode(){
        return version_code;
    }
    public void setVersioncode(String versioncode){
        this.version_code = versioncode;
    }
    public String getVersionname(){
        return version_name;
    }
    public void setVersionname(String versionname){
        this.version_name = versionname;
    }
    public String getRemark(){
        return remark;
    }
    public void setRemark(String remark){
        this.remark = remark;
    }
    public String getMd5(){
        return md5;
    }
    public void setMd5(String md5){
        this.md5 = md5;
    }
    public String getUrl(){
        return url;
    }
    public void setUrl(String url){
        this.url= url;
    }
    public String getTime(){
        return time;
    }
    public void setTime(String time){
        this.time = time;
    }



}
