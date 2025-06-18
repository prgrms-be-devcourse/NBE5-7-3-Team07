import axios from 'axios';
import { postRefreshToken, publicApi, privateApi } from "./ApiService";

// axios 기본 설정에 UTF-8 인코딩 추가
axios.defaults.headers.common['Accept'] = 'application/json; charset=utf-8';
axios.defaults.headers.post['Content-Type'] = 'application/json; charset=utf-8';
axios.defaults.headers.put['Content-Type'] = 'application/json; charset=utf-8';
axios.defaults.headers.patch['Content-Type'] = 'application/json; charset=utf-8';

// 사용자 정보 저장
let currentUser = null;

// 토큰 관리 유틸리티 함수들
const TokenManager = {
    // 토큰 설정 (localStorage + axios 헤더)
    setToken: (token) => {
        if (token) {
            console.log("토큰 설정:", token.substring(0, 10) + "...");
            localStorage.setItem('accessToken', token);
            axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
            console.log("Authorization 헤더 설정됨:", axios.defaults.headers.common['Authorization']);
        }
    },
    
    // 토큰 가져오기
    getToken: () => {
        return localStorage.getItem('accessToken');
    },
    
    // 토큰 제거
    removeToken: () => {
        localStorage.removeItem('accessToken');
        delete axios.defaults.headers.common['Authorization'];
        console.log("토큰이 제거되었습니다.");
    },
    
    // localStorage에서 토큰 복원
    restoreToken: () => {
        const accessToken = localStorage.getItem('accessToken');
        if (accessToken) {
            console.log("로컬 스토리지에서 accessToken 복원:", accessToken.substring(0, 10) + "...");
            axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
            console.log("로컬 스토리지의 accessToken으로 Authorization 헤더 설정됨");
            return true;
        }
        return false;
    }
};

// 사용자 정보 관리 유틸리티 함수들 (한글 인코딩 지원)
const UserManager = {
    // 사용자 정보 설정 (한글 안전 저장)
    setUser: (user) => {
        if (user) {
            console.log("사용자 정보 저장:", user);
            currentUser = user;
            
            // 한글 인코딩 문제를 방지하기 위해 Base64로 인코딩하여 저장
            try {
                const userString = JSON.stringify(user);
                // encodeURIComponent로 한글을 URL 인코딩한 후 Base64로 인코딩
                const encodedUser = btoa(encodeURIComponent(userString));
                localStorage.setItem('currentUser', encodedUser);
                console.log("사용자 정보 Base64 인코딩 저장 완료");
            } catch (error) {
                console.error("사용자 정보 저장 오류:", error);
                // 기본 방식으로 폴백
                localStorage.setItem('currentUser', JSON.stringify(user));
            }
        }
    },
    
    // 사용자 정보 가져오기 (한글 안전 복원)
    getUser: () => {
        if (!currentUser) {
            try {
                const storedUser = localStorage.getItem('currentUser');
                if (storedUser) {
                    try {
                        // Base64 디코딩 후 URL 디코딩으로 한글 복원
                        const decodedUserString = decodeURIComponent(atob(storedUser));
                        currentUser = JSON.parse(decodedUserString);
                        console.log("사용자 정보 Base64 디코딩 성공:", currentUser);
                    } catch (decodeError) {
                        // Base64 디코딩 실패 시 기본 JSON.parse 시도
                        console.warn("Base64 디코딩 실패, 기본 파싱 시도:", decodeError);
                        currentUser = JSON.parse(storedUser);
                    }
                }
            } catch (error) {
                console.error("로컬 스토리지 사용자 데이터 로드 오류:", error);
                // 오류 발생 시 localStorage에서 해당 데이터 제거
                localStorage.removeItem('currentUser');
            }
        }
        return currentUser;
    },
    
    // 사용자 정보 제거
    removeUser: () => {
        currentUser = null;
        localStorage.removeItem('currentUser');
        console.log("사용자 정보가 제거되었습니다.");
    },
    
    // 깨진 사용자 정보 복구 (한 번만 실행)
    repairCorruptedUserData: () => {
        const repairKey = 'userDataRepaired_v2'; // 버전 업데이트
        if (localStorage.getItem(repairKey)) {
            return; // 이미 복구됨
        }
        
        try {
            const storedUser = localStorage.getItem('currentUser');
            if (storedUser && currentUser) {
                // 현재 메모리의 사용자 정보를 다시 저장하여 인코딩 문제 해결
                console.log("사용자 정보 인코딩 복구 중...");
                UserManager.setUser(currentUser);
                localStorage.setItem(repairKey, 'true');
                console.log("사용자 정보 인코딩 복구 완료");
            }
        } catch (error) {
            console.error("사용자 정보 복구 중 오류:", error);
        }
    }
};

