package com.hyphenate.easeim.common.db.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.hyphenate.easeim.common.db.entity.EmUserEntity;
import com.hyphenate.easeui.domain.EaseUser;

import java.util.List;

@Dao
public interface EmUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(EmUserEntity... users);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(List<EmUserEntity> users);

    @Query("select * from em_users where username = :arg0")
    LiveData<List<EaseUser>> loadUserById(String arg0);

    @Query("select * from em_users where contact = 0")
    LiveData<List<EaseUser>> loadUsers();

    @Query("select * from em_users where contact = 0")
    List<EaseUser> loadContacts();

    @Query("select * from em_users where contact = 1")
    LiveData<List<EaseUser>> loadBlackUsers();

    @Query("select * from em_users where contact = 1")
    List<EaseUser> loadBlackEaseUsers();

    @Query("select username from em_users")
    List<String> loadAllUsers();

    @Query("select * from em_users")
    List<EaseUser> loadAllEaseUsers();

    @Query("delete from em_users")
    int clearUsers();

    @Query("delete from em_users where contact = 1")
    int clearBlackUsers();

    @Query("delete from em_users where username = :arg0")
    int deleteUser(String arg0);
}
