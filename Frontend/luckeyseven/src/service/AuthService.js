import { privateApi, publicApi } from "./ApiService";
import axios from 'axios';
import { postRefreshToken } from "./ApiService";

// 사용자 정보 저장
let currentUser = null;

// 로컬 스토리지에서 사용자 정보와 토큰 복원
try {
    // 사용자 정보 불러오기
    const storedUser = localStorage.getItem('currentUser');
    if (storedUser) {
        currentUser = JSON.parse(storedUser);
    }
    
    // accessToken 불러오기 및 헤더에 설정
    const accessToken = localStorage.getItem('accessToken');
    if (accessToken) {
        console.log("로컬 스토리지에서 accessToken 복원:", accessToken.substring(0, 10) + "...");
        axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
        console.log("로컬 스토리지의 accessToken으로 Authorization 헤더 설정됨");
    }
} catch (error) {
    console.error("로컬 스토리지 데이터 로드 오류:", error);
}

export const join = async (req) =>{
    try{
        const resp = await publicApi.post("/api/users/register", req);
        return resp.data;
    }catch(err){
        console.error("회원가입 오류:", err);
        throw err;
    }
}

export const login = async(req) => {
    try {
        console.log("로그인 요청 데이터:", req);
        
        // withCredentials를 명시적으로 설정하여 쿠키를 받을 수 있도록 함
        const resp = await privateApi.post("/api/users/login", req, {
            withCredentials: true
        });
        
        console.log("[1]로그인 응답 데이터:", resp.data);
        console.log("로그인 응답 헤더:", resp.headers);
        
        
        // 쿠키 확인 (HttpOnly 쿠키는 보이지 않음)
        console.log("로그인 후 쿠키 (HttpOnly 제외):", document.cookie);
        
        // 토큰 설정 - 응답 헤더에서 가져옴
        let authHeader = null;
        
        // 다양한 방식으로 헤더 접근 시도
        if (resp.headers.get) {
            authHeader = resp.headers.get('authorization');
        }
        
        if (!authHeader && resp.headers.authorization) {
            authHeader = resp.headers.authorization;
        }
        
        if (!authHeader && resp.headers['authorization']) {
            authHeader = resp.headers['authorization'];
        }
        
        if (authHeader && authHeader.startsWith('Bearer ')) {
            const accessToken = authHeader.substring(7);
            console.log("액세스 토큰 발견:", accessToken.substring(0, 10) + "...");
            
            // Authorization 헤더 설정
            axios.defaults.headers.common['Authorization'] = `Bearer ${accessToken}`;
            console.log("Authorization 헤더 설정 완료");
            
            // 헤더 설정 확인 로그 추가
            console.log("설정된 Authorization 헤더:", axios.defaults.headers.common['Authorization']);
            
            // localStorage에 accessToken 저장 (이전에는 저장하지 않았지만, 이제 저장함)
            localStorage.setItem('accessToken', accessToken);
            console.log("accessToken을 localStorage에 저장했습니다.");
        } else {
            console.log("응답 헤더에서 액세스 토큰을 찾을 수 없습니다.");
        }
        
        // 사용자 정보 저장
        const email = resp.data.email || req.email;
        const nickname = resp.data.nickname || req.email.split('@')[0];
        
        currentUser = { email, nickname };
        console.log("저장된 사용자 정보:", currentUser);
        
        // 로컬 스토리지에 사용자 정보 저장 (토큰은 저장하지 않음)
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        
        // refreshToken은 HttpOnly 쿠키로 설정되어 있어 접근 불가능
        console.log("refreshToken은 HttpOnly 쿠키로 설정되어 있어 자바스크립트에서 접근할 수 없습니다.");
        console.log("로그아웃 시 쿠키가 자동으로 전송됩니다.");
        
        // 인증 상태 최종 확인
        console.log("=== 로그인 후 인증 상태 확인 ===");
        console.log("메모리의 currentUser:", currentUser);
        console.log("Authorization 헤더:", axios.defaults.headers.common['Authorization']);
        console.log("privateApi 기본 헤더:", privateApi.defaults.headers);
        
        return resp.data;
    } catch(err) {
        console.error("로그인 API 오류:", err);
        throw err;
    }
}


