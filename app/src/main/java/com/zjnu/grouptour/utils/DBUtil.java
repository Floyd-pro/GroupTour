package com.zjnu.grouptour.utils;

import com.zjnu.grouptour.bean.GroupmateInfo;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.bean.Schedule;
import com.zjnu.grouptour.bean.Team;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;


/**
 * @author luchen
 * @Date 2021/4/15 23:33
 * @Description 数据库工具类
 */
public class DBUtil {

    private static final String DB_URL = "jdbc:mysql://121.199.46.84:3306/grouptour?useSSL=false"; // 数据库连接地址
    private static final int PORT = 3306; // 数据库开放端口
    private static final String DB_NAME = "grouptour"; // 数据库名称
    private static final String USER_GENERAL = "user_general"; // 普通用户账号
    private static final String PSW_GENERAL = "123456"; // 普通用户密码
    private static final String USER_ADMIN = "user_admin"; // 管理员账号
    private static final String PSW_ADMIN = "147258"; // 管理员密码
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; // 驱动类

    private static Connection conn = null;
    private static Statement stmt = null;
//    private static PreparedStatement ps = null; // 需在每个方法里单独定义 否则会无法操作数据库
    private static String sql = null;

    private final String formatDateType1 = "yyyy-MM-dd";
    private final String formatDateType2 = "yyyy-MM-dd HH:mm:ss";

    public DBUtil() {

    }

    private volatile static DBUtil instance = null;

    public static DBUtil getInstance() {
        if (instance == null) {
            synchronized (DBUtil.class) {
                if (instance == null) {
                    instance = new DBUtil();
                }
            }
        }
        return instance;
    }

    public static boolean execute() {
        return true;
    }

