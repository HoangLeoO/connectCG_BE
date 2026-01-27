package org.example.connectcg_be.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.connectcg_be.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Integer id;
    private String username;
    private String email;
    private boolean isLocked;
    private boolean isDeleted;
    @JsonIgnore
    private String password;
    private boolean isEnabled;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Integer id, String username, String email, String password, boolean isEnabled,
            boolean isLocked, boolean isDeleted,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.isEnabled = isEnabled;
        this.isLocked = isLocked;
        this.isDeleted = isDeleted;
        this.authorities = authorities;
    }

    // Hàm build từ Entity User sang UserPrincipal
    public static UserPrincipal create(User user) {
        // Mặc định role trong DB của bạn là String (VD: "USER"), cần thêm prefix ROLE_
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                Boolean.TRUE.equals(user.getIsEnabled()),
                Boolean.TRUE.equals(user.getIsLocked()),
                Boolean.TRUE.equals(user.getIsDeleted()),
                authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !isLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Tài khoản được bật nếu: đã kích hoạt Email VÀ chưa bị xóa
        return isEnabled && !isDeleted;
    }
}
