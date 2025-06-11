package com.luckyseven.backend.domain.team.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.luckyseven.backend.domain.budget.dao.BudgetRepository;
import com.luckyseven.backend.domain.budget.entity.Budget;
import com.luckyseven.backend.domain.budget.entity.CurrencyCode;
import com.luckyseven.backend.domain.expense.entity.Expense;
import com.luckyseven.backend.domain.expense.enums.ExpenseCategory;
import com.luckyseven.backend.domain.expense.enums.PaymentMethod;
import com.luckyseven.backend.domain.expense.repository.ExpenseRepository;
import com.luckyseven.backend.domain.member.entity.Member;
import com.luckyseven.backend.domain.member.repository.MemberRepository;
import com.luckyseven.backend.domain.team.dto.TeamCreateRequest;
import com.luckyseven.backend.domain.team.dto.TeamJoinRequest;
import com.luckyseven.backend.domain.team.entity.Team;
import com.luckyseven.backend.domain.team.entity.TeamMember;
import com.luckyseven.backend.domain.team.repository.TeamMemberRepository;
import com.luckyseven.backend.domain.team.repository.TeamRepository;
import com.luckyseven.backend.sharedkernel.jwt.utill.JwtTokenizer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TeamControllerTest {

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @MockitoBean
    private JwtTokenizer jwtTokenizer; // Mock JwtTokenizer

    private Member testMember;
    private Team testTeam;
    private String mockToken;

    @BeforeEach
    void setUp() {
        testMember = createTestMember("test@example.com", "테스트사용자");
        testTeam = createTestTeam("테스트팀", "test123", testMember);
        
        // Create mock token
        mockToken = createMockToken(testMember);
        
        // Set up the mock JwtTokenizer
        Claims mockClaims = Jwts.claims()
                .subject(testMember.getId().toString())
                .add("email", testMember.getEmail())
                .add("nickname", testMember.getNickname())
                .build();
        
        Mockito.when(jwtTokenizer.parseAccessToken(Mockito.anyString())).thenReturn(mockClaims);
    }
    
    private String createMockToken(Member member) {
        // This is just for testing, not for production use
        SecretKey key = Keys.hmacShaKeyFor("test-secret-key-for-testing-purposes-only".getBytes());
        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("email", member.getEmail())
                .claim("nickname", member.getNickname())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
                .signWith(key)
                .compact();
    }

    private Member createTestMember(String email, String nickname) {
        Member member = Member.builder()
                .email(email)
                .nickname(nickname)
                .password("password")
                .build();
        return memberRepository.save(member);
    }

    private Team createTestTeam(String name, String password, Member leader) {
        String teamCode = UUID.randomUUID().toString().substring(0, 8);
        
        Team team = Team.builder()
                .name(name)
                .teamPassword(password)
                .leader(leader)
                .teamCode(teamCode)
                .build();
        
        Team savedTeam = teamRepository.save(team);
        
        // Create a budget for the team with a non-null foreignCurrency
        Budget budget = Budget.builder()
                .team(savedTeam)
                .totalAmount(new BigDecimal("0"))
                .balance(new BigDecimal("0"))
                .foreignBalance(new BigDecimal("0"))
                .foreignCurrency(CurrencyCode.KRW)
                .avgExchangeRate(new BigDecimal("1"))
                .setBy(leader.getId())
                .build();
        
        Budget savedBudget = budgetRepository.save(budget);
        savedTeam.setBudget(savedBudget);
        
        return savedTeam;
    }

    @Test
    @DisplayName("팀 생성 테스트")
    void createTeamTest() throws Exception {
        // given
        TeamCreateRequest teamCreateRequest = TeamCreateRequest.builder()
                .name("테스트팀")
                .teamPassword("test123")
                .build();

        // when & then
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamCreateRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("테스트팀"))
                .andExpect(jsonPath("$.leaderId").value(testMember.getId()));
    }

    @Test
    @DisplayName("팀 참가 테스트")
    void joinTeamTest() throws Exception {
        // given
        Member leader = createTestMember("leader@example.com", "팀장");
        Team createdTeam = createTestTeam("참가할팀", "join123", leader); // Use helper method

        TeamJoinRequest teamJoinRequest = new TeamJoinRequest(createdTeam.getTeamCode(), "join123");

        // when & then
        mockMvc.perform(post("/api/teams/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(teamJoinRequest))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("참가할팀"))
                .andExpect(jsonPath("$.teamCode").value(createdTeam.getTeamCode()));
    }

    @Test
    @DisplayName("팀 멤버 조회 테스트")
    void getTeamMembersTest() throws Exception {
        // given
        Member leader = createTestMember("leader2@example.com", "팀장2");
        Team createdTeam = createTestTeam("멤버조회팀", "member123", leader); // Use helper method
        teamMemberRepository.save(TeamMember.builder().team(createdTeam).member(testMember).build());
        teamMemberRepository.save(TeamMember.builder().team(createdTeam).member(leader).build());


        // when & then
        mockMvc.perform(get("/api/teams/" + createdTeam.getId() + "/members")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].memberNickName").exists())
                .andExpect(jsonPath("$[1].memberNickName").exists());
    }

    @Test
    @DisplayName("팀 멤버 삭제 테스트")
    void removeTeamMemberTest() throws Exception {
        // given
        Member leader = createTestMember("leader3@example.com", "팀장3");
        Team createdTeam = createTestTeam("멤버삭제팀", "remove123", leader); // Use helper method
        
        // Add testMember to the team
        TeamMember testMemberTeam = TeamMember.builder().team(createdTeam).member(testMember).build();
        teamMemberRepository.save(testMemberTeam);
        createdTeam.addTeamMember(testMemberTeam);
        testMember.addTeamMember(testMemberTeam);
        
        // Add memberToRemove to the team
        Member memberToRemove = createTestMember("remove@example.com", "삭제될멤버");
        TeamMember memberToRemoveTeam = TeamMember.builder().team(createdTeam).member(memberToRemove).build();
        teamMemberRepository.save(memberToRemoveTeam);
        createdTeam.addTeamMember(memberToRemoveTeam);
        memberToRemove.addTeamMember(memberToRemoveTeam);

        // when & then
        // Create a mock token for the leader
        String leaderToken = createMockToken(leader);
        
        // Set up mock JwtTokenizer
        Claims leaderMockClaims = Jwts.claims()
                .subject(leader.getId().toString())
                .add("email", leader.getEmail())
                .add("nickname", leader.getNickname())
                .build();
        
        Mockito.when(jwtTokenizer.parseAccessToken(Mockito.anyString())).thenReturn(leaderMockClaims);
        
        mockMvc.perform(delete("/api/teams/" + createdTeam.getId() + "/members/" + memberToRemoveTeam.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + leaderToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("팀 대시보드 조회 테스트")
    void getTeamDashboardTest() throws Exception {
        // given
        Member leader = createTestMember("leader4@example.com", "팀장4");
        Team createdTeam = createTestTeam("대시보드팀", "dashboard123", leader); // Use helper method
        
        // Add team members
        TeamMember testMemberTeam = TeamMember.builder().team(createdTeam).member(testMember).build();
        teamMemberRepository.save(testMemberTeam);
        createdTeam.addTeamMember(testMemberTeam);
        testMember.addTeamMember(testMemberTeam);
        
        TeamMember leaderTeam = TeamMember.builder().team(createdTeam).member(leader).build();
        teamMemberRepository.save(leaderTeam);
        createdTeam.addTeamMember(leaderTeam);
        leader.addTeamMember(leaderTeam);

        // Create new budget with USD currency
        Budget oldBudget = createdTeam.getBudget();
        if (oldBudget != null) {
            budgetRepository.delete(oldBudget);
        }
        
        Budget newBudget = Budget.builder()
                .team(createdTeam)
                .totalAmount(new BigDecimal("1000000"))
                .balance(new BigDecimal("1000000"))
                .foreignBalance(new BigDecimal("1000"))
                .foreignCurrency(CurrencyCode.USD)
                .avgExchangeRate(new BigDecimal("1200"))
                .setBy(leader.getId())
                .build();
        
        Budget savedBudget = budgetRepository.save(newBudget);
        createdTeam.setBudget(savedBudget);

        // Add Expense data
        expenseRepository.save(Expense.builder()
                .team(createdTeam)
                .payer(testMember) // Corrected method name
                .description("점심 식사")
                .amount(new BigDecimal("12000"))
                .category(ExpenseCategory.MEAL)
                .paymentMethod(PaymentMethod.CARD)
                .build());

        expenseRepository.save(Expense.builder()
                .team(createdTeam)
                .payer(leader) // Corrected method name
                .description("교통비")
                .amount(new BigDecimal("5000"))
                .category(ExpenseCategory.TRANSPORT) // Corrected enum value
                .paymentMethod(PaymentMethod.CASH)
                .build());


        // when & then
        mockMvc.perform(get("/api/teams/" + createdTeam.getId() + "/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + mockToken)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team_id").value(createdTeam.getId()))
                .andExpect(jsonPath("$.foreignCurrency").value(CurrencyCode.USD.name()))
                .andExpect(jsonPath("$.totalAmount").value(1000000))
                .andExpect(jsonPath("$.expenseList").isArray())
                .andExpect(jsonPath("$.expenseList.length()").value(2))
                .andExpect(jsonPath("$.expenseList[0].description").value("교통비"))
                .andExpect(jsonPath("$.expenseList[1].description").value("점심 식사"));
    }
}