// 로컬 스토리지에서 사용자 정보와 토큰 복원
try {
    // 사용자 정보 불러오기
    UserManager.getUser();
    
    // 깨진 사용자 정보 복구 시도
    UserManager.repairCorruptedUserData();
    
    // accessToken 불러오기 및 헤더에 설정
    TokenManager.restoreToken();
} catch (error) {
    console.error("로컬 스토리지 데이터 로드 오류:", error);
}

// 이메일 인증 요청 함수
export const requestEmailVerification = async (email) => {
    return await publicApi.post(`/api/email/request-email`, { email });
}

export const join = async (req) => {
    return await publicApi.post(`/api/users/register`, req);
}

export const login = async (req) => {
    const response = await publicApi.post(`/api/users/login`, req);
    
    console.log("=== 로그인 응답 디버깅 ===");
    console.log("응답 상태:", response.status);
    console.log("응답 전체:", response);
    console.log("응답 데이터:", response.data);
    console.log("응답 헤더:", response.headers);
    
    // 로그인 성공 시 토큰과 사용자 정보 처리
    if (response.status === 200) {
        console.log("로그인 성공, 데이터 파싱 시작");
        console.log("response.data 내용:", JSON.stringify(response.data, null, 2));
        
        // 로그인 후 쿠키 상태 확인
        console.log("=== 로그인 후 쿠키 상태 ===");
        console.log("전체 쿠키:", document.cookie);
        const cookies = document.cookie.split(';');
        const refreshTokenCookie = cookies.find(cookie => cookie.trim().startsWith('refreshToken='));
        console.log("refreshToken 쿠키:", refreshTokenCookie ? refreshTokenCookie.trim() : "없음");
        
        // 헤더에서 토큰 추출
        const accessToken = response.headers?.authorization?.replace('Bearer ', '') ||
                          response.headers?.Authorization?.replace('Bearer ', '') ||
                          response.data?.accessToken || 
                          response.data?.access_token || 
                          response.data?.token;
                          
        // 사용자 정보는 보통 response body에 있거나, 토큰에서 디코딩해야 함
        const user = response.data?.user || 
                    response.data?.userInfo || 
                    response.data?.userData;
        
        console.log("추출된 accessToken:", accessToken ? accessToken.substring(0, 10) + "..." : "없음");
        console.log("추출된 user:", user);
        
        // 토큰 설정 (통합된 함수 사용)
        if (accessToken) {
            console.log("토큰 설정 중...");
            TokenManager.setToken(accessToken);
            console.log("토큰 설정 완료, localStorage 확인:", localStorage.getItem('accessToken') ? "저장됨" : "저장안됨");
            
            // 사용자 정보가 없으면 토큰에서 추출 시도 (한글 지원 JWT 디코딩)
            if (!user && accessToken) {
                try {
                    // JWT payload 추출 시 한글 인코딩 고려
                    const base64Payload = accessToken.split('.')[1];
                    // Base64 디코딩 시 한글 문자를 위한 처리
                    const decodedPayload = decodeURIComponent(escape(atob(base64Payload)));
                    const payload = JSON.parse(decodedPayload);
                    console.log("토큰에서 추출한 payload:", payload);
                    
                    // JWT payload에서 사용자 정보 구성
                    const userFromToken = {
                        id: payload.sub,
                        email: payload.email,
                        nickname: payload.nickname
                    };
                    
                    if (userFromToken.id || userFromToken.email) {
                        console.log("토큰에서 추출한 사용자 정보:", userFromToken);
                        UserManager.setUser(userFromToken);
                    }
                } catch (tokenError) {
                    console.warn("토큰 디코딩 실패, 기본 방식 시도:", tokenError);
                    // 기본 방식으로 폴백
                    try {
                        const payload = JSON.parse(atob(accessToken.split('.')[1]));
                        const userFromToken = {
                            id: payload.sub,
                            email: payload.email,
                            nickname: payload.nickname
                        };
                        if (userFromToken.id || userFromToken.email) {
                            UserManager.setUser(userFromToken);
                        }
                    } catch (fallbackError) {
                        console.error("토큰 디코딩 완전 실패:", fallbackError);
                    }
                }
            }
        } else {
            console.warn("⚠️ 응답에서 accessToken을 찾을 수 없습니다!");
            console.log("헤더 내용:", JSON.stringify(response.headers, null, 2));
            if (response.data && typeof response.data === 'object') {
                console.log("가능한 데이터 필드들:");
                Object.keys(response.data).forEach(key => {
                    console.log(`- ${key}:`, response.data[key]);
                });
            }
        }
        
        // 별도로 전달된 사용자 정보 설정 (한글 안전 저장)
        if (user) {
            console.log("응답에서 받은 사용자 정보 설정 중...");
            UserManager.setUser(user);
        }
    } else {
        console.warn("로그인 실패:", response.status, response.data);
    }
    
    console.log("=== 로그인 처리 완료 ===");
    return response;
}

