package com.distributed_system.purchasement.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("tb_user")
public class User {

    private String username;
    private String age;

    // Default constructor for deserialization
    public User() {
    }

    public User(String username, int age) {
        this.username = username;
        this.age = String.valueOf(age);
    }
}