    public static String execute(String sql) {
        String str = "";
        try{
            // 注册 JDBC 驱动
            Class.forName(JDBC_DRIVER);

            // 打开链接
            System.out.println("连接数据库...");
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);

            // 执行查询
            System.out.println(" 实例化Statement对象...");
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            // 展开结果集数据库
            while(rs.next()){
                // 通过字段检索
                String id = rs.getString("id");
                String name = rs.getString("name");

                // 输出数据
                str += ("ID: " + id);
                str += (", 名称: " + name);
                str += ("\n");
            }
            // 完成后关闭
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            // 处理 JDBC 错误
            se.printStackTrace();
        }catch(Exception e){
            // 处理 Class.forName 错误
            e.printStackTrace();
        }finally{
            // 关闭资源
            try{
                if(stmt!=null) stmt.close();
            }catch(SQLException se2){
            }// 什么都不做
            try{
                if(conn!=null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        str += ("Goodbye, general user!");
        MyLogUtil.LogE(str);
        return str;
    }

    // 管理员操作
    public static boolean insertPerson(Person p) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "INSERT INTO user(name,password) VALUES(?,?);";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, p.getName());
            ps.setString(2, p.getPsw());

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("插入新用户数据成功！\n" + p.toString());
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("插入新用户数据失败！\n" + p.toString());
        return false;
    }
    // 普通用户操作
    public static boolean updatePerson(Person p) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE user SET name=?,password=?,team_id=?,nickname=?,tel=?,wechat=?,qq=?,email=?,signature=?,real_name=?,identity_number=? WHERE id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, p.getName());
            ps.setString(2, p.getPsw());
            ps.setInt(3, p.getTeamID());
            ps.setString(4, p.getNickname());
            ps.setString(5, p.getTel());
            ps.setString(6, p.getWechat());
            ps.setString(7, p.getQq());
            ps.setString(8, p.getEmail());
            ps.setString(9, p.getSignature());
            ps.setString(10, p.getRealName());
            ps.setString(11, p.getIdentityNum());
            ps.setInt(12, p.getId());

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("用户：" + p.getName() + " 更新成功！");
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("用户：" + p.getName() + " 更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updatePerson(int id, String key, int value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE user SET " + key + "=? WHERE id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setInt(1, value);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的用户信息 " + key + " 字段的值成功更新为：" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的用户信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updatePerson(int id, String key, double value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE user SET " + key + "=? WHERE id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setDouble(1, value);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的用户信息 " + key + " 字段的值成功更新为：" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的用户信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updatePerson(int id, String key, String value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "UPDATE user SET " + key + "=? WHERE id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, value);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的用户信息 " + key + " 字段的值成功更新为：" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的用户信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updateLocation(String name, double longitude, double latitude) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE user SET longitude=?,latitude=? WHERE name=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setDouble(1, longitude);
            ps.setDouble(2, latitude);
            ps.setString(3, name);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("名为：" + name + " 的用户定位信息保存成功！ longtitude: " + longitude + " latitude: " + latitude);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("名为：" + name + " 的用户定位信息保存失败！ longtitude: " + longitude + " latitude: " + latitude);
        return false;
    }
    // 管理员操作
    public static boolean deletePerson(String name) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "DELETE FROM user WHERE name=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, name);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("用户：" + name + " 删除成功！");
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("用户：" + name + " 删除失败！");
        return false;
    }
    // 普通用户操作
    public static Person selectPerson(String name) {
        PreparedStatement ps = null;
        Person p = null;
        String str = "";
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            // TODO: 2021/4/18 隐私化查询待优化
            sql = "SELECT id,name,longitude,latitude,team_id,gender,nickname,tel,wechat,qq,email,signature,real_name,identity_number FROM user WHERE name=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            if(rs.next()) {
                p = new Person();

                // 通过字段检索
                p.setId(rs.getInt(1));
                p.setName(rs.getString(2));
                p.setLongitude(rs.getDouble(3));
                p.setLatitude(rs.getDouble(4));
                p.setTeamID(rs.getInt(5));
                p.setGender(rs.getString(6));
                p.setNickname(rs.getString(7));
                p.setTel(rs.getString(8));
                p.setWechat(rs.getString(9));
                p.setQq(rs.getString(10));
                p.setEmail(rs.getString(11));
                p.setSignature(rs.getString(12));
                p.setRealName(rs.getString(13));
                p.setIdentityNum(rs.getString(14));
                // 把 java.sql.Date 与 java.util.Date 之间的转换
//                java.util.Date date = rs.getDate(4);
//                ps.setDate(4, new java.sql.Date(date.getTime()));

                // 输出数据
                str += ("ID: " + p.getId());
                str += (", 用户名: " + p.getName());
                str += (", 经度: " + p.getLongitude());
                str += (", 纬度: " + p.getLatitude());
                str += (", 队伍ID: " + p.getTeamID());
                str += (", 性别: " + p.getTeamID());
                str += (", 昵称: " + p.getNickname());
                str += (", 联系电话: " + p.getTel());
                str += (", 微信号: " + p.getWechat());
                str += (", QQ号: " + p.getQq());
                str += (", 电子邮箱地址: " + p.getEmail());
                str += (", 个性签名: " + p.getSignature());
                str += (", 真实姓名: " + p.getEmail());
                str += (", 身份证号: " + p.getSignature());
                str += ("\n");
                MyLogUtil.LogE("查询用户数据成功:！\n" + str);
            }

            ps.close();
            conn.close();
            return p;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询用户数据失败！");
        return null;
    }
    // 普通用户操作 根据当前用户的用户名 筛选出处于同一队伍中所有用户的用户名
    public static ArrayList<GroupmateInfo> selectGroupmates(int teamID) {
        PreparedStatement ps = null;
        ArrayList<GroupmateInfo> list = null;
        boolean result = false;
        String str = "";
        int index = 0;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "SELECT id,name,longitude,latitude,nickname,tel,gender FROM user WHERE team_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setInt(1, teamID);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            while(rs.next()) {
                if(!result) {
                    list = new ArrayList<GroupmateInfo>();
                    result = true;
                }

                // 通过字段检索
                list.add(new GroupmateInfo(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getDouble(4), rs.getString(5), rs.getString(6), rs.getString(7)));

                // 输出数据
                ++index;
                str += ("第" + index + "位队员: ");
                str += (" ID: " + rs.getInt(1));
                str += (" 用户名: " + rs.getString(2));
                str += (" 昵称: " + rs.getString(5));
                str += (" 当前经度: " + rs.getDouble(3));
                str += (" 当前纬度: " + rs.getDouble(4));
                str += (" 手机号: " + rs.getString(6));
                str += (" 性别: " + rs.getString(7));
                str += ("\n");
            }
            if (!str.equals(""))
                MyLogUtil.LogE("查询队员数据成功！\n" + str);
            ps.close();
            conn.close();
            return list;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询队员数据失败！");
        return null;
    }
    // 管理员操作
    public static boolean insertTeam(Team t) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "INSERT INTO team(team_name,description,principal_name) VALUES(?,?,?);";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, t.getTeamName());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getPrincipalName());

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("插入新队伍数据成功！\n" + t.toString());
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("插入新队伍数据失败！\n" + t.toString());
        return false;
    }
    // 普通用户操作
    public static boolean updateTeam(Team t) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE team SET team_name=?,description=?,destination_name=?,destination_city=?,departure_datetime=?," +
                    "principal_id=?,principal_name=?,principal_tel=?,schedule_id=? WHERE team_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, t.getTeamName());
            ps.setString(2, t.getDescription());
            ps.setString(3, t.getDestinationName());
            ps.setString(4, t.getDestinationCity());
            ps.setDate(5, (Date) t.getDepartureDateTime());
            ps.setInt(6, t.getPrincipalID());
            ps.setString(7, t.getPrincipalName());
            ps.setString(8, t.getPrincipalTel());
            ps.setInt(9, t.getScheduleID());
            ps.setInt(10, t.getTeamID());
            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("名为 " + t.getTeamName() + " 的队伍数据更新成功！");
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("名为 " + t.getTeamName() + " 的队伍数据更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updateTeam(int id, String key, int value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE team SET " + key + "=? WHERE team_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setInt(1, value);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值成功更新为：\n" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updateTeam(int id, String key, double value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE team SET " + key + "=? WHERE team_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setDouble(1, value);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值成功更新为：" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updateTeam(int id, String key, String value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE team SET " + key + "=? WHERE team_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, value);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值成功更新为：\n" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updateTeam(int id, String key, java.util.Date value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE team SET " + key + "=? WHERE team_id=?";
            ps = conn.prepareStatement(sql);

//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Timestamp sqlDate = new Timestamp(value.getTime()); //uilt.Date转sql.Date
            //设置占位符对应的值
            ps.setTimestamp(1, sqlDate);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值成功更新为：\n" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的队伍信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 管理员操作
    public static boolean deleteTeam(String name) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "DELETE FROM team WHERE team_name=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, name);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("队伍：" + name + " 删除成功！");
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("队伍：" + name + " 删除失败！");
        return false;
    }
    // 普通用户操作
    public static Team selectTeam(int id) {
        PreparedStatement ps = null;
        Team t = null;
        String str = "";
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            // TODO: 2021/4/18 隐私化查询待优化
            sql = "SELECT team_id,team_name,description,destination_name,destination_city,destination_longitude,destination_latitude,departure_datetime," +
                    "principal_id,principal_name,principal_tel,schedule_id FROM team WHERE team_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            if(rs.next()) {
                t = new Team();

                // 通过字段检索
                t.setTeamID(rs.getInt(1));
                t.setTeamName(rs.getString(2));
                t.setDescription(rs.getString(3));
                t.setDestinationName(rs.getString(4));
                t.setDestinationCity(rs.getString(5));
                t.setDestinationLongitude(rs.getDouble(6));
                t.setDestinationLatitude(rs.getDouble(7));
                t.setDepartureDateTime(rs.getTimestamp(8));
                t.setPrincipalID(rs.getInt(9));
                t.setPrincipalName(rs.getString(10));
                t.setPrincipalTel(rs.getString(11));
                t.setScheduleID(rs.getInt(12));
                // 把 java.sql.Date 与 java.util.Date 之间的转换
//                java.util.Date date = rs.getDate(4);
//                ps.setDate(4, new java.sql.Date(date.getTime()));

                // 输出数据
                str += ("队伍ID: " + t.getTeamID());
                str += (", 队伍名称: " + t.getTeamName());
                str += (", 队伍简介: " + t.getDescription());
                str += (", 目的地名称: " + t.getDestinationName());
                str += (", 目的地城市: " + t.getDestinationCity());
                str += (", 目的地经度: " + t.getDestinationLongitude());
                str += (", 目的地纬度: " + t.getDestinationLatitude());
                str += (", 出发时间: " + t.getDepartureDateTime());
                str += (", 负责人ID: " + t.getPrincipalID());
                str += (", 负责人姓名: " + t.getPrincipalName());
                str += (", 负责人联系电话: " + t.getPrincipalTel());
                str += (", 队伍行程表ID: " + t.getScheduleID());
                str += ("\n");
                MyLogUtil.LogE("查询id为: " + id + " 的队伍数据成功:！\n" + str);
            }

            ps.close();
            conn.close();
            return t;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询id为: " + id + " 的队伍数据失败！");
        return null;
    }
    // 普通用户操作
    public static Team selectTeam(String name) {
        PreparedStatement ps = null;
        Team t = null;
        String str = "";
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            // TODO: 2021/4/18 隐私化查询待优化
            sql = "SELECT team_id,team_name,description,destination_name,destination_city,destination_longitude,destination_latitude,departure_datetime," +
                    "principal_id,principal_name,principal_tel,schedule_id FROM team WHERE team_name=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            if(rs.next()) {
                t = new Team();

                // 通过字段检索
                t.setTeamID(rs.getInt(1));
                t.setTeamName(rs.getString(2));
                t.setDescription(rs.getString(3));
                t.setDestinationName(rs.getString(4));
                t.setDestinationCity(rs.getString(5));
                t.setDestinationLongitude(rs.getDouble(6));
                t.setDestinationLatitude(rs.getDouble(7));
                t.setDepartureDateTime(rs.getTimestamp(8));
                t.setPrincipalID(rs.getInt(9));
                t.setPrincipalName(rs.getString(10));
                t.setPrincipalTel(rs.getString(11));
                t.setScheduleID(rs.getInt(12));
                // 把 java.sql.Date 与 java.util.Date 之间的转换
//                java.util.Date date = rs.getDate(4);
//                ps.setDate(4, new java.sql.Date(date.getTime()));

                // 输出数据
                str += ("队伍ID: " + t.getTeamID());
                str += (", 队伍名称: " + t.getTeamName());
                str += (", 队伍简介: " + t.getDescription());
                str += (", 目的地名称: " + t.getDestinationName());
                str += (", 目的地城市: " + t.getDestinationCity());
                str += (", 目的地经度: " + t.getDestinationLongitude());
                str += (", 目的地纬度: " + t.getDestinationLatitude());
                str += (", 出发时间: " + t.getDepartureDateTime());
                str += (", 负责人ID: " + t.getPrincipalID());
                str += (", 负责人姓名: " + t.getPrincipalName());
                str += (", 负责人联系电话: " + t.getPrincipalTel());
                str += (", 队伍行程表ID: " + t.getScheduleID());
                str += ("\n");
                MyLogUtil.LogE("查询队伍: " + name + "的数据成功:！\n" + str);
            }

            ps.close();
            conn.close();
            return t;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询队伍: " + name + "的数据失败！");
        return null;
    }
    // 管理员操作
    public static boolean insertSchedule(Schedule s) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "INSERT INTO schedule(schedule_name,schedule_info) VALUES(?,?);";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, s.getScheduleName());
            ps.setString(2, s.getScheduleInfo());

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("插入新行程表数据成功！\n" + s.toString());
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("插入新行程表数据失败！\n" + s.toString());
        return false;
    }
    // 普通用户操作
    public static boolean updateSchedule(Schedule s) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE schedule SET schedule_name=?,schedule_info=? WHERE schedule_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, s.getScheduleName());
            ps.setString(2, s.getScheduleInfo());
            ps.setInt(3, s.getScheduleID());

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("名为 " + s.getScheduleName() + " 的行程表数据更新成功！");
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("名为 " + s.getScheduleName() + " 的行程表数据更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean updateSchedule(int id, String key, String value) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            sql = "UPDATE schedule SET " + key + "=? WHERE schedule_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, value);
            ps.setInt(2, id);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("id：" + id + " 的行程表信息 " + key + " 字段的值成功更新为：" + value);
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("id：" + id + " 的行程表信息 " + key + " 字段的值更新失败！");
        return false;
    }
    // 普通用户操作
    public static boolean deleteSchedule(String name) {
        PreparedStatement ps = null;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "DELETE FROM schedule WHERE schedule_name=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setString(1, name);

            ps.executeUpdate();

            ps.close();
            conn.close();
            MyLogUtil.LogE("行程表：" + name + " 删除成功！");
            return true;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("行程表：" + name + " 删除失败！");
        return false;
    }
    // 普通用户操作
    public static Schedule selectSchedule(int id) {
        PreparedStatement ps = null;
        Schedule s = null;
        String str = "";
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
            // TODO: 2021/4/18 隐私化查询待优化
            sql = "SELECT schedule_id,schedule_name,schedule_info FROM schedule WHERE schedule_id=?";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            if(rs.next()) {
                s = new Schedule();

                // 通过字段检索
                s.setScheduleID(rs.getInt(1));
                s.setScheduleName(rs.getString(2));
                s.setScheduleInfo(rs.getString(3));
                // 把 java.sql.Date 与 java.util.Date 之间的转换
//                java.util.Date date = rs.getDate(4);
//                ps.setDate(4, new java.sql.Date(date.getTime()));

                // 输出数据
                str += ("行程表ID: " + s.getScheduleID());
                str += (", 行程表名称: " + s.getScheduleName());
                str += (", 行程表内容: " + s.getScheduleInfo());
                str += ("\n");
                MyLogUtil.LogE("查询ID为: " + id + "的行程表数据成功:！\n" + str);
            }

            ps.close();
            conn.close();
            return s;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询ID为: " + id + "的行程表数据失败！");
        return null;
    }
    // 管理员操作
    public static ArrayList<Person> selectAllUsers() {
        PreparedStatement ps = null;
        ArrayList<Person> list = null;
        boolean result = false;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "SELECT id,name,password,longitude,latitude,team_id,gender,nickname,tel,wechat,qq,email,signature,real_name,identity_number FROM user WHERE 1=1";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
//            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            while(rs.next()) {
                if(!result) {
                    list = new ArrayList<Person>();
                    result = true;
                }

                // 通过字段检索
                list.add(new Person(rs.getInt(1), rs.getString(2), rs.getString(3),
                                    rs.getDouble(4), rs.getDouble(5), rs.getInt(6),
                                    rs.getString(7), rs.getString(8), rs.getString(9),
                                    rs.getString(10), rs.getString(11), rs.getString(12),
                                    rs.getString(13), rs.getString(14), rs.getString(15)));
                // 把 java.sql.Date 与 java.util.Date 之间的转换
//                java.util.Date date = rs.getDate(4);
//                ps.setDate(4, new java.sql.Date(date.getTime()));

                // 输出数据
                MyLogUtil.LogE("查询所有用户数据成功！");
            }

            ps.close();
            conn.close();
            return list;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询所有用户数据失败！");
        return null;
    }
    // 管理员操作
    public static ArrayList<Team> selectAllTeams() {
        PreparedStatement ps = null;
        ArrayList<Team> list = null;
        boolean result = false;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            sql = "SELECT team_id,team_name,description,destination_name,destination_city,destination_longitude,destination_latitude,departure_datetime," +
                    "principal_id,principal_name,principal_tel,schedule_id FROM team WHERE 1=1";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
//            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            while(rs.next()) {
                if(!result) {
                    list = new ArrayList<Team>();
                    result = true;
                }

                // 通过字段检索
                list.add(new Team(rs.getInt(1), rs.getString(2), rs.getString(3),
                                rs.getString(4), rs.getString(5), rs.getDouble(6),
                                rs.getDouble(7), rs.getTimestamp(8), rs.getInt(9),
                                rs.getString(10), rs.getString(11), rs.getInt(12)));
                // 把 java.sql.Date 与 java.util.Date 之间的转换
//                java.util.Date date = rs.getDate(4);
//                ps.setDate(4, new java.sql.Date(date.getTime()));

                // 输出数据
                MyLogUtil.LogE("查询所有队伍数据成功！");
            }

            ps.close();
            conn.close();
            return list;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询所有队伍数据失败！");
        return null;
    }
    // 管理员操作
    public static ArrayList<Schedule> selectAllSchedules() {
        PreparedStatement ps = null;
        ArrayList<Schedule> list = null;
        boolean result = false;
        try{
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL,USER_ADMIN,PSW_ADMIN);
            // TODO: 2021/4/18 隐私化查询待优化
            sql = "SELECT schedule_id,schedule_name,schedule_info FROM schedule WHERE 1=1";
            ps = conn.prepareStatement(sql);
            //设置占位符对应的值
//            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            // 展开结果集数据库
            while(rs.next()) {
                if(!result) {
                    list = new ArrayList<Schedule>();
                    result = true;
                }

                // 通过字段检索
                list.add(new Schedule(rs.getInt(1), rs.getString(2), rs.getString(3)));

                // 通过字段检索
                // 把 java.sql.Date 与 java.util.Date 之间的转换
//                java.util.Date date = rs.getDate(4);
//                ps.setDate(4, new java.sql.Date(date.getTime()));

                // 输出数据
                MyLogUtil.LogE("查询所有队伍行程表成功！");
            }

            ps.close();
            conn.close();
            return list;
        } catch (SQLException se) {
            // 处理 JDBC 错误
            se.printStackTrace();
        } catch (Exception e) {
            // 处理 Class.forName 错误
            e.printStackTrace();
        } finally {
            // 关闭资源
            try{
                if(ps != null) ps.close();
            }catch(SQLException se2){
                // 什么都不做
            }
            try{
                if(conn != null) conn.close();
            }catch(SQLException se){
                se.printStackTrace();
            }
        }
        MyLogUtil.LogE("查询所有队伍行程表失败！");
        return null;
    }