export const getCurrentUser = () => {
    console.log("getCurrentUser 호출됨");
    const user = UserManager.getUser();
    console.log("현재 사용자:", user);
    console.log("현재 Authorization 헤더:", axios.defaults.headers.common['Authorization'] || "없음");
    return user;
}

export const checkEmailDuplicate = async(email) => {
    try{
        const resp = await publicApi.post("api/users/checkEmail",{params : {email : email}});
        if(resp.status === 200){
            alert("사용 가능한 이메일입니다.");
        }
    }catch(err){
        console.log(err);
    }
}

export const logout = async() => {
    try {
        console.log("logout 진입");
        
        // 현재 인증 상태 확인
        console.log("로그아웃 전 인증 상태:");
        console.log("- currentUser:", currentUser);
        console.log("- Authorization 헤더:", axios.defaults.headers.common['Authorization'] || "없음");
        
                // 서버 환경의 필터 문제로 인해 클라이언트에서만 로그아웃 처리
        console.log("=== 서버 환경 문제로 클라이언트 측 로그아웃만 실행 ===");
        console.log("쿠키 상태 (httpOnly 제외):", document.cookie);
        console.log("⚠️ 서버 API 호출 없이 클라이언트 상태만 정리합니다.");
        
        // TODO: 백엔드 JwtAuthenticationFilter 수정 후 서버 API 호출 복원 필요
        
        try {
            // 임시로 서버 API 호출 주석 처리
            /*
            const response = await publicApi.post("/api/users/logout", {}, {
                withCredentials: true,
                headers: { 'Content-Type': 'application/json' }
            });
            console.log("로그아웃 API 성공:", response.status);
            */
            
            console.log("클라이언트 측 로그아웃 처리 시작...");
        } catch (apiError) {
            // 에러 발생 시에도 클라이언트 정리는 수행
            console.warn("서버 로그아웃 실패하지만 클라이언트 정리는 계속 진행:", apiError.message);
        }
        
        // 최종적으로 클라이언트 상태 정리 (혹시 위에서 처리되지 않았을 경우 안전장치)
        console.log("최종 클라이언트 상태 정리 확인...");
        if (localStorage.getItem('accessToken') || currentUser) {
            console.log("아직 토큰/사용자 정보가 남아있음 - 추가 정리 수행");
            TokenManager.removeToken();
            UserManager.removeUser();
        } else {
            console.log("이미 모든 토큰/사용자 정보가 정리됨");
        }
        
        console.log("로그아웃 후 인증 상태:");
        console.log("- currentUser:", currentUser);
        console.log("- Authorization 헤더:", axios.defaults.headers.common['Authorization'] || "없음");
        
        console.log("로그아웃 완료: 사용자 정보 및 토큰 제거됨");
        
        return { success: true };
    } catch (err) {
        console.error("로그아웃 처리 중 오류:", err);
        
        // 오류가 발생해도 클라이언트 상태 정리
        TokenManager.removeToken();
        UserManager.removeUser();
        
        return { success: false, error: err.message };
    }
}