export const getCurrentUser = () => {
    console.log("getCurrentUser 호출됨");
    console.log("현재 메모리의 currentUser:", currentUser);
    console.log("현재 Authorization 헤더:", axios.defaults.headers.common['Authorization'] || "없음");
    
    // 현재 메모리에 사용자 정보가 없는 경우 로컬 스토리지에서 확인
    if (!currentUser) {
        console.log("메모리에 currentUser가 없음, localStorage 확인 중...");
        try {
            const storedUser = localStorage.getItem('currentUser');
            if (storedUser) {
                console.log("localStorage에서 사용자 정보 발견:", storedUser);
                currentUser = JSON.parse(storedUser);
                // accessToken은 localStorage에 저장하지 않으므로 확인하지 않음
            } else {
                console.log("localStorage에 사용자 정보 없음");
            }
        } catch (error) {
            console.error("로컬 스토리지 데이터 로드 오류:", error);
        }
    }
    
    console.log("최종 반환되는 currentUser:", currentUser);
    return currentUser;
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
        
        // HttpOnly 쿠키는 자바스크립트에서 접근할 수 없지만,
        // withCredentials: true 설정으로 쿠키가 자동으로 요청에 포함됨
        try {
            // 쿠키가 자동으로 포함되도록 명시적으로 withCredentials 설정
            const response = await privateApi.post("/api/users/logout", {}, {
                withCredentials: true,
                // 백엔드에서 올바른 CORS 설정이 필요함:
                // - Access-Control-Allow-Origin: http://localhost:3000 (프론트엔드 도메인)
                // - Access-Control-Allow-Credentials: true
            });
            
            console.log("로그아웃 성공:", response.data);
        } catch (apiError) {
            console.error("로그아웃 API 오류:", 
                apiError.response?.status, 
                apiError.response?.data || apiError.message);
                
            if (apiError.response?.status === 400) {
                console.error("로그아웃 실패 - 쿠키가 전송되지 않았을 가능성이 있습니다.");
                console.error("백엔드 CORS 설정과 쿠키 설정을 확인하세요.");
            }
            
            // API 오류가 발생해도 클라이언트 상태 정리는 진행
        }
        
        // 항상 실행되는 클라이언트 상태 정리
        // 현재 사용자 정보 초기화
        currentUser = null;
        
        // 로컬 스토리지에서 사용자 정보 제거
        localStorage.removeItem('currentUser');
        
        // localStorage에서 accessToken 제거
        localStorage.removeItem('accessToken');
        console.log("localStorage에서 accessToken 제거됨");
        
        // Authorization 헤더 제거
        delete axios.defaults.headers.common['Authorization'];
        
        console.log("로그아웃 후 인증 상태:");
        console.log("- currentUser:", currentUser);
        console.log("- Authorization 헤더:", axios.defaults.headers.common['Authorization'] || "없음");
        
        console.log("로그아웃 완료: 사용자 정보 및 토큰 제거됨");
        
        // 서버 로그아웃에 실패해도 클라이언트 로그아웃은 성공으로 처리
        return { success: true };
    } catch (err) {
        console.error("로그아웃 처리 중 오류:", err);
        
        // 오류가 발생해도 클라이언트 상태 정리
        currentUser = null;
        localStorage.removeItem('currentUser');
        localStorage.removeItem('accessToken'); // accessToken도 제거
        delete axios.defaults.headers.common['Authorization'];
        
        return { success: false, error: err.message };
    }
}

// 토큰 갱신 요청 함수 추가
export const refreshAccessToken = async () => {
    try {
        console.log("토큰 갱신 요청 시작");
        console.log("현재 인증 상태:");
        console.log("- currentUser:", currentUser);
        console.log("- Authorization 헤더:", axios.defaults.headers.common['Authorization']);
        
        // 쿠키에 refreshToken이 있는지 확인할 수 없음 (HttpOnly)
        console.log("refreshToken은 HttpOnly 쿠키로 설정되어 있어 확인 불가능");
        
        // refreshToken을 사용하여 accessToken 갱신 요청
        const response = await postRefreshToken();
        console.log("토큰 갱신 응답:", response.data);
        
        // 새 액세스 토큰 설정
        const newAccessToken = response.data.accessToken;
        if (newAccessToken) {
            console.log("새 액세스 토큰 설정:", newAccessToken.substring(0, 10) + "...");
            axios.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`;
            
            // 갱신 후 인증 상태 확인
            console.log("갱신 후 Authorization 헤더:", axios.defaults.headers.common['Authorization']);
            
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
    } catch (error) {
        console.error("토큰 갱신 실패:", error);
        
        if (error.response) {
            console.error("서버 응답:", error.response.status, error.response.data);
        }
        
        // 에러 발생 시 인증 상태 초기화 여부 결정
        if (error.response && error.response.status === 401) {
            console.warn("인증 만료: 사용자 정보 초기화");
            currentUser = null;
            localStorage.removeItem('currentUser');
            delete axios.defaults.headers.common['Authorization'];
        }
        
        return { 
            success: false, 
            error: error.message,
            status: error.response?.status
        };
    }
};