package com.red.yogaback.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "User")
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; // user_id

    private String userLoginId;
    private String userName;
    private String userPwd;
    private String userNickname;
    private String userProfile;
    private Long createdAt;
    private Long modifyAt;

    @ManyToOne
    private Room room;

    // 한 사용자가 여러 RoomRecord(방 기록)에 참여함
    @OneToMany(mappedBy = "user")
    private List<RoomRecord> roomRecords;

    // 한 사용자가 여러 PoseRecord(자세 기록)를 가질 수 있음
    @OneToMany(mappedBy = "user")
    private List<PoseRecord> poseRecords;

    // 사용자에 대한 추가 기록 (1:1)
    @OneToOne(mappedBy = "user")
    private UserRecord userRecord;

    // 사용자가 획득한 뱃지 내역 (1대다)
    @OneToMany(mappedBy = "user")
    private List<UserBadge> userBadges;

    // 사용자가 등록한 코스 (1대다)
    @OneToMany(mappedBy = "user")
    private List<UserCourse> userCourses;
}
