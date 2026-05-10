package com.clubmgmt.app.data

val mockClubMembers = listOf(
    ClubMember(101, "Alex Doe", "https://i.pravatar.cc/150?u=a042581f4e29026704d"),
    ClubMember(102, "Jane Smith", "https://i.pravatar.cc/150?u=a042581f4e29026704e"),
    ClubMember(103, "Sam Wilson", "https://i.pravatar.cc/150?u=a042581f4e29026704f"),
    ClubMember(104, "Emily Brown", "https://i.pravatar.cc/150?u=a042581f4e29026704a"),
    ClubMember(105, "Chris Lee", "https://i.pravatar.cc/150?u=a042581f4e29026704b"),
    ClubMember(106, "Maria Garcia", "https://i.pravatar.cc/150?u=a042581f4e29026704c"),
    ClubMember(107, "Peter Jones", "https://i.pravatar.cc/150?u=a042581f4e29026704g")
)

val mockClubs = listOf(
    Club(1, "摄影社团", "捕捉瞬间，讲述故事。加入我们一起探索摄影艺术。", 124, "艺术", "https://picsum.photos/seed/photoclub/600/400", Rating(4.5f, 88), mockClubMembers.take(4), listOf(mockClubMembers[6]), ClubStatus.APPROVED),
    Club(2, "编程中心", "从Python到React，我们一起编程。欢迎所有技能水平的人。", 256, "科技", "https://picsum.photos/seed/codehub/600/400", Rating(4.8f, 152), mockClubMembers.subList(1, 5), emptyList(), ClubStatus.APPROVED),
    Club(3, "登山徒步社", "与大自然爱好者一起探索风景优美的小径，征服山峰。", 88, "体育", "https://picsum.photos/seed/trekkers/600/400", Rating(4.7f, 70), mockClubMembers.subList(2, 6), emptyList(), ClubStatus.APPROVED),
    Club(4, "书香社", "为读者提供一个舒适的角落，讨论从经典文学到现代科幻的一切。", 150, "艺术", "https://picsum.photos/seed/bookworms/600/400", Rating(4.6f, 110), mockClubMembers.take(3), emptyList(), ClubStatus.APPROVED),
    Club(5, "AI创新者", "深入人工智能和机器学习项目的世界。", 180, "科技", "https://picsum.photos/seed/aiinnovators/600/400", Rating(4.9f, 130), mockClubMembers.subList(3, 6), emptyList(), ClubStatus.APPROVED),
    Club(6, "天文观测社", "用我们的望远镜和夜空观测活动发现宇宙。", 75, "科学", "https://picsum.photos/seed/stargazers/600/400", Rating(4.8f, 65), mockClubMembers.subList(1, 4), emptyList(), ClubStatus.APPROVED),
    Club(7, "美食创造者", "分享食谱、烹饪技巧，一起享受美味佳肴。", 110, "生活", "https://picsum.photos/seed/culinary/600/400", Rating(4.4f, 90), mockClubMembers.subList(2, 5), emptyList(), ClubStatus.APPROVED),
    Club(8, "电影爱好者", "每周放映电影并讨论摄影和故事叙述。", 95, "艺术", "https://picsum.photos/seed/filmbuffs/600/400", Rating(4.5f, 82), mockClubMembers.take(5), emptyList(), ClubStatus.APPROVED)
)

val mockActivities = listOf(
    Activity(1, "夜空观测", "天文观测社", 6, "2024-08-15", "9:00 PM", "天文台山", "加入我们在星空下观赏英仙座流星雨的奇妙夜晚。", "https://picsum.photos/seed/activity1/600/400", ActivityStatus.APPROVED),
    Activity(2, "React工作坊", "编程中心", 2, "2024-08-18", "10:00 AM", "科技楼301室", "面向初学者的React基础实践工作坊。", "https://picsum.photos/seed/activity2/600/400", ActivityStatus.APPROVED),
    Activity(3, "日出徒步", "登山徒步社", 3, "2024-08-22", "5:00 AM", "鹰峰步道入口", "清晨徒步，在鹰峰欣赏壮丽的日出。", "https://picsum.photos/seed/activity3/600/400", ActivityStatus.APPROVED),
    Activity(4, "人像摄影实践", "摄影社团", 1, "2024-08-25", "2:00 PM", "城市植物园", "在美丽的环境中与摄影同好一起练习人像摄影技巧。", "https://picsum.photos/seed/activity4/600/400", ActivityStatus.APPROVED),
    Activity(5, "图书交换社交", "书香社", 4, "2024-09-01", "4:00 PM", "校园图书馆休息室", "带一本书来，带一本书走！与书友交流你最喜欢的故事。", "https://picsum.photos/seed/activity5/600/400", ActivityStatus.APPROVED)
)

val mockUser = User(
    id = 101,
    name = "Alex Doe",
    email = "alex.doe@example.com",
    avatarUrl = "https://i.pravatar.cc/150?u=a042581f4e29026704d",
    joinedClubs = listOf(1, 3, 4),
    registeredActivities = listOf(3, 4),
    managedClubs = listOf(1),
    ratings = listOf(
        UserRating(2, 5),
        UserRating(3, 4),
        UserRating(6, 5)
    ),
    role = UserRole.ADMIN
)

val mockUsers = listOf(
    mockUser,
    User(102, "Jane Smith", "jane.smith@example.com", "https://i.pravatar.cc/150?u=a042581f4e29026704e", listOf(2, 5), emptyList(), emptyList(), emptyList(), UserRole.USER),
    User(103, "Sam Wilson", "sam.wilson@example.com", "https://i.pravatar.cc/150?u=a042581f4e29026704f", listOf(3), emptyList(), listOf(3), emptyList(), UserRole.ADMIN),
    User(104, "Emily Brown", "emily.brown@example.com", "https://i.pravatar.cc/150?u=a042581f4e29026704a", listOf(1, 4), emptyList(), emptyList(), emptyList(), UserRole.USER),
    User(105, "Chris Lee", "chris.lee@example.com", "https://i.pravatar.cc/150?u=a042581f4e29026704b", listOf(2, 5, 7), emptyList(), emptyList(), emptyList(), UserRole.USER)
)

val mockPendingClubs = listOf(
    Club(9, "辩论协会", "参与有深度的辩论，提升演讲技巧。", 0, "学术", "https://picsum.photos/seed/debate/600/400", Rating(0f, 0), emptyList(), emptyList(), ClubStatus.PENDING),
    Club(10, "绿色环保行动", "致力于在校园推广环保意识和可持续发展的社团。", 0, "社会", "https://picsum.photos/seed/gogreen/600/400", Rating(0f, 0), emptyList(), emptyList(), ClubStatus.PENDING)
)

val mockPendingActivities = listOf(
    Activity(6, "机器学习入门", "AI创新者", 5, "2024-09-05", "6:00 PM", "科学楼第二讲堂", "涵盖机器学习基础及其应用的研讨会。", "https://picsum.photos/seed/activity6/600/400", ActivityStatus.PENDING),
    Activity(7, "意大利美食之夜", "美食创造者", 7, "2024-09-10", "7:00 PM", "学生活动中心厨房", "学习制作正宗手工意面，享受美味的意大利晚餐。", "https://picsum.photos/seed/activity7/600/400", ActivityStatus.PENDING)
)
