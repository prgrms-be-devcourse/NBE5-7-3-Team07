package com.luckyseven.backend.domain.member.service.utill;

import com.luckyseven.backend.domain.member.entity.Member;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class MemberDetails implements UserDetails {
  private final Long id;
  private final String password;
  private final String email;
  private final String nickname;
  private final Collection<? extends GrantedAuthority> authorities;

  public MemberDetails(Member member) {
    this.id          = member.getId();
    this.email       = member.getEmail();
    this.nickname     = member.getNickname();
    this.password = member.getPassword();
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  public MemberDetails(Long id, String password, String email, String nickname) {
    this.id = id;
    this.password = password;
    this.email = email;
    this.nickname = nickname;
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
  }

  public Long getId() { return id; }

  @Override public String getUsername() { return nickname; }
  public String getEmail() { return email; }
  @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

  @Override
  public String getPassword() {
    return password;
  }

  @Override public boolean isAccountNonExpired()     { return true; }
  @Override public boolean isAccountNonLocked()      { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled()               { return true; }
}