// 토큰 갱신 요청 함수 (서버 환경 문제로 임시 비활성화)
export const refreshAccessToken = async () => {
    try {
        console.log("⚠️ 서버 환경 문제로 토큰 갱신 기능 임시 비활성화");
        console.log("사용자에게 재로그인 안내 필요");
        
        // 서버 환경의 JwtAuthenticationFilter 문제로 인해 임시 비활성화
        return { 
            success: false, 
            error: "서버 환경 문제로 인해 토큰 갱신이 불가능합니다. 다시 로그인해주세요.",
            requireReLogin: true 
        };
        
        /* 백엔드 수정 후 복원 필요
        console.log("토큰 갱신 요청 시작");
        console.log("현재 인증 상태:");
        console.log("- currentUser:", currentUser);
        console.log("- Authorization 헤더:", axios.defaults.headers.common['Authorization']);
        
        // refreshToken을 사용하여 accessToken 갱신 요청
        const response = await postRefreshToken();
        console.log("토큰 갱신 응답:", response.data);
        
        // 새 액세스 토큰 설정 (통합된 함수 사용)
        const newAccessToken = response.data.accessToken;
        if (newAccessToken) {
            console.log("새 액세스 토큰 설정:", newAccessToken.substring(0, 10) + "...");
            TokenManager.setToken(newAccessToken); // 통합된 함수 사용
            
            return { 
                success: true, 
                data: response.data,
                message: "토큰이 성공적으로 갱신되었습니다."
            };
        } else {
            console.warn("응답에 새 액세스 토큰이 포함되어 있지 않습니다:", response.data);
            return { 
                success: false, 
                error: "응답에 액세스 토큰이 없습니다.",
                data: response.data
            };
        }
        */
    } catch (error) {
        console.error("토큰 갱신 실패:", error);
        
        if (error.response) {
            console.error("서버 응답:", error.response.status, error.response.data);
        }
        
        // 에러 발생 시 인증 상태 초기화 여부 결정
        if (error.response && error.response.status === 401) {
            console.warn("인증 만료: 사용자 정보 초기화");
            TokenManager.removeToken();
            UserManager.removeUser();
        }
        
        return { 
            success: false, 
            error: error.message,
            status: error.response?.status
        };
    }
};

// 이메일 인증 상태 확인 함수
export const verifyEmailToken = async (token) => {
    return await publicApi.get(`/api/email/verify?token=${token}`);
}

// 유틸리티 함수들을 외부에서 사용할 수 있도록 export
export { TokenManager, UserManager };

// 한글 인코딩 테스트 함수 (개발용)
export const testKoreanEncoding = () => {
    const testUser = {
        id: "test123",
        email: "test@example.com",
        nickname: "테스트닉네임한글"
    };
    
    console.log("=== 한글 인코딩 테스트 시작 ===");
    console.log("원본 사용자 정보:", testUser);
    
    // 저장 테스트
    UserManager.setUser(testUser);
    
    // 현재 메모리 사용자 정보 제거
    currentUser = null;
    
    // 복원 테스트
    const restoredUser = UserManager.getUser();
    console.log("복원된 사용자 정보:", restoredUser);
    
    // 닉네임 비교
    const isNicknameMatch = testUser.nickname === restoredUser?.nickname;
    console.log("닉네임 일치 여부:", isNicknameMatch);
    console.log("원본 닉네임:", testUser.nickname);
    console.log("복원된 닉네임:", restoredUser?.nickname);
    
    if (isNicknameMatch) {
        console.log("✅ 한글 인코딩 테스트 성공!");
    } else {
        console.log("❌ 한글 인코딩 테스트 실패!");
    }
    
    console.log("=== 한글 인코딩 테스트 완료 ===");
    return isNicknameMatch;
}