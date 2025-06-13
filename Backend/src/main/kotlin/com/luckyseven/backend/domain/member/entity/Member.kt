package com.luckyseven.backend.domain.member.entity

import com.luckyseven.backend.sharedkernel.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "member")
 class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(length = 255, nullable = false)
    val email: String,
    
    @Column(length = 255, nullable = false)
    val password: String,
    
    @Column(length = 50, nullable = false)
    val nickname: String
) : BaseEntity() 