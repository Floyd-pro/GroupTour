package com.zjnu.grouptour.utils;

import com.zjnu.grouptour.api.GroupTourApi;

/**
 * @author luchen
 * @Date 2021/4/1 10:00
 * @Description 常量类
 */
public class ConstantUtils {

    public static final boolean ACCEPT_INVITATION_ALWAYS = false; // 默认添加好友时，false为不需要验证，true为需要验证
    public static final boolean EMCLIENT_DEBUG_MODE = true;

    // MySQL 8.0 以下版本 - JDBC 驱动名及数据库 URL (连本地)
    public static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    public static final String DB_URL = "jdbc:mysql://192.168.0.105:3306/GrouptourDB";
    // MySQL 8.0 以上版本 - JDBC 驱动名及数据库 URL
//    public static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
//    public static final String DB_URL = "jdbc:mysql://localhost:3306/GrouptourDB?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    // 数据库的用户名与密码，需要根据自己的设置
    public static final String USER = "luchen";
    public static final String PASS = "luchen@123456";

    // 阿里云RDS MySQL数据库 配置 (连云数据库，未购买ECS服务器产品并绑定，不能使用)
//    public static final String INTRANET_ADDR = "rm-bp1zz980djkz81t73.mysql.rds.aliyuncs.com"; // 内网地址
//    public static final String INTERNET_ADDR = "rm-bp1zz980djkz81t73wo.mysql.rds.aliyuncs.com"; // 外网地址
//    public static final int INTRANET_PORT = 3306; // 内网端口
//    public static final int INTERNET_PORT = 3306; // 外网端口
//    public static final String DB_USER = "admin_dev";
//    public static final String DB_PASSWORD = "admin_dev@123456";
//    public static final String DATABASE = "grouptour";

    // 阿里云 轻量应用服务器 MySQL数据库 配置 (云上)
    public static String IP = "121.199.46.84"; // 公网IP地址
    public static int PORT = 3306; // 数据库开放端口
    public static String USER_GENERAL = "user_general"; // 普通用户账号
    public static String USER_GENERAL_PSW = "123456"; // 普通用户密码
    public static String USER_ADMIN = "user_admin"; // 管理员账号
    public static String USER_ADMIN_PSW = "147258"; // 管理员密码

    public static int API_INDEX = 1;// 控制测试0、灰度1、正式2，场景id或者api
    public static int LOCATION_DISTANCE_SPACING = 1; // 定位距离间隔 默认0 单位米
    public static int LOCATION_TIME_SPACING = 2000; // 定位时间间隔 默认2000 单位毫秒
    public static String LOCATION_MODE = "hight"; // 定位灵敏度 默认hight 分别是hight,middle,low

    //请求接口地址

}