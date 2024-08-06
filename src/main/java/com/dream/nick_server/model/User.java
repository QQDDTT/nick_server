package com.dream.nick_server.model;

import java.util.Collection;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知属性的 JSON 反序列化
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String id; // 用户 ID
    private String username; // 用户名
    private String password; // 密码
    private String email; // 电子邮件
    private Collection<Authority> authorities; // 权限集合

    // 默认构造函数
    public User() {}

    // 带参数的构造函数
    public User(String id, String username, String password, String email, Collection<Authority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }

    @Override
    public Collection<Authority> getAuthorities() {
        return this.authorities; // 返回用户的权限集合
    }

    public void setAuthorities(Collection<Authority> authorities) {
        this.authorities = authorities; // 设置用户的权限集合
    }

    public String getId() {
        return id; // 返回用户 ID
    }

    public void setId(String id) {
        this.id = id; // 设置用户 ID
    }

    @Override
    public String getUsername() {
        return this.username; // 返回用户名
    }

    public void setUsername(String username) {
        this.username = username; // 设置用户名
    }

    @Override
    public String getPassword() {
        return this.password; // 返回密码
    }

    public void setPassword(String password) {
        this.password = password; // 设置密码
    }

    public String getEmail() {
        return email; // 返回电子邮件
    }

    public void setEmail(String email) {
        this.email = email; // 设置电子邮件
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", email=" + email + ", authorities=" + authorities + "]";
        // 返回用户对象的字符串表示
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true; // 自身比较
        if (obj == null)
            return false; // 对象为空比较
        if (getClass() != obj.getClass())
            return false; // 类别不同比较
        User other = (User) obj;
        if (id == null) {
            if (other.id != null)
                return false; // ID 不同
        } else if (!id.equals(other.id))
            return false;
        if (username == null) {
            if (other.username != null)
                return false; // 用户名不同
        } else if (!username.equals(other.username))
            return false;
        if (password == null) {
            if (other.password != null)
                return false; // 密码不同
        } else if (!password.equals(other.password))
            return false;
        if (email == null) {
            if (other.email != null)
                return false; // 电子邮件不同
        } else if (!email.equals(other.email))
            return false;
        return true; // 所有属性相等
    }
}