//    // 普通用户操作 根据同一队伍中的某一用户名 查询 对方的昵称，目前位置
//    public static GroupmateInfo selectGroupmateInfo(String name) {
//        PreparedStatement ps = null;
//        GroupmateInfo g = null;
//        String str = "";
//        try{
//            Class.forName(JDBC_DRIVER);
//            conn = DriverManager.getConnection(DB_URL,USER_GENERAL,PSW_GENERAL);
//            sql = "SELECT id,name,longitude,latitude,nickname FROM user WHERE name=?";
//            ps = conn.prepareStatement(sql);
//            //设置占位符对应的值
//            ps.setString(1, name);
//
//            ResultSet rs = ps.executeQuery();
//
//            // 展开结果集数据库
//            if(rs.next()) {
//                g = new GroupmateInfo();
//
//                // 通过字段检索
//                g.setId(rs.getInt(1));
//                g.setName(rs.getString(2));
//                g.setLongitude(rs.getDouble(3));
//                g.setLatitude(rs.getDouble(4));
//                g.setNickname(rs.getString(5));
//
//                // 输出数据
//                str += ("ID: " + g.getId());
//                str += (", 用户名: " + g.getName());
//                str += (", 经度: " + g.getLongitude());
//                str += (", 纬度: " + g.getLatitude());
//                str += (", 昵称: " + g.getNickname());
//                str += ("\n");
//                MyLogUtil.LogE("查询同组成员数据成功:！\n" + str);
//            }
//
//            ps.close();
//            conn.close();
//            return g;
//        } catch (SQLException se) {
//            // 处理 JDBC 错误
//            se.printStackTrace();
//        } catch (Exception e) {
//            // 处理 Class.forName 错误
//            e.printStackTrace();
//        } finally {
//            // 关闭资源
//            try{
//                if(ps != null) ps.close();
//            }catch(SQLException se2){
//                // 什么都不做
//            }
//            try{
//                if(conn != null) conn.close();
//            }catch(SQLException se){
//                se.printStackTrace();
//            }
//        }
//        MyLogUtil.LogE("查询同组成员数据失败！");
//        return null;
//    }
}
