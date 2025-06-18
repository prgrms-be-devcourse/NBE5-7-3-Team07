import axios from 'axios';
import { API_BASE_URL } from '../backend/backendApi';

// 모든 axios 요청에 withCredentials 설정
axios.defaults.withCredentials = true;

// 토큰이 필요없는 요청
export const publicApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키 포함
});

// 토큰이 필요한 요청
export const privateApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키 포함
});

// publicApi 요청 디버깅
publicApi.interceptors.request.use((config) => {
  console.log("=== publicApi 요청 디버깅 ===");
  console.log("요청 URL:", config.url);
  console.log("요청 메서드:", config.method);
  console.log("요청 헤더:", config.headers);
  console.log("요청 데이터:", config.data);
  console.log("요청 withCredentials:", config.withCredentials);
  console.log("Base URL:", config.baseURL);
  return config;
});

// publicApi 응답 디버깅
publicApi.interceptors.response.use(
  (response) => {
    console.log("=== publicApi 응답 성공 ===");
    console.log("응답 상태:", response.status);
    console.log("응답 데이터:", response.data);
    return response;
  },
  (error) => {
    console.error("=== publicApi 응답 오류 ===");
    console.error("오류 상태:", error.response?.status);
    console.error("오류 데이터:", error.response?.data);
    console.error("오류 헤더:", error.response?.headers);
    console.error("전체 오류:", error);
    return Promise.reject(error);
  }
);

// privateApi 생성 시점의 기본 헤더 로그
console.log("privateApi 생성 시점의 기본 헤더:", privateApi.defaults.headers);

// privateApi 요청 인터셉터
privateApi.interceptors.request.use((config) => {
  console.log("=== privateApi 요청 시작 ===");
  console.log("요청 URL:", config.url);
  console.log("요청 메서드:", config.method);
  console.log("요청 헤더:", JSON.stringify(config.headers));
  console.log("axios 기본 헤더:", JSON.stringify(axios.defaults.headers.common));
  console.log("쿠키 전송 여부(withCredentials):", config.withCredentials);

  // Authorization 헤더에 토큰 삽입
  const token = axios.defaults.headers.common['Authorization']?.split(' ')[1];
  if (token) {
    console.log("토큰 발견:", token.substring(0, 10) + "...");
    config.headers.Authorization = `Bearer ${token}`;
    console.log("헤더에 토큰 설정 완료");
  } else {
    console.log("토큰이 없습니다. Authorization 헤더가 설정되지 않았습니다.");
  }

  console.log("인터셉터 실행 후 헤더:", JSON.stringify(config.headers));
  return config;
}, (error) => {
  console.error("privateApi 요청 인터셉터 오류:", error);
  return Promise.reject(error);
});

// privateApi 응답 인터셉터
privateApi.interceptors.response.use(
  (response) => {
    console.log("=== privateApi 응답 수신 ===");
    console.log("응답 URL:", response.config.url);
    console.log("응답 상태:", response.status);
    console.log("응답 헤더:", JSON.stringify(response.headers));

    if (response.headers['set-cookie']) {
      console.log("Set-Cookie 헤더 발견:", response.headers['set-cookie']);
    } else {
      console.log("Set-Cookie 헤더 발견 안됨");
    }

    return response;
  },
  async (error) => {
    console.error("privateApi 응답 오류:", error.response?.status, error.message);

    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        console.log("401 오류로 인한 토큰 재발급 시도");
        const response = await postRefreshToken();
        const newAccessToken = response.data.accessToken;

        axios.defaults.headers.common['Authorization'] = `Bearer ${newAccessToken}`;
        originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
        return axios(originalRequest);
      } catch (refreshError) {
        console.error('Token refresh failed:', refreshError);
        window.location.href = "/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// refreshToken 재발급 요청
export async function postRefreshToken() {
  console.log("refreshToken 재발급 요청 시작");
  const token = axios.defaults.headers.common['Authorization']?.split(' ')[1];
  try {
    const response = await privateApi.post(
      '/api/refresh',
      {}, // 빈 body
      {
        withCredentials: true, // 쿠키가 함께 전송되도록 명시적으로 설정 (매우 중요)
        
        // 토큰이 있을 경우에만 헤더에 포함
        ...(token ? { headers: { Authorization: `Bearer ${token}` } } : {})
      }
    );
    
    console.log('토큰 갱신 응답 상태:', response.status);
    console.log('토큰 갱신 응답 헤더:', JSON.stringify(response.headers));
    
    // 응답에서 새 토큰 확인 (헤더에서 가져오기)
    const authHeader = response.headers?.authorization || response.headers?.['authorization'];
    if (authHeader && authHeader.startsWith('Bearer ')) {
      const newToken = authHeader.substring(7);
      console.log('응답 헤더에서 새 토큰 발견:', newToken.substring(0, 10) + "...");
      
      // response.data에 accessToken 추가
      if (!response.data) {
        response.data = {};
      }
      response.data.accessToken = newToken;
      console.log('응답 데이터에 accessToken 추가됨:', newToken.substring(0, 10) + "...");
      
      // 새 토큰으로 헤더 즉시 업데이트 (기존 방식 유지 - 순환 참조 방지)
      axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
      console.log('axios 기본 헤더에 새 토큰 설정됨');
      
      // localStorage에 accessToken 저장 (기존 방식 유지 - 순환 참조 방지)
      localStorage.setItem('accessToken', newToken);
      console.log('localStorage에 accessToken 저장됨');
    } else {
      console.warn('응답 헤더에서 Authorization 토큰을 찾을 수 없습니다.');
    }
    return response;
  } catch (error) {
    console.error('postRefreshToken error:', error);
    window.location.href = "/login";
    throw error;
  }
}
