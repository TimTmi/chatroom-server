package com.example.chatroomserver.repository;

import com.example.chatroomserver.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Integer> { }
