package com.luckyseven.backend.domain.member.service.utill

import com.luckyseven.backend.domain.member.entity.Member
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

open class MemberDetails : UserDetails {
    val id: Long
    private val password: String
    private val email: String
    private val nickname: String
    private val authorities: Collection<GrantedAuthority>
    
    constructor(member: Member) {
        this.id = member.id!!
        this.email = member.email
        this.nickname = member.nickname
        this.password = member.password
        this.authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
    }
    
    constructor(id: Long, password: String, email: String, nickname: String) {
        this.id = id
        this.password = password
        this.email = email
        this.nickname = nickname
        this.authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
    }
    
    override fun getUsername(): String = email
    

    fun getEmail(): String = email
    
    override fun getAuthorities(): Collection<GrantedAuthority> = authorities
    
    override fun getPassword(): String = password
    
    override fun isAccountNonExpired(): Boolean = true
    
    override fun isAccountNonLocked(): Boolean = true
    
    override fun isCredentialsNonExpired(): Boolean = true
    
    override fun isEnabled(): Boolean = true
} 